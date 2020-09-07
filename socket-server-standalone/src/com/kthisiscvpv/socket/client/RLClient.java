package com.kthisiscvpv.socket.client;

import static com.kthisiscvpv.socket.server.RLConst.MAX_ROOM_NAME_LENGTH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kthisiscvpv.socket.server.RLServer;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.AbstractLogger.Level;

public class RLClient implements Runnable {

	private RLServer server;
	private AbstractLogger logger;

	private Socket socket;

	private InputStreamReader isr;
	private BufferedReader br;
	private PrintWriter pw;

	private String ipAddress;
	private String clientRoom;
	private String clientName;

	private boolean isTerminated;

	private long lastMessageEpoch;

	private HashSet<RLClient> party;

	public RLClient(RLServer server, Socket socket) {
		this.server = server;
		this.logger = server.getLogger();

		this.socket = socket;

		this.lastMessageEpoch = System.currentTimeMillis();

		this.ipAddress = socket.getInetAddress().getHostAddress();
		this.isTerminated = false;
	}

	@Override
	public void run() {
		try {
			logger.println(RLClient.class, Level.SYSTEM, "Establishing connection with " + ipAddress);
			long startTime = System.currentTimeMillis();

			// Add the client to the list of connected clients on the server.
			HashSet<RLClient> clients = server.getClients();
			synchronized (clients) {
				clients.add(this);
			}

			Map<String, Integer> activeIPCount = server.getClientAddresses();
			int ipCount = 1;
			synchronized (activeIPCount) {
				if (activeIPCount.containsKey(ipAddress))
					ipCount += activeIPCount.get(ipAddress);
				activeIPCount.put(ipAddress, ipCount);
			}

			// Define connection stream variables.
			isr = new InputStreamReader(socket.getInputStream());
			br = new BufferedReader(isr);
			pw = new PrintWriter(socket.getOutputStream(), true);

			// Perform IP address integrity check.
			if (server.getIPLimit() > 0) {
				if (ipCount > server.getIPLimit()) {
					logger.println(RLClient.class, Level.WARN, "Terminating connection with " + ipAddress + " as they have exceeded the IP address limit (" + ipCount + " > " + server.getIPLimit() + ")");
					sendMessage("<col=b4281e>Connection count exceeded. Maximum of " + server.getIPLimit() + " connections allowed per IP address.");
					Thread.sleep(2000L);
					terminate();
					return;
				}
			}

			while (true) {
				if (isTerminated || !socket.isConnected() || socket.isClosed())
					break;

				if (pw.checkError())
					throw new IOException("Broken transmission stream detected");

				String packet = br.readLine();
				if (packet == null)
					break;

				lastMessageEpoch = System.currentTimeMillis();

				if (packet.isEmpty())
					continue;

				long packetParseTime = System.currentTimeMillis();

				logger.println(RLClient.class, Level.INFO, "Recieved packet from " + ipAddress + " (length=" + packet.length() + ")");

				JSONObject data = new JSONObject(packet);

				if (!data.has("header"))
					throw new NullPointerException(String.format("Packet from %s has no header", ipAddress));

				String header = data.getString("header");

				if (header.equals(PacketType.JOIN)) {
					clientRoom = data.getString("room"); // SHA-256 encrypted room password.
					if (clientRoom.length() > MAX_ROOM_NAME_LENGTH)
						clientRoom = clientRoom.substring(0, MAX_ROOM_NAME_LENGTH);

					clientName = data.getString("name"); // AES-256 encrypted username.

					// Integrity check, to make sure no clients with the same name exists.
					List<RLClient> toDisconnect = new ArrayList<RLClient>();

					synchronized (clients) {
						for (RLClient client : clients) {
							if (client == null || client == this)
								continue;

							String name = client.getClientName();
							if (name != null && name.equals(clientName))
								toDisconnect.add(client);
						}
					}

					for (RLClient client : toDisconnect)
						client.terminate();

					// Add the client to the same server room.
					Map<String, HashSet<RLClient>> rooms = server.getRooms();
					ArrayList<RLClient> clientParty;

					synchronized (rooms) {
						party = rooms.get(clientRoom);
						if (party == null)
							party = new HashSet<RLClient>();

						party.add(this);
						rooms.put(clientRoom, party);
						clientParty = new ArrayList<RLClient>(party);
					}

					// Perform party size integrity check.
					int partySize = clientParty.size();
					if (server.getRoomLimit() > 0) {
						if (partySize > server.getRoomLimit()) {
							logger.println(RLClient.class, Level.WARN, "Terminating connection with " + ipAddress + " as they have exceeded the party size limit (" + partySize + " > " + server.getRoomLimit() + ")");
							sendMessage("<col=b4281e>Room size exceeded. Maximum of " + server.getRoomLimit() + " connections allowed per room.");
							Thread.sleep(2000L);
							terminate();
							return;
						}
					}

					logger.println(RLClient.class, Level.SYSTEM, "Identified " + ipAddress + ". Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");

					// Send the updated party list to all members.
					JSONObject response = new JSONObject();
					response.put("header", PacketType.JOIN);

					JSONArray party = new JSONArray();
					for (RLClient client : clientParty)
						party.put(client.getClientName());
					response.put("party", party);

					response.put("player", clientName);

					sendPacketToRoom(response.toString());

					logger.println(RLClient.class, Level.INFO, "Updated party list has been sent to members of " + clientRoom);
				} else if (party == null) {
					logger.println(RLClient.class, Level.INFO, "Ignoring " + header + " packet from " + ipAddress + " as they have not joined a session yet");
					continue;

				} else if (header.equals(PacketType.BROADCAST)) {
					sendPacketToRoom(packet); // Sends the same packet, as is.
					logger.println(RLClient.class, Level.INFO, "Packet broadcasted. Elapsed time: " + (System.currentTimeMillis() - packetParseTime) + "ms");

				} else if (header.equals(PacketType.PING)) {
					sendPacket(packet); // Sends the same packet, as is.
					logger.println(RLClient.class, Level.INFO, ipAddress + " just pinged the server");

				}
			}
		} catch (Exception ex) {
			logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		terminate();
	}

	public void terminate() {
		if (isTerminated)
			return;

		isTerminated = true;

		// Close all socket streams.
		try {
			if (!socket.isClosed()) {
				pw.flush();				
				pw.close();

				br.close();
				isr.close();
				
				socket.close();
			}
		} catch (Exception ex) {}

		// Remove the client from the client list.
		HashSet<RLClient> clients = server.getClients();
		synchronized (clients) {
			try {
				if (clients.contains(this)) {
					clients.remove(this);

					// Remove from the IP count.
					Map<String, Integer> clientAddresses = server.getClientAddresses();
					synchronized (clientAddresses) {
						if (clientAddresses.containsKey(ipAddress)) {
							int count = clientAddresses.get(ipAddress) - 1;
							if (count > 0)
								clientAddresses.put(ipAddress, count);
							else
								clientAddresses.remove(ipAddress);
						}
					}
				}
			} catch (Exception ignorred) {}
		}

		// Remove the client from their party list.
		if (party != null) {
			synchronized (party) {
				party.remove(this);
			}

			if (party.isEmpty()) {
				Map<String, HashSet<RLClient>> rooms = server.getRooms();
				synchronized (rooms) {
					rooms.remove(clientRoom);
				}

				logger.println(RLClient.class, Level.INFO, "Party room is now empty and '" + clientRoom + "' has been removed");

			} else {
				// Sends the updated party to all members.
				JSONObject response = new JSONObject();
				response.put("header", PacketType.LEAVE);

				synchronized (party) {
					JSONArray partyArray = new JSONArray();
					for (RLClient client : party)
						partyArray.put(client.getClientName());
					response.put("party", partyArray);
				}

				response.put("player", clientName);

				sendPacketToRoom(response.toString());
			}
		}

		logger.println(RLClient.class, Level.INFO, "Connection with " + ipAddress + " has been terminated");
	}

	public void sendMessage(String message) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("header", PacketType.MESSAGE);
			obj.put("message", message);
			sendPacket(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(String packet) {
		try {
			pw.println(packet);
		} catch (Exception ex) {
			logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			terminate();
		}
	}

	public void sendPacketToRoom(String packet) {
		try {
			if (party == null)
				throw new IllegalAccessError(ipAddress + " tried to broadcast before joining a room");

			List<RLClient> clonedList;

			synchronized (party) {
				clonedList = new ArrayList<RLClient>(party);
			}

			for (RLClient client : clonedList)
				client.sendPacket(packet);

			int count = clonedList.size();
			logger.println(RLClient.class, Level.INFO, String.format("%s broadcasted a packet to %d total member%s", ipAddress, count, count != 1 ? "s" : ""));
		} catch (Exception ex) {
			logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			terminate();
		}
	}

	public String getIPAddress() {
		return ipAddress;
	}

	public String getClientRoom() {
		return clientRoom;
	}

	public String getClientName() {
		return clientName;
	}

	public boolean isTerminated() {
		return isTerminated;
	}

	public long getLastMessageEpoch() {
		return lastMessageEpoch;
	}

	public HashSet<RLClient> getParty() {
		return party;
	}
}
