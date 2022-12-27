# Pre-requisites

At a minimum, the following is required to run the backend server:

* Python 3
* Django 3.2.x

Here is an example of what you might use to install these on your system, if they are not there already.
```bash
sudo apt-get install python3.7
sudo apt install python3-pip
python3 -m pip install Django==3.2.5
```

**Note:** Your python command may be different (e.g. `python` or `/path/to/python3`) depending on your system, installation and path.  Please use the appropriate command.

To run the backend server with Keychain enabled, you must also have Keychain and Keychain Python libraries installed.  See `README.md` for information on how to reference it during run.


# Setting up the Backend

If everything is installed, then setup should be a snap.

We need to create the database, migrate the SQL, add a few environment variables, and then we're done.

You can do it manually if you export `LD_LIBRARY_PATH` and `PYTHONPATH` so your Keychain installation is pointed to, then run the right migration commands.

A shortcut is the `run-command.sh` script, which we use below.  It wraps the command so the variables are set properly.

```bash
scripts/run-command.sh makemigrations backend
scripts/run-command.sh migrate
```
