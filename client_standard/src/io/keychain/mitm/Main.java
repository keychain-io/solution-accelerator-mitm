package io.keychain.mitm;

import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
  
  private static final Logger LOGGER = Logger.getLogger(StandardClient.class.getName());
  private static final String deviceName = "ttoyosu1meter";

  public static void main(String[] args) {
    // Extract the target URL from command line
    final String url = args.length > 0 ? args[0] : null;

    if (url == null) {
        LOGGER.severe("Target URL must be provided as a command line argument");
        System.exit(1);
    }
    
    final String withKeychain = System.getProperty("with.keychain", null);
    if (withKeychain != null) {
        LOGGER.info("Client will be powered by Keychain");
    }

    Client client = withKeychain != null ? new KeychainClient(url, deviceName) : new StandardClient(url, deviceName);

    Scanner sc = new Scanner(System.in);
    while (true) {
        try {
            System.out.println("Type <ENTER> to send a reading, and Q<ENTER> to quit");
            String line = sc.nextLine();
            if (line.equals("Q")) {
                client.stop();
                break;
            }

            client.sendReading();
            // Thread.sleep(5000);
        } catch (Exception e) {
            LOGGER.severe("Exception while sending post: " + e.getMessage());
        }
    }
  }
}
