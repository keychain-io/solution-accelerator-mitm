## 1.2.0

### Breaking
* Keychain 2.4.5 update; no more `Monitor` object, now `Gateway` is used
* As written, Python servers will now look to project-local `config/keychain.cfg` file for Keychain configuration (previously was `~/.keychain/config/keychain.cfg`).  Database will be in `data/[dbfile]`

### Quality of Life
* Java Client `DirectoryThread` use newer loops
* Python servers using common script to pull out Keychain library information
* Python servers config and data folder locations are now configurable
* Python server run scripts are combined and now take in arguments
* Installation procedure has been changed


## 1.1.1

### Bugfixes
* Frontend bug fixed with mode 'Insert', where sometimes not reading the response of 1st message caused 2nd message send to fail
* Java Client's 2 instances of DirectoryThread caused race conditions on the Gateway and have been replaced by 1 DirectoryThread with multiple PairHelpers in sequence

### Quality of Life
* Java Client has the ability to specify keychain.cfg location, and default keychain.cfg in 'config/'
* Java Client has fallbacks for DB and cfg file locations if they are not specified on JVM arguments
* Java Client has the ability to quit the client with 'Q' to gracefully stop
* Java Client has Java DirectoryThread and PairHelper hoisted out of core for visibility and improvement possibilities


## 1.1.0

### Breaking
* Refactored 6 subapplications into 3, with appropriate run-scripts.  Keychain/no-Keychain is now differentiated by the run script and configuration.
* AWS scripts removed - the demo standup scripts must be migrated to an appropriate platform
* Java Client uses build script instead of `make` to build classes

### Bugfixes
* Client sends proper message to frontend (JSON key `msg` added)
* Added proper primary key defaults for python models (avoids warnings on startup)
* Added conditional Keychain library load in python (avoids warnings on migrations and other non-runserver commands)

### Features
* Frontend can properly pass through a message if it fails to decrypt
* Java Client now will wait 5s and expect user to press 'Enter' before sending a message (this can be removed in Java Client), letting you take time to study the outputs before they scroll off screen

### Quality of Life
* Removed unused subapplications and inconsistent documentation
* Much improved documentation to walk through installation, running, and troubleshooting
* Simplified all settings and configuration and put them in just 1-2 files for the applications
* Refactored code in Java Client to be easier for users to understand and extend for their own use cases
* Added more debug statements for readability
* All applications can stand up on **1 server**, making it easy to demo on a laptop or personal computer
* Added trusted directory suffix support to avoid collision with other demos
* Added default SSL support in Django to give users using SSL a head start on configuration