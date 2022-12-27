from json.decoder import JSONDecodeError
from django.http import HttpResponse
from django.conf import settings
import http.client
import urllib.parse
from frontend.models import Session
from frontend.models import RelayMode
from django.utils import timezone
from pathlib import Path
if getattr(settings, "WITH_KEYCHAIN", False):
    import keychain
import sys
import json
import logging
from django.views.decorators.csrf import csrf_exempt
import re


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

    # Only allow 'POST'
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
    else:
        # was 'body'
        clear_text_msg = request.body
    
    #  Now we have the clear text, decode as JSON
    msg = None
    force_pass = False
    try:
        json_data = json.loads(clear_text_msg)
        msg = json_data["msg"]
        regex_str = r'[^#*|$%&"\']{0,10240}'
        if not re.match(regex_str, msg):
            raise Exception('value %s passed as msg failed to match regex: %s' % (msg, regex_str))
    except JSONDecodeError:
        # Added for the case where Front can not decrypt Client<>Back data, or isn't using Keychain but Front<>Back is
        view_logger.warning('value was not JSON; forwarding to backend')
        msg = clear_text_msg
        force_pass = True
    except Exception as e:
        view_logger.exception(e)
        result = { 'response_code': return_codes['BadInput'] }
        return HttpResponse(json.dumps(result))

    
    s = Session(received_msg=msg, receive_time=timezone.now())
    s.save()

    modes = RelayMode.objects.all()
    if len(modes) == 0:
        mode = RelayMode()
        mode.save()
        modes = RelayMode.objects.all()

    mode = modes[0].mode
    if force_pass:
        mode = 'PassThrough'
    
    conn = http.client.HTTPConnection(getattr(settings, "BACKEND_SERVER"))

    if mode == 'PassThrough':
        view_logger.debug(mode)
        try:
            # This should work for Keychain and plain
            if force_pass:
                params = msg
            elif getattr(settings, "WITH_KEYCHAIN", False):
                # Pass through the encrypted body as-is
                params = request.body
            else:
                params = json.dumps({'msg': msg})
            headers = { "Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain" }
            conn.request("POST", "/backend/recv", params, headers)
            res = conn.getresponse()
            s.sent_msg = params
            s.send_time = timezone.now()
            s.save()
        except:
            view_logger.error("An exception occurred: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

    elif mode == 'Insert':
        view_logger.debug(mode)
        new_msg = json.dumps({'msg': '12345'})

        # send a new message
        try:
            params = new_msg
            if getattr(settings, "WITH_KEYCHAIN", False):
                contacts = g.get_contacts()
                params = g.sign_then_encrypt(params, contacts)
            
            headers = { "Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain" }
            conn.request("POST", "/backend/recv", params, headers)
            res = conn.getresponse()
            res.read()
            s.sent_msg = params
            s.send_time = timezone.now()
            s.save()
        except:
            view_logger.error("An exception occurred: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

        # relay original message
        try:
            params = json.dumps({'msg': msg})
            if getattr(settings, "WITH_KEYCHAIN", False):
                # Pass through the encrypted body as-is
                params = request.body
            
            headers = { "Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain" }
            conn.request("POST", "/backend/recv", params, headers)
            res = conn.getresponse()
            s.sent_msg = params
            s.send_time = timezone.now()
            s.save()
        except:
            view_logger.error("An exception occurred: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

    elif mode == 'Modify':
        view_logger.debug(mode)

        # add a new field to modify the message
        new_msg = 'MODIFIED BY HACKER'
        modified_clear_text = json.dumps({'msg': msg, 'added': new_msg})
        view_logger.debug("Sending " + modified_clear_text)
        try:
            params = modified_clear_text
            if getattr(settings, "WITH_KEYCHAIN", False):
                contacts = g.get_contacts()
                params = g.sign_then_encrypt(params, contacts)
            
            headers = { "Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain" }
            conn.request("POST", "/backend/recv", params, headers)
            res = conn.getresponse()

            s.sent_msg = params
            s.send_time = timezone.now()
            s.save()
        except:
            view_logger.error("An exception occurred: " + str(sys.exc_info()[0]))
            result = { 'response_code': return_codes['InternalSystemError'] }
            return HttpResponse(json.dumps(result))

    else:
        view_logger.error("Bad mode: " + str(mode))
        result = { 'response_code': return_codes['InternalSystemError'] }
        return HttpResponse(json.dumps(result))

    result = { 'response_code': return_codes['OK'] }

    return HttpResponse(json.dumps(result))
