 package io.keychain.mitm.util;

import io.keychain.core.*;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

 /**
  * Utility class that can be put on a thread to continuously run in the background and synchronize contacts and
  * own persona to/from the trusted directories passed to it.
  *
  * On construction pass all needed PairHelper instances (each PairHelper represents 1 domain)
  */
 public class DirectoryThread implements Runnable {
    private volatile boolean shouldStop;
    private Gateway gateway;
    private static final String TAG = "DirectoryThread";
    private static final Logger LOGGER = Logger.getLogger(DirectoryThread.class.getName());

    static ReentrantLock mutex = new ReentrantLock();

    private List<PairHelper> pairHelpers;

    public DirectoryThread(Gateway gateway, PairHelper... pairHelpers) {
        this.gateway = gateway;
        this.pairHelpers = new ArrayList<PairHelper>(pairHelpers.length);
        for (PairHelper ph : pairHelpers) this.pairHelpers.add(ph);
    }

    private void doContactSync(PairHelper pairHelper) {
        final String DOMAIN = pairHelper.getDomain();
        LOGGER.info("[" + TAG + "] [" + DOMAIN + "] In doContactSync");
        try {
            LOGGER.info("[" + TAG + "] [" + DOMAIN + "] Contacts starts @ " + this.gateway.getContacts().size());
            List<Uri> serverUris = pairHelper.getAllUri();
            Persona persona = this.gateway.getActivePersona();
            List<Contact> contacts = this.gateway.getContacts();
            List<Persona> personas = this.gateway.getPersonas();
            Uri personaUri = persona.getUri();

            boolean selfFound = false;

            List<Uri> myUris = new ArrayList<>(contacts.size() + personas.size());
            for (Contact c : contacts) myUris.add(c.getUri());
            for (Persona p : personas) myUris.add(p.getUri());

            // for each server URI, check if it is contact/self, and add if not; at the end, if self wasn't found, upload
            for (Uri uri : serverUris) {
                boolean shouldAdd = true;
                if (uri.toString().equals(personaUri.toString())) {
                    selfFound = true;
                    continue;
                }
                for (Uri myUri : myUris) {
                    if (uri.toString().equals(myUri.toString())) {
                        shouldAdd = false;
                        break;
                    }
                }
                if (shouldAdd) {
                    // add contact
                    LOGGER.info("[" + TAG + "] [" + DOMAIN + "] Adding contact for URI: " + uri.toString());
                    Contact c = this.gateway.createContact(uri.toString().substring(0, 16), pairHelper.getDomain(), new Uri(uri.toString()));
                    LOGGER.info("[" + TAG + "] [" + DOMAIN + "] Contacts now @ " + this.gateway.getContacts().size());
                    LOGGER.info("[" + TAG + "] [" + DOMAIN + "] C is " + (c == null ? "null" : c.getUri().toString()));
                }
            }
            if (!selfFound) {
                // add self
                LOGGER.info("[" + TAG + "] [" + DOMAIN + "] Adding persona to Trusted Directory: " + personaUri.toString());
                pairHelper.uploadUri(personaUri);
            }
        } catch (Exception e) {
            LOGGER.severe("[" + TAG + "] [" + DOMAIN + "] Unhandled exception bubbled from doContactSync: " + e.getMessage());
        }
    }

    public void run() {
        this.shouldStop = false;
        LOGGER.info("[" + TAG + "] " + "Directory thread Running");
        while (!this.shouldStop) {
            for (final PairHelper pairHelper : this.pairHelpers) {
                LOGGER.info("[" + TAG + "] " + "Synchronizing with PairHelper " + pairHelper.getDomain());
                // lock access to doContactSync
                mutex.lock();
                doContactSync(pairHelper);
                mutex.unlock();
            }

            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
                LOGGER.severe("[" + TAG + "] Exception in sleep: " + e.getMessage());
            }
        }
        LOGGER.info("[" + TAG + "] Exiting");
    }

    public void onStop() {
        this.shouldStop = true;
    }
}
