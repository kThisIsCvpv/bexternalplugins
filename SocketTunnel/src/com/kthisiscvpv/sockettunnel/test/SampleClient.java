package com.kthisiscvpv.sockettunnel.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

import com.kthisiscvpv.sockettunnel.TunnelServer;

public class SampleClient implements Runnable {

	public static final String SERVER_HOST = "socket.kthisiscvpv.com";
	public static final int SERVER_PORT = TunnelServer.SERVER_PORT;

	private Socket socket;
	private BufferedReader bufferedreader;
	private PrintWriter printwriter;

	private Thread instance;

	private boolean isValid;

	public SampleClient() {
		this.socket = null;
		this.bufferedreader = null;
		this.printwriter = null;
		this.instance = null;
		this.isValid = false;
	}

	@Override
	public void run() {
		try {
			System.out.println("Connection started.");
			
			String ln;
			while (this.isValid && this.socket.isConnected() && !this.socket.isClosed() && (ln = this.bufferedreader.readLine()) != null) {
				System.out.println(ln);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.closeConnection();
	}

	public void connect(String host, int port) {
		try {
			this.socket = new Socket(host, port);
			this.bufferedreader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.printwriter = new PrintWriter(this.socket.getOutputStream(), true);

			this.isValid = true;
			this.instance = new Thread(this);
			this.instance.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void joinRoom(int room) {
		JSONObject joinObject = new JSONObject();
		joinObject.put("command", "JOIN_ROOM");
		joinObject.put("room", 3);
		this.printwriter.println(joinObject.toString());
	}

	public void broadcast(String message) {
		JSONObject payload = new JSONObject();
		payload.put("message", message);

		JSONObject broadcastObject = new JSONObject();
		broadcastObject.put("command", "BROADCAST");
		broadcastObject.put("payload", payload);
		this.printwriter.println(broadcastObject.toString());
	}

	public void closeConnection() {
		try {
			if (this.bufferedreader != null)
				this.bufferedreader.close();
		} catch (Exception ignorred) {}
		this.bufferedreader = null;

		try {
			if (this.printwriter != null)
				this.printwriter.close();
		} catch (Exception ignorred) {}
		this.printwriter = null;

		try {
			if (this.socket != null)
				this.socket.close();
		} catch (Exception ignorred) {}
		this.socket = null;

		this.isValid = false;
		System.out.println("Connection closed.");
	}

	public static void main(String args[]) throws Exception {
		SampleClient client = new SampleClient();
		client.connect(SERVER_HOST, SERVER_PORT);
		client.broadcast("Broadcasting Message 1");
		client.joinRoom(123);
		client.broadcast("Broadcasting Message 2");

		client.printwriter.println("Fuck you.");

		// System.out.println("Waiting 20 seconds...");
		Thread.sleep(10000L);

		client.broadcast("Broadcasting Message 3");
		client.closeConnection();
	}
}
