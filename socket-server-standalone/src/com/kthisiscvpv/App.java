package com.kthisiscvpv;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

import com.kthisiscvpv.socket.client.PacketType;
import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.socket.server.RLServer;

public class App {

	public static void main(String[] args) throws Exception {
		// Parse the arguments into a map.
		Map<String, String> arguments = new HashMap<String, String>();

		for (int i = 0; i < args.length; i++) {
			String var = args[i];
			if (!var.startsWith("--"))
				continue;

			var = var.substring(2);
			if (var.isEmpty())
				continue;

			String argument = "";

			for (int j = (i + 1); j < args.length; j++) {
				String key = args[j];
				if (key.isEmpty())
					continue;

				if (key.startsWith("--"))
					break;

				if (!argument.isEmpty())
					argument += " ";
				argument += key;
			}

			arguments.put(var, argument.isEmpty() ? null : argument);
		}

		// Start the socket server on the given arguments
		RLServer server = new RLServer(arguments);
		new Thread(server).start();

		// Start debugging through system input
		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			String next = scanner.nextLine();
			int count = server.getClients().size();

			if (next.startsWith("--")) {
				String message = next.substring(2).trim();
				if (message.isEmpty())
					continue;

				JSONObject obj = new JSONObject();
				obj.put("header", PacketType.MESSAGE);
				obj.put("message", message);

				String packet = obj.toString();
				for (RLClient client : server.getClients())
					if (client.getClientRoom() != null)
						client.sendPacket(packet);

				System.out.printf("Sent message to %d user%s: %s\n", count, count != 1 ? "s" : "", next);
			} else if (next.startsWith("list") || next.startsWith("ls")) {
				System.out.printf("There %s currently %d individual%s connected.\n", count != 1 ? "are" : "is", count, count != 1 ? "s" : "");
			}
		}

		scanner.close();
	}

}
