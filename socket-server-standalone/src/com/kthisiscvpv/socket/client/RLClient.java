package com.kthisiscvpv.socket.client;

import static com.kthisiscvpv.socket.server.RLServer.MAX_ROOM_NAME_LENGTH;

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

	// Server related variables.
	private RLServer server;
	private AbstractLogger logger;
	private HashSet<RLClient> party;

	// Socket related variables.
	private Socket socket;

	private BufferedReader inputStream;
	private PrintWriter outputStream;

	// The last UNIX Epoch time that the client sent a heartbeat to the server.
	private long lastHeartbeat;

	// This represents the IP address of the client.
	private String address;

	// The room that the client is connected to.
	private String clientRoom;

	// The encrypted player name that denotes the client. The server does not have the password to decrypt this name.
	private String clientName;

	// Variable that checks whether or not terminate() has been called.
	private boolean isTerminated;

	// Last heartbeat time, decides when to send.
	private long heartbeatTime;

	/**
	 * Identify a new client connection.
	 * @param server The server that accepted the connection.
	 * @param socket The socket of the connection's traffic.
	 */
	public RLClient(RLServer server, Socket socket) {
		this.server = server;
		this.logger = this.server.getLogger();

		this.socket = socket;

		this.lastHeartbeat = System.currentTimeMillis();
		this.heartbeatTime = 0L;
		this.address = this.socket.getInetAddress().getHostAddress();

		this.isTerminated = false;
	}

	@Override
	public void run() {
		try {
			this.logger.println(RLClient.class, Level.INFO, "Establishing connection with " + this.address);
			long startTime = System.currentTimeMillis();

			// Add to the list of connected clients.
			HashSet<RLClient> allClients = this.server.getConnectedClients();
			synchronized (allClients) {
				allClients.add(this);
			}

			// Add to the IP count.
			Map<String, Integer> activeIPCount = this.server.getActiveCount();
			int count = 1;
			synchronized (activeIPCount) {
				if (activeIPCount.containsKey(this.address))
					count += activeIPCount.get(this.address);
				activeIPCount.put(this.address, count);
			}

			this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.outputStream = new PrintWriter(this.socket.getOutputStream(), true);

			if (this.server.getIPLimit() > 0) {
				if (count > this.server.getIPLimit()) {
					this.logger.println(RLClient.class, Level.INFO, "Terminating connection with " + this.address + " as they have exceeded the IP address limit (" + count + " > " + this.server.getIPLimit() + ")");
					this.errorAndTerminate("<col=b4281e>Connection count exceeded. Maximum of " + this.server.getIPLimit() + " allowed per IP address. Consider hosting your own server to bypass this limit.");
					this.logger.println(RLClient.class, Level.INFO, "Disconnection error message sent to " + this.address);
					return;
				}
			}

			while (true) {
				if (this.isTerminated || !this.socket.isConnected() || this.socket.isClosed())
					break;

				if (this.outputStream.checkError())
					throw new IOException("Broken transmission stream detected");

				if (!this.inputStream.ready()) {
					long elapsedHeartbeat = System.currentTimeMillis() - this.heartbeatTime;
					if (elapsedHeartbeat >= 1000L) {
						this.outputStream.println();
						this.heartbeatTime = System.currentTimeMillis();
					}

					Thread.sleep(5L);
					continue;
				}

				String packet = this.inputStream.readLine();
				if (packet == null)
					continue;

				if (packet.isEmpty()) {
					this.lastHeartbeat = System.currentTimeMillis();
					continue;
				}

				long packetParseTime = System.currentTimeMillis();

				if (this.server.isVerbose())
					this.logger.println(RLClient.class, Level.INFO, "Recieved packet from " + this.address + ": " + packet);
				else
					this.logger.println(RLClient.class, Level.INFO, "Recieved packet from " + this.address + " (length=" + packet.length() + ")");

				JSONObject data = new JSONObject(packet);

				if (!data.has("header"))
					throw new NullPointerException(String.format("Packet from %s has no header", this.address));

				String header = data.getString("header");

				if (header.equals(Packet.JOIN)) {
					this.clientRoom = data.getString("room"); // SHA-256 encrypted room password.
					if (this.clientRoom.length() > MAX_ROOM_NAME_LENGTH)
						this.clientRoom = this.clientRoom.substring(0, MAX_ROOM_NAME_LENGTH);

					this.clientName = data.getString("name"); // AES-256 encrypted username.

					Map<String, HashSet<RLClient>> activeClients = this.server.getActiveClients();
					ArrayList<RLClient> clientsClone;

					synchronized (activeClients) {
						this.party = activeClients.get(this.clientRoom);
						if (this.party == null)
							this.party = new HashSet<RLClient>();

						this.party.add(this);
						activeClients.put(this.clientRoom, this.party);
						clientsClone = new ArrayList<RLClient>(this.party);
					}

					this.lastHeartbeat = System.currentTimeMillis();
					this.logger.println(RLClient.class, Level.INFO, "Identified " + this.address + ". Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");

					JSONObject response = new JSONObject(); // Sends the updated party to all members.
					response.put("header", Packet.JOIN);

					JSONArray party = new JSONArray();
					for (RLClient client : clientsClone)
						party.put(client.clientName);
					response.put("party", party);

					response.put("player", this.clientName);

					this.sendPacketToRoom(response.toString());

					this.lastHeartbeat = System.currentTimeMillis();
					this.logger.println(RLClient.class, Level.INFO, "Updated party list has been sent to members of " + this.clientRoom);

				} else if (this.party == null) {
					this.logger.println(RLClient.class, Level.WARN, "Ignoring " + header + " packet from " + this.address + " as they have not joined a session yet");
					continue;

				} else if (header.equals(Packet.BROADCAST)) {
					this.sendPacketToRoom(packet); // Sends the same packet, as is.
					this.logger.println(RLClient.class, Level.INFO, "Packet broadcasted. Elapsed time: " + (System.currentTimeMillis() - packetParseTime) + "ms");

				} else if (header.equals(Packet.PING)) {
					this.lastHeartbeat = System.currentTimeMillis();
					this.sendPacket(packet); // Sends the same packet, as is.
					this.logger.println(RLClient.class, Level.INFO, this.address + " just pinged the server");

				}
			}
		} catch (Exception ex) {
			this.logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		this.terminate();
	}

	public void errorAndTerminate(String message) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("header", Packet.MESSAGE);
			obj.put("message", message);
			String packet = obj.toString();
			this.sendPacket(packet);

			Thread.sleep(5000L);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.terminate();
		}
	}

	/**
	 * Close all connections in this socket.
	 */
	public void terminate() {
		if (this.isTerminated)
			return;

		this.isTerminated = true;

		HashSet<RLClient> allClients = this.server.getConnectedClients();
		synchronized (allClients) {
			try {
				if (allClients.contains(this)) {
					allClients.remove(this);

					// Remove from the IP count.
					Map<String, Integer> activeIPCount = this.server.getActiveCount();
					synchronized (activeIPCount) {
						if (activeIPCount.containsKey(this.address)) {
							int count = activeIPCount.get(this.address) - 1;
							if (count > 0)
								activeIPCount.put(this.address, count);
							else
								activeIPCount.remove(this.address);
						}
					}
				}
			} catch (Exception ignorred) {}
		}

		if (this.party != null) {
			synchronized (this.party) {
				this.party.remove(this);
			}

			if (this.party.isEmpty()) {
				Map<String, HashSet<RLClient>> activeClients = this.server.getActiveClients();
				synchronized (activeClients) {
					activeClients.remove(this.clientRoom);
				}

				this.logger.println(RLClient.class, Level.INFO, "Party room is now empty. " + this.clientRoom + " has been removed");
			} else {
				JSONObject response = new JSONObject(); // Sends the updated party to all members.
				response.put("header", Packet.LEAVE);

				synchronized (this.party) {
					JSONArray party = new JSONArray();
					for (RLClient client : this.party)
						party.put(client.clientName);
					response.put("party", party);
				}

				response.put("player", this.clientName);

				this.sendPacketToRoom(response.toString());
			}
		}

		try {
			if (this.inputStream != null)
				this.inputStream.close();
		} catch (Exception ignorred) {}

		try {
			if (this.outputStream != null)
				this.outputStream.close();
		} catch (Exception ignorred) {}

		try {
			if (this.socket != null) {
				this.socket.shutdownInput();
				this.socket.shutdownOutput();
				this.socket.close();
			}
		} catch (Exception ignorred) {}

		this.logger.println(RLClient.class, Level.INFO, "Connection with " + this.address + " has been terminated");
	}

	/**
	 * Attempts to send a packet to the client.
	 * @param packet The packet to send.
	 */
	public void sendPacket(String packet) {
		try {
			this.outputStream.println(packet);
		} catch (Exception ex) {
			this.logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			this.terminate();
		}
	}

	/**
	 * Attempts to send a packet to every member in a room.
	 * @param packet The packet to send.
	 */
	public void sendPacketToRoom(String packet) {
		try {
			if (this.party == null)
				throw new IllegalAccessError(this.address + " tried to broadcast before joining a room");

			List<RLClient> clonedList;

			synchronized (this.party) {
				clonedList = new ArrayList<RLClient>(this.party);
			}

			for (RLClient client : clonedList)
				client.sendPacket(packet);

			int count = clonedList.size();
			this.logger.println(RLClient.class, Level.INFO, String.format("%s broadcasted a packet to %d total member%s", this.address, count, count != 1 ? "s" : ""));
		} catch (Exception ex) {
			this.logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			this.terminate();
		}
	}

	/**
	 * Return the last time a heartbeat was sent.
	 * @return The last heartbeat in UNIX epoch.
	 */
	public long getLastHeartbeat() {
		return this.lastHeartbeat;
	}

	/**
	 * Returns the IP Address that this client is connected from.
	 * @return The client's IP address.
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Returns the room that the client is in.
	 * @return The client's room name.
	 */
	public String getClientRoom() {
		return this.clientRoom;
	}
}
