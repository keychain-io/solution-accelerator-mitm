import keychain
from django.conf import settings


keychain_home = getattr(settings, "KEYCHAIN_HOME")
keychain_db = getattr(settings, "KEYCHAIN_DB_FILE_PATH")
keychain_cfg = getattr(settings, "KEYCHAIN_CONFIG_FILE")

keychain_setting = keychain.Gateway.init(keychain_cfg, keychain_db, False, keychain_home + '/data/drop_keychain.sql', keychain_home + '/data/keychain.sql')
keychain_gateway = keychain.Gateway(keychain_setting, keychain_db)
keychain_gateway.seed()

keychain_gateway.on_start()
keychain_gateway.on_resume()

keychain_needs_persona = False
active_persona = None
try:
    active_persona = keychain_gateway.get_active_persona()
except:
    keychain_needs_persona = True


if keychain_needs_persona:
    active_persona = keychain_gateway.create_persona("front", "mitm", keychain.SecurityLevel.MEDIUM)

# This should be defined in settings_keychain.py
td_suffix = getattr(settings, 'TRUSTED_DIRECTORY_SUFFIX', '')

keychain_directory = keychain.Directory(keychain_setting, "mitm-hacker-front" + td_suffix, keychain_gateway, 23)
keychain_directory.on_start()


keychain_directory_2 = keychain.Directory(keychain_setting, "mitm-hacker-back" + td_suffix, keychain_gateway, 23)
keychain_directory_2.on_start()
