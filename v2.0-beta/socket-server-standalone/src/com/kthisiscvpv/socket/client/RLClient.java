package com.kthisiscvpv.socket.client;

import static com.kthisiscvpv.socket.server.RLServer.MAX_ROOM_NAME_LENGTH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kthisiscvpv.socket.server.RLServer;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.MockLogger.Level;

public class RLClient implements Runnable {

	// Server related variables.
	private RLServer server;
	private AbstractLogger logger;

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

			this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.outputStream = new PrintWriter(this.socket.getOutputStream(), true);

			while (true) {
				if (this.isTerminated || !this.socket.isConnected() || this.socket.isClosed())
					break;

				if (this.outputStream.checkError())
					throw new IOException("Broken transmission stream detected");

				if (!this.inputStream.ready()) {
					this.outputStream.println();
					Thread.sleep(200L);
					continue;
				}

				String packet = this.inputStream.readLine();
				if (packet == null || packet.isEmpty())
					continue;

				this.logger.println(RLClient.class, Level.INFO, "Recieved packet from " + this.address + ": " + packet);

				String datagram = new String(Base64.getDecoder().decode(packet));
				JSONObject data = new JSONObject(datagram);

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
						HashSet<RLClient> roomClients = activeClients.get(this.clientRoom);
						if (roomClients == null)
							roomClients = new HashSet<RLClient>();

						roomClients.add(this);
						activeClients.put(this.clientRoom, roomClients);
						clientsClone = new ArrayList<RLClient>(roomClients);
					}

					this.lastHeartbeat = System.currentTimeMillis();
					this.logger.println(RLClient.class, Level.INFO, "Identified " + this.address + ". Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");

					JSONObject response = new JSONObject(); // Sends the updated party to all members.
					response.put("header", Packet.JOIN);

					JSONArray party = new JSONArray();
					for (RLClient client : clientsClone)
						party.put(client.clientName);
					response.put("party", party);

					this.sendPacketToRoom(Base64.getEncoder().encodeToString(response.toString().getBytes()));

					this.lastHeartbeat = System.currentTimeMillis();
					this.logger.println(RLClient.class, Level.INFO, "Updated party list has been sent to members of " + this.clientRoom);

				} else if (this.clientName == null || this.clientName == null) {
					this.logger.println(RLClient.class, Level.WARN, "Ignoring " + header + " packet from " + this.address + " as they have not joined a session yet");
					continue;

				} else if (header.equals(Packet.PING)) {
					this.lastHeartbeat = System.currentTimeMillis();
					this.sendPacket(packet); // Sends the same packet, as is.
					this.logger.println(RLClient.class, Level.INFO, this.address + " just pinged the server");

				} else if (header.equals(Packet.BROADCAST)) {
					this.sendPacketToRoom(packet); // Sends the same packet, as is.

				}
			}
		} catch (Exception ex) {
			this.logger.println(RLClient.class, Level.ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		this.terminate();
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
				allClients.remove(this);
			} catch (Exception ignorred) {}
		}

		Map<String, HashSet<RLClient>> activeClients = this.server.getActiveClients();
		ArrayList<RLClient> clientsClone = null;

		synchronized (activeClients) {
			try {
				HashSet<RLClient> roomClients = activeClients.get(this.clientRoom);
				if (roomClients != null) {
					roomClients.remove(this);
					if (roomClients.isEmpty())
						activeClients.remove(this.clientRoom);
					else
						clientsClone = new ArrayList<RLClient>(roomClients);
				}
			} catch (Exception ignorred) {}
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

		if (clientsClone != null) {
			JSONObject response = new JSONObject(); // Sends the updated party to all members.
			response.put("header", Packet.LEAVE);

			JSONArray party = new JSONArray();
			for (RLClient client : clientsClone)
				party.put(client.clientName);
			response.put("party", party);

			this.sendPacketToRoom(Base64.getEncoder().encodeToString(response.toString().getBytes()));
		}

		this.logger.println(RLClient.class, Level.INFO, "Connection with " + this.address + " has been terminated");
	}

	/**
	 * Attempts to send a packet to the client.
	 * @param packet The packet to send.
	 */
	public synchronized void sendPacket(String packet) {
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
			if (this.clientName == null || this.clientRoom == null)
				throw new IllegalAccessError(this.address + " tried to broadcast before joining a room");

			Map<String, HashSet<RLClient>> activeClients = this.server.getActiveClients();
			List<RLClient> clonedList;

			synchronized (activeClients) {
				HashSet<RLClient> clients = activeClients.get(this.clientRoom);
				if (clients == null)
					throw new NullPointerException(this.address + " is in " + this.clientRoom + ", a room that no longer exists");
				clonedList = new ArrayList<RLClient>(clients);
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
}
