package com.kthisiscvpv;

import java.util.Scanner;

import org.json.JSONObject;

import com.kthisiscvpv.socket.client.Packet;
import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.socket.server.RLServer;

public class App {

	private static int DEFAULT_SERVER_PORT = 26388;

	public static void main(String[] args) throws Exception {
		int serverPort = DEFAULT_SERVER_PORT;

		// Check if the user provided a port
		if (args.length > 0)
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.err.printf("Error: %s is not a valid port number.\n", args[0]);
				System.exit(1);
			}

		// Start the socket server on the given port
		RLServer server = new RLServer(serverPort);
		new Thread(server).start();

		// Start debugging through system input
		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			String next = scanner.nextLine();
			int count = server.getConnectedClients().size();

			if (next.startsWith("--")) {
				System.out.printf("There %s currently %d individual%s connected.\n", count != 1 ? "are" : "is", count, count != 1 ? "s" : "");
			} else {
				JSONObject obj = new JSONObject();
				obj.put("header", Packet.MESSAGE);
				obj.put("message", next);
				String packet = obj.toString();
				for (RLClient client : server.getConnectedClients())
					if (client.getClientRoom() != null)
						client.sendPacket(packet);
				System.out.printf("Sent message to %d user%s: %s\n", count, count != 1 ? "s" : "", next);
			}
		}

		scanner.close();
	}

}
