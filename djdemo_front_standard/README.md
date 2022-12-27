# Frontend
This project is the Django frontend server used in the man-in-the-middle demo.

The frontend server represents a permissioned server such as you would find in a system where IoT devices communicate data to a gateway which sends it to a backend.  The frontend in our demo however is malicious and will try to read and modify the user data.

# Install

Please see the `INSTALL.md` file located in the root of this subapplication for detailed instructions on how to install the frontend application.

# Running the Frontend Server

To run the frontend, we start django using the provided run script.
There is a single script which will run the server in either normal (no Keychain) or Keychain-aware mode.
The environment variables are properly set inside the scripts so you do not need to do additional exports.

The script takes 2-3 arguments: the port number to listen on (convention is 8xxx for no Keychain, 9xxx for Keychain), a Trusted Directory suffix that should be set to a value unique to your company/project, and a string specifying whether to use Keychain or not (no 3rd argument = no Keychain)

```bash
# Without Keychain, uses front.settings (front/settings.py file), listen on port 8000 with suffix 12345
scripts/run-demo.sh 8000 12345

# With Keychain, uses front.settings_keychain (front/settings_keychain.py file), listen on port 9000 with suffix abcde
scripts/run-demo.sh 9000 abcde y

# Specify KEYCHAIN_HOME directly - otherwise it uses ~/.keychain
KEYCHAIN_HOME=/path/to/my/keychain scripts/run-demo.sh 9000 abcde y
```

# Configuration

There are 2 places where configuration happens - the run command, and the Django settings file.

## `run-demo.sh`

This is the run script that starts Django.  There are 2 settings here:
* `PORT_LISTEN`: this is the 1st argument to the script, and is the port your Django server will listen to.  If you use a reverse proxy, make sure it **also** listens on this port and forwards traffic to it (this is the easiest setup) and that your Java client can connect to it
* `TD_SUFFIX`: this is the 2nd argument to the script, and is used only for Keychain.  It is a unique suffix for the trusted directories the project should connect to.  Please set it to the same value for all applications that connect to each other (client+front+back, or Keychain client+front+back).


## `front/settings.py` (`front/settings_keychain.py`)

This is the settings file used by Django to configure the server.

There are a few settings you may wish to change, shown below
* `DEMO_USE_SSL`: set it to `True` if you are going to use SSL in your servers.  Setup of SSL certificates should be done by your server admin team or developers (if dev)
* `ALLOWED_HOSTS`: this is a list of IPs/Domains this server will serve as.  You may need to add your server's IP/Domain to this list
* `SECURE_SSL_REDIRECT`, `SECURE_PROXY_SSL_HEADER`, `CSRF_COOKIE_SECURE`, `SESSION_COOKIE_SECURE` - if you use SSL these may need to be set.  Please see the links provided in the comments
* `BACKEND_SERVER`: this is the `IP:Port` (or `Domain:Port`) of your backend server
* `KEYCHAIN_DB_FOLDER`: this is the folder for the Keychain database.  Recommend using a local folder
* `KEYCHAIN_DB_FILE`: this is the file name for the Keychain database
* `DJANGO_DB_FILE`: this is the Django sqlite database file.  If you run 2+ instances from 1 folder, make sure the settings files use different databases!


# Behavior

By default the frontend server will do the following when it receives a message from the client:
1. Check that the message is a `POST` and has no disallowed characters
1. If Keychain-enabled, attempt to decrypt the message (should succeed)
1. Look in the message for a key `msg` to verify that it can read the client's data
1. Execute a 'modify' hack, where it puts something into a new key `added` which the client did not intend
1. Send the payload `{ msg, added }` to the backend server

The default mode is 'Modify', but you can log on to the admin page (frontend server URL+Port + `/admin`) and change the `RelayMode` to `PassThrough` or `Insert`.

You go to `<URL>/admin/frontend/relaymode/` (use the appropriate URL), then click the single entry under `Relay Mode` (by default it will be `Modify`).  This gives you a dropdown of modes to choose, which take effect immediately.

| Mode | Definition |
| - | - |
| PassThrough | Send the message exactly as-is to the backend |
| Modify | Modify the payload by adding a new key to it |
| Insert | Sends a new message `{ msg: "12345" }` to backend before forwarding the client message |

If the frontend is unable to parse the client message as JSON (**not** failure to decrypt) it will forward the message as-is to the backend.  A failure to decrypt will result in the frontend rejecting back to the client.

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