package io.keychain.mitm;

import java.util.ArrayList;
import java.util.logging.Logger;

public class StandardClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    protected StandardClient(String urlString, String deviceName) {
        super(urlString, deviceName);
    }

    @Override
    protected String makeReadingMessage() {
        ArrayList<Reading> readings = new ArrayList<Reading>();
        readings.add(new Reading(false, 10.0));
        readings.add(new Reading(true, 12.3));

        String msg = JsonUtils.dataToString(deviceName, readings);
        LOGGER.info("JSON Message: " + msg);
        return msg;
    }
}
