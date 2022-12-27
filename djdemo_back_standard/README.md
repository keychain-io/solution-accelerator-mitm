# Backend
This project is the Django backend server used in the man-in-the-middle demo.

The backend server represents a server that may be hosting a database or other system client software wants to access but that needs a frontend/permissioned middleman to communicate between them.

# Install

Please see the `INSTALL.md` file located in the root of this subapplication for detailed instructions on how to install the backend application.

# Running the Backend Server

To run the backend, we start django using the provided run script.
There is a single script which will run the server in either normal (no Keychain) or Keychain-aware mode.
The environment variables are properly set inside the scripts so you do not need to do additional exports.

The script takes 2-3 arguments: the port number to listen on (convention is 8xxx for no Keychain, 9xxx for Keychain), a Trusted Directory suffix that should be set to a value unique to your company/project, and a string specifying whether to use Keychain or not (no 3rd argument = no Keychain)

```bash
# Without Keychain, uses back.settings (back/settings.py file), listen on port 8001 with suffix 12345
scripts/run-demo.sh 8001 12345

# With Keychain, uses back.settings_keychain (back/settings_keychain.py file), listen on port 9001 with suffix abcde
scripts/run-demo.sh 9001 abcde y

# Specify KEYCHAIN_HOME directly - otherwise it uses ~/.keychain
KEYCHAIN_HOME=/path/to/my/keychain scripts/run-demo.sh 9001 abcde y
```

# Configuration

There are 2 places where configuration happens - the run command, and the Django settings file.

## `run-demo.sh`

* `PORT_LISTEN`: this is the 1st argument to the script, and is the port your Django server will listen to.  If you use a reverse proxy, make sure it **also** listens on this port and forwards traffic to it (this is the easiest setup) and that your Java client can connect to it
* `TD_SUFFIX`: this is the 2nd argument to the script, and is used only for Keychain.  It is a unique suffix for the trusted directories the project should connect to.  Please set it to the same value for all applications that connect to each other (client+front+back, or Keychain client+front+back).


## `back/settings.py` (`back/settings_keychain.py`)

This is the settings file used by Django to configure the server.

There are a few settings you may wish to change, shown below
* `DEMO_USE_SSL`: set it to `True` if you are going to use SSL in your servers.  Setup of SSL certificates should be done by your server admin team or developers (if dev)
* `ALLOWED_HOSTS`: this is a list of IPs/Domains this server will serve as.  You may need to add your server's IP/Domain to this list
* `SECURE_SSL_REDIRECT`, `SECURE_PROXY_SSL_HEADER`, `CSRF_COOKIE_SECURE`, `SESSION_COOKIE_SECURE` - if you use SSL these may need to be set.  Please see the links provided in the comments
* `KEYCHAIN_DB_FOLDER`: this is the folder for the Keychain database.  Recommend using a local folder
* `KEYCHAIN_DB_FILE`: this is the file name for the Keychain database
* `DJANGO_DB_FILE`: this is the Django sqlite database file.  If you run 2+ instances from 1 folder, make sure the settings files use different databases!


# Behavior

The backend server simply listens for messages from the frontend server, which it expects to be relayed faithful to what the client sent.

Without Keychain, the backend will only be able to receive the message and confirm that it is JSON.  However, if the frontend modifies it in any way, the limitations of SSL mean the backend can not detect this.

With Keychain, the frontend will add the following behavior:
1. Verify that the message is signed
1. Verify that the signer is someone the backend trusts

An 'Insert' by the frontend will cause the inserted message to fail step #2, but the client's message that follows will pass.

These extra steps show that with Keychain, we gain the ability to:
1. Determine who can read our messages (in this demo all 3 parties can, but we can restrict this)
1. Absolutely prevent modification of the message sent by the client, even by a trusted 3rd party who can decrypt it
1. Detect tampering and validate authenticity of messages

# Troubleshooting

The most common problems and solutions are listed below.

| Problem | Solution |
| - | - |
| No such table XXX | Make sure you have run the `makemigrations XXXX` and `migrate` commands |
| CSRF Cookie Not Set | Make sure you are using `POST` on the full URL including `/frontend/recv`.  Normally this will not occur because the apps handle this for you |
| Decryption fails (Keychain Error) | If you just created a persona for the frontend, client or backend, it will take 30-60 seconds for them all to add themselves to the trusted directories and pull down new contacts.  Please wait a bit and see if it clears up.
| `facade_is_mature` warnings | Make sure your Keychain libraries (Core, Python, Java) all are the same version |
| Various Keychain Errors | Delete the `data` folder for Keychain and restart.  It is possible for the DB to be inconsistent if the application is Ctrl+C terminated while a persona is being created. |
| DB Exception / `database is busy` | This should not cause any issues (just a warning), but occurs occasionally with sqlite3 db files.  It is resolved in newer Keychain versions |