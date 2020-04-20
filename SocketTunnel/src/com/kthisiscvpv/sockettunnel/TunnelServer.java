package com.kthisiscvpv.sockettunnel;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TunnelServer implements Runnable {

	public static final int SERVER_PORT = 25340;
	private Map<Integer, HashSet<TunnelClient>> connections;

	private Thread heartbeat;

	public TunnelServer() {
		this.connections = new HashMap<Integer, HashSet<TunnelClient>>();
		this.heartbeat = new Thread(this);
	}

	@Override
	public void run() {
		while (true) {
			List<TunnelClient> terminated = new ArrayList<TunnelClient>();
			int count = 0;

			synchronized (this.connections) {
				for (Integer key : this.connections.keySet()) {
					for (TunnelClient client : this.connections.get(key)) {
						try {
							if (client.getOutputStream() != null) {
								client.getOutputStream().println("Server heartbeat: " + System.currentTimeMillis());
								count++;
							}
						} catch (Exception e) {
							terminated.add(client);
							e.printStackTrace();
						}
					}
				}
			}

			try {
				for (TunnelClient client : terminated)
					client.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("--- Heartbeat " + System.currentTimeMillis() + " ---");
			System.out.println("Sent heartbeat to " + count + " connections.");
			System.out.println("Terminated " + terminated.size() + " connections.");

			try {
				Thread.sleep(60 * 1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		this.heartbeat.start();

		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
			System.out.println("Listening on server port " + SERVER_PORT + "...");
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					TunnelClient tunnel = new TunnelClient(this, clientSocket);
					tunnel.listen();
				} catch (Exception ex) {
					System.err.println("Warning: an error occured while trying to accept a client connection");
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("Fatal: unable to start the server");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void join(int room, TunnelClient client) {
		this.purge(client);

		synchronized (this.connections) {
			HashSet<TunnelClient> clients = new HashSet<TunnelClient>();
			Integer key = (Integer) room;
			if (this.connections.containsKey(key))
				clients = this.connections.get(key);

			clients.add(client);
			this.connections.put(key, clients);
		}
	}

	public void purge(TunnelClient client) {
		synchronized (this.connections) {
			List<Integer> toRemove = new ArrayList<Integer>();

			for (Integer key : this.connections.keySet()) {
				HashSet<TunnelClient> clients = this.connections.get(key);
				if (clients.contains(client))
					clients.remove(client);
				if (clients.isEmpty())
					toRemove.add(key);
			}

			for (Integer key : toRemove)
				this.connections.remove(key);
		}

		System.gc();
	}

	public void broadcast(int room, String payload) {
		synchronized (this.connections) {
			Integer key = (Integer) room;
			if (this.connections.containsKey(key)) {
				HashSet<TunnelClient> clients = this.connections.get(key);
				int count = 0;
				for (TunnelClient client : clients)
					try {
						if (client.isValid()) {
							client.getOutputStream().println(payload.toString());
							count++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				System.out.println("Broadcasting to " + count + " different clients...");
			}
		}
	}

	public static void main(String[] args) {
		TunnelServer server = new TunnelServer();
		server.start();
	}
}
