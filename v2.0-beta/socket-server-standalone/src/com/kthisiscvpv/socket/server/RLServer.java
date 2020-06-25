package com.kthisiscvpv.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.MockLogger;
import com.kthisiscvpv.util.logger.MockLogger.Level;

public class RLServer implements Runnable {

	// This is the maximum length of the name of a room.
	public static int MAX_ROOM_NAME_LENGTH = 32;

	private AbstractLogger logger;
	private int port;

	// This contains a set of all the active connections on the server.
	private HashSet<RLClient> connectedClients;

	// This contains a mapping of all the rooms with the clients.
	private Map<String, HashSet<RLClient>> activeClients;

	/**
	 * Construct a new RuneLite socket server.
	 * @param port Valid port number (1 - 65565) to start the server on.
	 */
	public RLServer(int port) {
		this.logger = new MockLogger(System.out);
		this.port = port;

		this.connectedClients = new HashSet<RLClient>();
		this.activeClients = new HashMap<String, HashSet<RLClient>>();
	}

	/**
	 * Return the debug logger.
	 * @return The debug logger.
	 */
	public AbstractLogger getLogger() {
		return this.logger;
	}

	/**
	 * Retrieve the port number that the server was started on.
	 * @return The port number of the server.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Returns the list of all connected clients.
	 * @return A hashset of all connected clients.
	 */
	public HashSet<RLClient> getConnectedClients() {
		return this.connectedClients;
	}

	/**
	 * Return the room mapping of all connected clients with their respective rooms. A client could be connected and not be in any room yet.
	 * @return A map of all rooms and their connected clients.
	 */
	public Map<String, HashSet<RLClient>> getActiveClients() {
		return this.activeClients;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		this.logger.println(RLServer.class, Level.INFO, "Starting socket server on port " + this.port);

		new Thread(new RLServerDoctor(this)).start();
		this.logger.println(RLServer.class, Level.INFO, "Starting routine integrity checks");

		try (ServerSocket serverSocket = new ServerSocket(this.port)) {
			this.logger.println(RLServer.class, Level.INFO, "Done. Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");

			while (true) {
				this.logger.println(RLServer.class, Level.INFO, "Awaiting new client connection");

				try {
					Socket clientSocket = serverSocket.accept();
					startTime = System.currentTimeMillis();
					RLClient tunnel = new RLClient(this, clientSocket);
					new Thread(tunnel).start();
				} catch (Exception ex) {
					this.logger.println(RLServer.class, Level.ERROR, "An error occured while trying to accept a client connection");
					ex.printStackTrace();
				}

				this.logger.println(RLServer.class, Level.INFO, "New connection processed. Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
			}
		} catch (Exception e) {
			this.logger.println(RLServer.class, Level.FATAL, "Unable to start the socket server");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
