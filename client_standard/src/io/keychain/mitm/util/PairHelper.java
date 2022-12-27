package io.keychain.mitm.util;

import io.keychain.core.Uri;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

/**
 * A sample utility for pairing URIs from a trusted directory.
 * Only supports HTTP and a particular trusted directory endpoint (hardcoded), as well as methods.
 * However, this should get users started on the idea.
 *
 * It can be used to get or upload URIs to the trusted directory under a specific "domain" (endpoint)
 */
public class PairHelper {
    private final String TAG;
    private static final Logger LOGGER = Logger.getLogger(DirectoryThread.class.getName());
    private final String primaryHost;
    private final int primaryPort;
    private final String domain;

    private final HttpClient client;
    private final String baseUri;

    public PairHelper(String primaryHost, int primaryPort, String domain) {
        this.primaryHost = primaryHost;
        this.primaryPort = primaryPort;
        this.domain = domain;
        this.TAG = "PairHelper " + domain;

        this.client = HttpClient.newBuilder().build();
        this.baseUri = "http://" + this.primaryHost + ":" + this.primaryPort + "/adsimulator/";
    }

    public String getDomain() {
        return this.domain;
    }

    public void uploadUri(Uri paramUri) throws IllegalArgumentException {
        LOGGER.info("[" + TAG + "] " + "Uploading " + paramUri.toString());

        // TODO: this is TEMPORARY until java Uri has methods to display txid and vout for encr and sign
        final String[] parts = paramUri.toString().split("[;:]");
        if (parts == null || parts.length != 4) {
            throw new IllegalArgumentException("Param URI is invalid");
        }

        final String eot = parts[0];
        final String eov = parts[1];
        final String sot = parts[2];
        final String sov = parts[3];
        final URI uri = URI.create(this.baseUri + "uploaduri/" + this.domain + "/" + eot + "/" + eov + "/" + sot + "/" + sov);
        final HttpRequest request = HttpRequest.newBuilder(uri).build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("[" + TAG + "] " + "Unable to send request to TD Server");
            return;
        }

        if (response == null || response.statusCode() != 200) {
            LOGGER.severe("[" + TAG + "] " + "TD Server returned code " + response.statusCode());
            return;
        }

        final String body = response.body();
        JSONObject obj = new JSONObject(body);
        final String responseCode = obj.getString("response_code");
        if (!"OK".equals(responseCode)) {
            LOGGER.severe("[" + TAG + "] " + "TD Server returned response code : " + responseCode);
            return;
        }
    }

    public List<Uri> getAllUri() {
        LOGGER.info("[" + TAG + "] " + "Retrieving all URIs");
        final List<Uri> emptyList = Collections.emptyList();

        final URI uri = URI.create(this.baseUri + "getalluri/" + this.domain);
        final HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("[" + TAG + "] " + "Unable to send request to TD Server");
            return emptyList;
        }

        if (response == null || response.statusCode() != 200) {
            LOGGER.severe("[" + TAG + "] " + "TD Server returned code " + response.statusCode());
            return emptyList;
        }

        final String body = response.body();
        JSONObject obj = new JSONObject(body);
        final String responseCode = obj.getString("response_code");
        if (!"OK".equals(responseCode)) {
            LOGGER.severe("[" + TAG + "] " + "TD Server returned response code : " + responseCode);
            return emptyList;
        }

        JSONArray results = obj.getJSONArray("results");
        final List<Uri> uris = new ArrayList<Uri>(results.length());
        LOGGER.info("[" + TAG + "] " + "  > Retrieved " + results.length() + " URIs");

        for (int i = 0; i < results.length(); i++) {
            final JSONObject o = results.getJSONObject(i);
            final Uri contactUri = new Uri(o.getString("encr_txid") + ":" + o.getInt("encr_vout") + ";" +
                    o.getString("sign_txid") + ":" + o.getInt("sign_vout"));
            uris.add(contactUri);

            // LOGGER.fine("[" + TAG + "]   > made URI " + contactUri.toString());
        }
        return uris;
    }

    public void clearAllUri() {
        LOGGER.info("[" + TAG + "] " + "Clearing all URIs on TD server");
        final URI uri = URI.create(this.baseUri + "clearalluri/" + this.domain);
        final HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("[" + TAG + "] " + "Unable to send request to TD Server");
            return;
        }

        if (response == null || response.statusCode() != 200) {
            LOGGER.severe("[" + TAG + "] " + "TD Server returned code " + response.statusCode());
            return;
        }

        final String body = response.body();
        JSONObject obj = new JSONObject(body);
        final String responseCode = obj.getString("response_code");
        if (!"OK".equals(responseCode)) {
            LOGGER.severe("[" + TAG + "] " + "TD Server did not clear all URI : " + responseCode);
            return;
        }
    }
}