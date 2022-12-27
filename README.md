# man-in-the-middle-web-demo

## ⚠️ Version 2.4.0

This project demonstrates the additional data security Keychain Core provides against man-in-the-middle attacks between an IoT client and a web server.

The project supports the use of SSL/TLS or normal HTTP communication, but shows how even when SSL is used, an authorized man in the middle (not a network sniffer) can easily read and modify the client data on its way to a backend server without the server being aware.  This is because SSL/TLS encrypts only over the wire.

With Keychain, the client is able to encrypt his data and sign it so that the frontend middleman can neither read it, nor modify it without the backend server being aware that the data is not from the client. This is depicted in the following image.

![Man-in-th-Middle](/assets/images/mitm.jpg "San Juan Mountains")

## Installation Instructions

### Pre-requisites

#### Client

To build and run the client, you need to have the following:

Java 8+ JDK (not just the JRE)
Java 8+ JDK on the path, or else JAVA_HOME set to the absolute path to your Java installation that has bin/java and bin/javac in it (e.g. /usr/lib/java/java-11.0.0)

For running with Keychain enabled, you must also ensure you have downloaded and installed latest 2.4 version of [Keychain Core Java package](https://keychain.jfrog.io/artifactory/keychain-core-release-generic/linux/x64/keychain-java)

#### Frontend and Backend Servers

At a minimum, the following is required to run the backend server:

* Python 3
* Django 3.2.x

Here is an example of what you might use to install these on your system, if they are not there already.

<pre>
sudo apt-get install python3.7
sudo apt install python3-pip
python3 -m pip install Django==3.2.5
</pre>


Note: Your python command may be different (e.g. python or /path/to/python3) depending on your system, installation and path. Please use the appropriate command.

To run the backend server with Keychain enabled, you must also have the latest 2.4 versions of the [Keychain Core C++ package](https://keychain.jfrog.io/artifactory/keychain-core-release-generic/linux/x64/keychain/) and [Keychain Core Python package](https://keychain.jfrog.io/artifactory/keychain-core-release-generic/linux/x64/keychain-python/) libraries installed.


### Installation

#### Client

To install the client for the Man-in-the-Middle sample, please follow the [Client Installation Instructions](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/client_standard/INSTALL.md).

For information on how to run the client, please see the [README file](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/client_standard/README.md).

#### Backend Server

To install the backend server, please follow the [Backend Server Installation Instructions](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/djdemo_back_standard/INSTALL.md).

For additional information on how to run the backend server, please review this [README file](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/djdemo_back_standard/README.md).

#### Frontend Server

To install the backend server, please follow the [Frontend Server Installation Instructions](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/djdemo_front_standard/INSTALL.md)

For additional information on how to run the backend server, please review this [README file](https://github.com/keychain-io/man-in-the-middle-web-demo/blob/2.4/djdemo_front_standard/README.md).

#

# Architecture / Cases

| # | Client | Frontend | Backend | HTTPS | Hackers can Read? | Front can Read? | Front can Write? | Backend can Verify? |
| - | - | - | - | - | - | - | - | - |
| 1 | Plain | Plain | Plain | No | Yes | Yes | Yes | No |
| 2 | Plain | Plain | Plain | SSL | No | Yes | Yes | No |
| 3 | Keychain | Plain | Keychain | No | No | No | No | Yes |
| 4 | Keychain | Plain | Keychain | SSL | No | No | No | Yes |
| 5 | Keychain | Keychain | Keychain | No | No | Yes* | No | Yes |
| 6 | Keychain | Keychain | Keychain | SSL | No | Yes* | No | Yes |

In the above table, we see the following conclusions:
* SSL only protects against reading by a "hacker", someone who is not expected to be in the middle of a transmission
* With Keychain between the client and backend, **no one** can read or modify the data such that the recipient can not verify it.  It is completely safe.
* With Keychain also on the frontend, we can make it so that the frontend is able to read the data (there may be cases where this is desirable) or limit it so the frontend can still not read it.  It is up to the client who he wants to read his data.

**Note**: to support cases #3 and #4, you should run the frontend using `run-demo.sh` but change the port and URL in that script and the settings file to point to the Keychain-enabled backend

# Functionality / Behavior

This demo connects 3 machines to each other in a relay, where an upstream client will send a message through a frontend to a downstream backend.

The frontend may pass the message reliably, alter it, or insert its own message, depending on configuration (see the frontend `README.md`).

The goal of this demo is to see how, when Keychain is enabled, the backend is able to detect such malicious intents from the frontend and verify that the message has been tampered with.

# Directories

There are a few Trusted Directories used by the systems.

A trusted directory is simply an online repository of public keys that applications can upload to, and download from.  The idea is that a company would host a "trusted directory" of user keys for others to easily find.

You see these in the code when a `DirectoryThread` or `keychain.Directory` is created.  Each application uploads to, and reads contacts from, 2 directories, as below.

**Note**: these are the *non-suffixed* names.  Your application will put a value at the end, `TD_SUFFIX`, to distinguish them from other client demos.

| TD Name | Client | Front | Back |
| - | - | - | - |
| mitm-legit | o | | o |
| mitm-hacker-front | o | o | |
| mitm-hacker-back | | o | o |

The client application will upload its public key to 2 directories, `mitm-legit` and `mitm-hacker-front`.  The frontend uploads to `mitm-hacker-front` and `mitm-hacker-back`.  The backend uploads to `mitm-hacker-back` and `mitm-legit`.

This means that all applications, as configured, will know about each others' public keys and therefore be able to encrypt messages for each other.

Furthermore, in our code the client will encrypt his messages in a way that **all** his contacts - so every key from `mitm-legit` and `mitm-hacker-front`, i.e. the frontend and backend - can decrypt his message.  This lets the frontend **read** his message.

Keychain applications know what directory each public key for their contacts came from.  So the backend server is able to determine whether a message he received (and decrypted) came from `mitm-legit` or `mitm-hacker-back`.  You can see this in the backend's `backend/views.py` code.

It is this check that allows the backend to validate that the sender of a message is verified and trustworthy (`mitm-legit` is trustworthy, `mitm-hacker-back` is not).

# Connecting The Pieces

We have provided 4 scripts for convenience: 1 script for Keychain and 1 for plaintext on the Client, and 1 for each of the Django servers (it does Keychain and plaintext).

The scripts are located in the following places.  Please note that if you move them relative to those locations they may no longer work because of relative paths, expected files, etc.  You will have to troubleshoot any problems on your own.

```bash
client_standard/scripts/run-demo.sh
client_standard/scripts/run-demo_keychain.sh

front_standard/run-demo.sh

back_standard/run-demo.sh
```

When you are ready to run the projects, you make the following choices:
* With or without Keychain
* Frontend server location and port
* Backend server location and port
* Database location and name for Keychain (if used)

Then you need to modify the appropriate files, and you should be all set!

Below we cover the files, the variables, and some examples.

## Client

The Java client runs in the JVM, which accepts arbitrary command line arguments with `-D`.  This lets us place all configuration in the `run-demo.sh` and `run-demo_keychain.sh` scripts and avoid `export` commands that would otherwise pollute the environment.  We want to avoid `export` because it makes it difficult to run the **full** demo (i.e. all 6 projects at once) on a single laptop.

The main values that need to be set in the client are the full URL of the frontend server, the full path to the Keychain database file, and a flag telling the client whether to use plaintext or Keychain encryption.  The flag is hardcoded in each script because it is dependent on the script itself.

In `run-demo.sh` and `run-demo_keychain.sh` we have already taken care of creating these variables for you and plugging them in to the `java` command at the bottom.  There should not be a reason to change the variable names, simply change the values.

| Variable | Meaning | Examples | Note |
| - | - | - | - |
| `FRONTEND_URL` | Full url of the frontend server | `https://1.2.3.4:8000/frontend/recv` | Don't forget `frontend/recv` at the end!
| `KEYCHAIN_DB_FOLDER` | Keychain database folder | `../data` | Best practice is to use a local-to-project folder |
| `KEYCHAIN_DB_FILE` | Keychain database file name | `client_mitm.db` | |
| `KEYCHAIN_CFG_FOLDER` | Keychain config folder | `../config` | Best practice is to use a local-to-project folder |
| `KEYCHAIN_CFG_FILE` | Keychain config file name | `keychain.cfg` | |
| `TD_SUFFIX` | Keychain Trusted Directory suffix to avoid collision with other clients who may be using the demo (the TD is shared).  It is a unique suffix for the trusted directories the project should connect to.  Please set it to the same value for all applications that connect to each other (client+front+back, or Keychain client+front+back) and change it from the value the demo comes with (`12345k`).
| `WITH_KEYCHAIN` | Optional flag to use Keychain | `-Dwith.keychain` | **Do not change this**

## Front and Back

The frontend and backend servers run in a Python Django server.  The start script lets you configure the basic server (port and whether or not to use Keychain) as well as the Trusted Directory suffix.  Based on whether you use Keychain, it will launch the server pointing to the appropriate settings.py file.

Please see the `README.md` for the projects for more detail about all settings.



# Notes

## SSL

While we have made every attempt to point out SSL-related variables and settings, the final configuration for SSL will be up to the individual client's site preferences and systems.

Please create any required certificates and modify the settings/code for the projects as needed to ensure SSL is connected properly if you choose to use it.

If you are using a reverse proxy such as Nginx and have the applications on separate servers, Django should be able to run with little knowledge of SSL outside of headers.

## Nginx

The configuration we have provided assumes the applications have direct communication with each other on the listed ports.  The demo can stand up *locally* with no additional servers.

However, if you want to use multiple servers and use a reverse proxy such as Nginx to handle SSL, this may not be a valid assumption.

For example, you might configure the frontend application such that Nginx listens on port 8000 but forwards traffic to Django running on port 12345.  Please take care to match your ports and traffic forwarding configurations.x

# Future Improvments

Here are some ideas for expansion:

1. Combine the front and back projects into 1, as they reuse much of the same code and settings
1. Use a virtual env (venv) so it is easier to roll out
