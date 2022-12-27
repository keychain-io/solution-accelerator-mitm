# Client
This project is the Java client for the man-in-the-middle demo.

The Java client represents a user who wants to send data to a backend server, but via a frontend server that acts as a middle-man.

The Client can be run with or without Keychain support to demonstrate how Keychain encryption protects the client data in cases where SSL/TLS do not provide adequate guarantees.

# Install

Please check the `INSTALL.md` file for detailed instructions on how to get the environment prepared for this application.

# Running the Client

Running the client is done with the 2 run scripts, `run-demo.sh` and `run-demo_keychain.sh` in the script folder.

```bash
cd scripts

# run without Keychain enabled
./run-demo.sh

# run with Keychain enabled
./run-demo_keychain.sh
```

Please make sure to look into the scripts to change any variables needed for your setup.  Specifically

* `FRONTEND_URL`: this is the full URL of the frontend server, starting with `http` or `https` and ending with the REST endpoint (`front/recv`).  Keep it synchronized with the frontend server `settings.py`!
* `KEYCHAIN_DB_FOLDER`: this is the folder where Keychain puts its database file (below).  Best practice is to use a local folder to the project, **not** something global like `/home/user/.keychain/data`
* `KEYCHAIN_CFG_FOLDER`: this is the folder where Keychain's configuration resides.  Best practice is to use a local folder to the project
* `KEYCHAIN_DB_FILE`: this is the name of the database file that Keychain uses.  Make sure it is unique for the project
* `KEYCHAIN_CFG_FILE`: this is the name of the configuration file that Keychain uses.
* `TD_SUFFIX`: this is used only for Keychain.  It is a unique suffix for the trusted directories the project should connect to.  Please set it to the same value for all applications that connect to each other (client+front+back, or Keychain client+front+back) and change it from the value the demo comes with (`12345k`) to avoid collision.

In addition, you can add to `scripts/run-demo_keychain.sh` on the JVM command line the following 2 arguments
* `-Dkeychain.trusted.directory.host=HOST`: if you want to use a non-Keychain trusted directory, put the IP here
* `-Dkeychain.trusted.directory.port=PORT`: if you want to use a non-Keychain trusted directory, put the port here

By default they will be the Keychain values inside the code.  If the values are passed in via JVM, we use those instead.


# Client Output

The client will send JSON data every 7 seconds to the frontend server, printing the data and the response.  It should look like this:

```bash
Jul 09, 2021 3:12:59 PM io.keychain.mitm.Client sendReading
INFO: Sending Message: {"msg":"{\"readings\":[{\"accumulatedAmount\":10,\"readingDateTime\":\"2021-07-09T15:12:59.467150\",\"direction\":\"0\"},{\"accumulatedAmount\":12.3,\"readingDateTime\":\"2021-07-09T15:12:59.467204\",\"direction\":\"1\"}],\"meterId\":\"ttoyosu1meter\"}"}

Jul 09, 2021 3:12:59 PM io.keychain.mitm.Client sendReading
INFO: Response: {"response_code": "OK"}

Jul 09, 2021 3:13:06 PM io.keychain.mitm.Client sendReading
INFO: Sending Message: {"msg":"{\"readings\":[{\"accumulatedAmount\":10,\"readingDateTime\":\"2021-07-09T15:13:06.569977\",\"direction\":\"0\"},{\"accumulatedAmount\":12.3,\"readingDateTime\":\"2021-07-09T15:13:06.570067\",\"direction\":\"1\"}],\"meterId\":\"ttoyosu1meter\"}"}

Jul 09, 2021 3:13:06 PM io.keychain.mitm.Client sendReading
INFO: Response: {"response_code": "OK"}
```

# Troubleshooting

The most common problems and solutions are listed below.

| Problem | Solution |
| - | - |
| `java`/`javac` not found | Make sure you have the JDK installed.  Set `JAVA_HOME` to it if it is not on the path |
| Keychain load fails | Check that `$HOME/.keychain/lib` exists and has `libkeychain.so`, `libkeychain-jni.so` and `java/keychain.jar` inside.  Make sure `LD_LIBRARY_PATH` is set to `$HOME/.keychain/lib` (it is in `run-demo.sh` and `run-demo_keychain.sh`) |
| Keychain is found but the app crashes | Make sure the `KEYCHAIN_DB_FOLDER` is created or you have permissions to create (the script makes it).  If you force stopped the app earlier, the DB may be corrupt, so try removing the `KEYCHAIN_DB_FILE` and start again |
| Can't connect to frontend | Make sure `FRONTEND_URL` is correct, the frontend server is listening, and that you have access to it from the client machine |
| Data isn't decrypted by frontend / `INFO: Response: {"response_code": "S001_SYSTEM_ERROR"}` | Look at the frontend / backend logs to see what happened.  If you just created a persona, it may take 30-60 seconds for the directory to update.  Until then the 3 servers will not have each others' keys |
| Various Keychain Errors | Delete the `data` folder for Keychain and restart.  It is possible for the DB to be inconsistent if the application is Ctrl+C terminated while a persona is being created. |
