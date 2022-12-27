# Create your views here.
from django.http import HttpResponse
from django.conf import settings
from django.utils import timezone
from backend.models import Session
from pathlib import Path
import sys
import json
import logging
from django.views.decorators.csrf import csrf_exempt
import re

if getattr(settings, "WITH_KEYCHAIN", False):
    import keychain

view_logger = logging.getLogger('mitm')

return_codes = {
    'OK': 'OK',
    'BadInput': 'V001_BAD_INPUT',
    'MethodNotSupported': 'V002_NOT_SUPPORTED',
    'NotImplementedYet': 'V003_NYI',
    'InternalSystemError': 'S001_SYSTEM_ERROR',
}


@csrf_exempt
def recv(request):
    view_logger.debug("############")
    view_logger.debug(request.method + ' ' + request.path + ' ')

    # Only accept 'POST'
    if request.method != "POST":
        result = { 'response_code': return_codes['MethodNotSupported'] }
        return HttpResponse(json.dumps(result))

    # Make sure we get only allowed characters
    try:
        regex_str = r'[^#*|$%&\']{0,10240}'
        body = request.body.decode('utf-8')
        view_logger.debug("request body: " + body)
        if not re.match(regex_str, body):
            raise Exception('value %s passed as msg failed to match regex: %s' % (body, regex_str))

    except Exception as e:
        view_logger.exception(e)
        result = { 'response_code': return_codes['BadInput'] }
        return HttpResponse(json.dumps(result))

    
    # If this server is Keychain-enabled, we must try to decrypt the text
    if getattr(settings, "WITH_KEYCHAIN", False):
        # Make the Gateway
        try:
            home = str(Path.home())
            keychain_db = getattr(settings, "KEYCHAIN_DB_FILE_PATH")
            setting = keychain.Gateway.init(home + '/.keychain/config/keychain.cfg', keychain_db, 
                                            False, home + '/.keychain/data/drop_keychain.sql', home + '/.keychain/data/keychain.sql')
            g = keychain.Gateway(setting, keychain_db)
        except:
            view_logger.exception("Exception occured while initializing Keychain: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

        # Decode to clear text
        clear_text_msg = None
        encoding = None
        results = None
        payload = bytearray(len(request.body))
        payload[:] = request.body

        try:
            clear_text_msg, encoding, results = g.decrypt_then_verify(payload)
        except:
            view_logger.exception("Exception occured while decrypting: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

        view_logger.debug('clear text: ' + clear_text_msg)

        # Verify that the signer exists, is who he says he is, and sent this exact message
        verified = False
        signer_known = False
        
        # This should be defined in settings_keychain.py
        td_suffix = getattr(settings, 'TRUSTED_DIRECTORY_SUFFIX', '')

        for i in range(len(results)):
            facade = results[i].get_facade()
            if results[i].is_verified():
                verified = True
                if facade.subname() == 'mitm-legit' + td_suffix:
                    signer_known = True

        accepted = False
        if len(results) > 0:
            if verified == False:
                view_logger.warn("Message signed but no signatures verified")
            else:
                if signer_known == True:
                    view_logger.info("Message verified and ACCEPTED by known signer")
                    accepted = True
                else:
                    view_logger.warn("Message verified but not by expected signer")

    else:
        # was 'body'
        clear_text_msg = body
        view_logger.debug("message: " + clear_text_msg)
        accepted = True

    #  Now we have the clear text, decode as JSON
    msg = None
    try:
        json_data = json.loads(clear_text_msg)
        msg = json_data["msg"]
        regex_str = r'[^#*|$%&"\']{0,10240}'
        if not re.match(regex_str, msg):
            raise Exception(
                'value %s passed as msg failed to match regex: %s' % (msg, regex_str))
        
    except Exception as e:
        view_logger.exception(e)
        result = { 'response_code': return_codes['BadInput'] }
        return HttpResponse(json.dumps(result))

    view_logger.debug("Updating session")
    s = Session(received_msg=msg, accepted=accepted, receive_time=timezone.now())
    s.save()

    result = { 'response_code': return_codes['OK'] }
    view_logger.debug("Sending result = " + json.dumps(result))
    return HttpResponse(json.dumps(result))
