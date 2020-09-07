package com.kthisiscvpv.socket.server;

import static com.kthisiscvpv.socket.server.RLConst.DEFAULT_SERVER_PORT;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.util.Utils;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.AbstractLogger.Level;
import com.kthisiscvpv.util.logger.MockFileLogger;
import com.kthisiscvpv.util.logger.MockLogger;

public class RLServer implements Runnable {

	private AbstractLogger logger;
	private int port;

	private HashSet<RLClient> clients;
	private Map<String, HashSet<RLClient>> rooms;

	private int roomLimit;

	private int ipLimit;
	private Map<String, Integer> activeIPAddresses;

	public RLServer(Map<String, String> arguments) {
		loadLogger(arguments);
		loadArguments(arguments);
		
		this.clients = new HashSet<RLClient>();
		this.rooms = new HashMap<String, HashSet<RLClient>>();
		this.activeIPAddresses = new HashMap<String, Integer>();
	}

	public AbstractLogger getLogger() {
		return logger;
	}

	public int getPort() {
		return port;
	}

	public HashSet<RLClient> getClients() {
		return clients;
	}

	public Map<String, Integer> getClientAddresses() {
		return activeIPAddresses;
	}

	public Map<String, HashSet<RLClient>> getRooms() {
		return rooms;
	}

	public int getRoomLimit() {
		return roomLimit;
	}

	public int getIPLimit() {
		return ipLimit;
	}

	private void loadLogger(Map<String, String> arguments) {
		boolean verbose = arguments.containsKey("verbose");

		if (arguments.containsKey("logging"))
			logger = new MockFileLogger(System.out, verbose);
		else
			logger = new MockLogger(System.out, verbose);

		logger.println(RLServer.class, Level.SYSTEM, String.format("Logger has been loaded (type=%s, verbose=%s)", logger.getClass().getName(), Boolean.toString(verbose)));
	}

	private void loadArguments(Map<String, String> arguments) {
		logger.println(RLServer.class, Level.SYSTEM, "Detected settings as " + arguments.toString());

		if (arguments.containsKey("port"))
			port = Utils.parseIntExitIfFail(arguments.get("port"));
		else
			port = DEFAULT_SERVER_PORT;
		logger.println(RLServer.class, Level.SYSTEM, "Server port set to " + port);

		if (arguments.containsKey("room-limit"))
//			roomLimit = Math.max(2, Utils.parseIntExitIfFail(arguments.get("room-limit")));
			roomLimit = Math.max(1, Utils.parseIntExitIfFail(arguments.get("room-limit")));
		else
			roomLimit = -1;
		logger.println(RLServer.class, Level.SYSTEM, "Maximum amount of people per room set to " + (roomLimit > 0 ? roomLimit : "'undefined'"));

		if (arguments.containsKey("ip-limit"))
			ipLimit = Math.max(1, Utils.parseIntExitIfFail(arguments.get("ip-limit")));
		else
			ipLimit = -1;
		logger.println(RLServer.class, Level.SYSTEM, "Maximum amount of people per IP address set to " + (ipLimit > 0 ? ipLimit : "'undefined'"));
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();

		new Thread(new RLServerDoctor(this)).start();
		logger.println(RLServer.class, Level.SYSTEM, "Starting routine integrity checks");

		try (ServerSocket server = new ServerSocket(port)) {
			logger.println(RLServer.class, Level.SYSTEM, "Done. Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");

			while (true) {
				logger.println(RLServer.class, Level.SYSTEM, "Awaiting new client connection");

				try {
					Socket socket = server.accept();
					startTime = System.currentTimeMillis();
					RLClient client = new RLClient(this, socket);
					new Thread(client).start();
				} catch (Exception ex) {
					logger.println(RLServer.class, Level.ERROR, "An error occured while trying to accept a client connection");
					ex.printStackTrace();
				}

				logger.println(RLServer.class, Level.SYSTEM, "New connection processed. Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
			}
		} catch (Exception e) {
			logger.println(RLServer.class, Level.FATAL, "Unable to start the socket server");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
