package com.kthisiscvpv.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.AbstractLogger.Level;
import com.kthisiscvpv.util.logger.MockFileLogger;
import com.kthisiscvpv.util.logger.MockLogger;

public class RLServer implements Runnable {

	// Default server port number.
	private static int DEFAULT_SERVER_PORT = 26388;

	// This is the maximum length of the name of a room.
	public static int MAX_ROOM_NAME_LENGTH = 64;

	private Map<String, String> arguments;

	private AbstractLogger logger;
	private int port = DEFAULT_SERVER_PORT;

	private boolean isVerbose;

	private int roomLimit = -1;
	private int ipLimit = -1;

	public int getRoomLimit() {
		return this.roomLimit;
	}

	public int getIPLimit() {
		return this.ipLimit;
	}

	// This contains a set of all the active connections on the server.
	private HashSet<RLClient> connectedClients;

	// This contains a mapping of all the rooms with the clients.
	private Map<String, HashSet<RLClient>> activeClients;

	// This contains a mapping of all the IPs with their active connections.
	private Map<String, Integer> activeIPCount;

	public Map<String, Integer> getActiveCount() {
		return this.activeIPCount;
	}

	/**
	 * Construct a new RuneLite socket server.
	 * @param port Valid port number (1 - 65565) to start the server on.
	 */
	public RLServer(Map<String, String> arguments) {
		if (arguments.containsKey("logging"))
			this.logger = new MockFileLogger(System.out);
		else
			this.logger = new MockLogger(System.out);

		this.arguments = arguments;
		this.loadArguments();

		this.connectedClients = new HashSet<RLClient>();
		this.activeClients = new HashMap<String, HashSet<RLClient>>();
		this.activeIPCount = new HashMap<String, Integer>();
	}

	private void loadArguments() {
		this.logger.println(RLServer.class, Level.INFO, "Detected settings as " + this.arguments.toString());

		this.isVerbose = this.arguments.containsKey("verbose");
		this.logger.println(RLServer.class, Level.INFO, "Logging verbose is set to " + this.isVerbose);

		if (this.arguments.containsKey("port"))
			this.port = this.parseIntExitIfFail(this.arguments.get("port"));

		if (this.arguments.containsKey("room-limit")) // It wouldn't make sense if there were less than 2 people in a room.
			this.roomLimit = Math.max(2, this.parseIntExitIfFail(this.arguments.get("room-limit")));
		this.logger.println(RLServer.class, Level.INFO, "Maximum amount of people per room set to " + this.roomLimit);

		if (this.arguments.containsKey("ip-limit")) // It wouldn't make sense if no-one could connect.
			this.ipLimit = Math.max(1, this.parseIntExitIfFail(this.arguments.get("ip-limit")));
		this.logger.println(RLServer.class, Level.INFO, "Maximum amount of people per IP address set to " + this.ipLimit);
	}

	private int parseIntExitIfFail(String s) {
		if (s == null)
			s = "null";

		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			System.err.printf("Error: %s is not a valid integer.\n", s);
			System.exit(1);
			return -1;
		}
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

	public boolean isVerbose() {
		return this.isVerbose;
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
