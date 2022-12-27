package io.keychain.mitm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public abstract class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    final String urlString;
    final String deviceName;

    protected Client(String urlString, String deviceName) {
        this.urlString = urlString;
        this.deviceName = deviceName;
    }

    // POST a reading and return the response
    public void sendReading() {
        final String msg = makeReadingMessage();
        LOGGER.info("Sending Message: " + msg + "\n");
        String response = executePost(msg);
        LOGGER.info("Response: " + response);
    }

    public void stop() {}


    // Implementations should return a suitable string
    protected abstract String makeReadingMessage();


    protected String executePost(String urlParameters) {
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
                response.append('\n');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            LOGGER.severe("Exception trying to POST to frontend: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
