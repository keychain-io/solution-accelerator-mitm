package io.keychain.mitm;

import io.keychain.core.Gateway;
import io.keychain.core.SecurityLevel;
import io.keychain.core.Contact;
import io.keychain.core.Persona;
import io.keychain.core.Refreshable;
import io.keychain.mitm.util.PairHelper;
import io.keychain.mitm.util.DirectoryThread;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class KeychainClient extends StandardClient {

    static {
        // System.loadLibrary("c++_shared");
        System.loadLibrary("keychain");
        System.loadLibrary("keychain-jni");
    }

    private static final Logger LOGGER = Logger.getLogger(KeychainClient.class.getName());
    private Gateway gateway;
    private DirectoryThread directoryThread;

    public KeychainClient(String urlString, String deviceName) {
        super(urlString, deviceName);

        io.keychain.core.Context keychainContext;

        try {
            String keychainHome = System.getProperty("user.home") + "/.keychain/";
            String keychainDbFile = System.getProperty("keychain.db.file", keychainHome + "data/data.db");
            String keychainCfgFile = System.getProperty("keychain.cfg.file", keychainHome + "config/keychain.cfg");
            LOGGER.info("Initializing Keychain with DB File : " + keychainDbFile);
            LOGGER.info("Initializing Keychain with CFG File : " + keychainCfgFile);
            keychainContext = Gateway.initializeDb(keychainCfgFile, keychainDbFile, keychainHome + "data/drop_keychain.sql", keychainHome + "data/keychain.sql");

            if (!keychainContext.isNull()) {
                LOGGER.info("DB init succeeded");
            } else {
                LOGGER.warning("DB init failed");
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred: ", e);
            return;
        }

        //gateway = new Gateway(keychainContext);
        gateway = new Gateway(keychainContext, new Refreshable() {
            public void onRefresh() { }
        });
        gateway.seed();

        gateway.onStart();
        gateway.onResume();

        try {
            if (!gateway.maturePersonaExists()) {
                Persona persona = gateway.createPersona(deviceName, "mitm", SecurityLevel.MEDIUM);
            }

            while (!gateway.maturePersonaExists()) {
                try {
                    LOGGER.info("Sleeping until mature persona exists");
                    Thread.sleep(11000);
                } catch (Exception e) {
                    LOGGER.severe("Exception waiting for mature persona: " + e.getMessage());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            LOGGER.info("My URI: " + gateway.getActivePersona().getUri().toString());
        } catch (Exception e) {
            LOGGER.severe("Error printing persona URI: " + e.getMessage());
        }
        LOGGER.info("Mature persona exists, synchronizing with trusted directories");
        final String tdSuffix = System.getProperty("keychain.trusted.directory.suffix", "");
        final String tdHost = System.getProperty("keychain.trusted.directory.host", "54.65.160.194");
        final Integer tdPort = Integer.valueOf(System.getProperty("keychain.trusted.directory.port", "3301"));
        LOGGER.info("Suffix is " + tdSuffix);

        final PairHelper pairHelperHacker = new PairHelper(tdHost, tdPort, "mitm-hacker-front" + tdSuffix);
        final PairHelper pairHelperLegit = new PairHelper(tdHost, tdPort, "mitm-legit" + tdSuffix);

        directoryThread = new DirectoryThread(gateway, pairHelperHacker, pairHelperLegit);
        Thread dtt = new Thread(directoryThread);
        dtt.start();
    }

    @Override
    public void stop() {
        directoryThread.onStop();
        gateway.onPause();
        gateway.onStop();
    }

    @Override
    public String makeReadingMessage() {
        final String msg = super.makeReadingMessage();
        ArrayList<Contact> contacts = gateway.getContacts();
        String encryptedMsg;
        try {
            encryptedMsg = gateway.signThenEncrypt(contacts, msg.getBytes("UTF-16"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.severe("Error encoding message: " + e.getMessage());
            return msg;
        }
        return encryptedMsg;
    }
}
