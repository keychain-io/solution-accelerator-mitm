# Pre-requisites

## Environment
To build and run the client, you need to have the following:

* Java 8+ JDK (**not** just the JRE)
* Java 8+ JDK on the path, or else `JAVA_HOME` set to the absolute path to your Java installation that has `bin/java` and `bin/javac` in it (e.g. `/usr/lib/java/java-11.0.0`)

For running with Keychain enabled, you must also ensure you have the following:

* Keychain Core 2.2.0 or above installed (`$HOME/.keychain` should exist and have `config/keychain.cfg` and `lib/libkeychain.so`) 
* Keychain Java 2.2.0 or above installed (`$HOME/.keychain/lib/java/keychain.jar` and `$HOME/.keychain/lib/libkeychain-jni.so`)

The Keychain libraries can be downloaded from JFrog.  Please ask a customer service representative for assistance.

## Other
After download and installing (follow the install instructions in the packages) you will find `$HOME/.keychain/config/keychain.cfg`.

You must add your `API_KEY` to it in order to run a Keychain app.

Instructions will be in the package.


# Building the Client

The client can be built using the included script `build.sh`.

```bash
cd scripts
./build.sh
```

The result will be class files compiled into `target/classes/`
