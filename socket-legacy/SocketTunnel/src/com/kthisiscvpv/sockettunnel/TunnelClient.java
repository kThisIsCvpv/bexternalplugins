package com.kthisiscvpv.sockettunnel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class TunnelClient implements Runnable {

	private TunnelServer server;
	private Socket socket;

	private Thread instance;

	private BufferedReader inputstream;
	private PrintWriter outputstream;

	private int room;

	private boolean isValid;

	public TunnelClient(TunnelServer server, Socket socket) {
		this.server = server;
		this.socket = socket;

		this.instance = null;

		this.inputstream = null;
		this.outputstream = null;
		this.room = -1;

		this.isValid = true;
	}

	public void listen() {
		this.instance = new Thread(this);
		this.instance.start();
	}

	public BufferedReader getInputStream() {
		return this.inputstream;
	}

	public PrintWriter getOutputStream() {
		return this.outputstream;
	}

	public int getRoom() {
		return this.room;
	}

	public void logConnection(InetAddress address, String info) {
		try {
			JSONObject status = new JSONObject();
			status.put("command", info);
			status.put("ip", address.getHostAddress());
			status.put("name", address.getHostName());
			System.out.println(status.toString(4));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isValid() {
		return this.isValid;
	}

	protected void closeConnection() {
		if(!this.isValid)
			return;
		
		try {
			if (this.inputstream != null)
				this.inputstream.close();
		} catch (Exception ignorred) {}

		try {
			if (this.outputstream != null)
				this.outputstream.close();
		} catch (Exception ignorred) {}

		try {
			if (this.socket != null)
				this.socket.close();
		} catch (Exception ignorred) {}

		this.isValid = false;

		this.server.purge(this);

		this.logConnection(this.socket.getInetAddress(), "CLOSE_CONNECTION");
	}

	@Override
	public void run() {
		try {
			this.inputstream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.outputstream = new PrintWriter(this.socket.getOutputStream(), true);
			this.logConnection(this.socket.getInetAddress(), "OPEN_CONNECTION");

			String ln;
			while (this.isValid && this.socket.isConnected() && !this.socket.isClosed() && (ln = this.inputstream.readLine()) != null) {
				ln = ln.trim();
				if(ln.length() <= 2)
					continue;
				
				JSONObject obj = new JSONObject(ln);
				System.out.println(obj.toString(4));

				if (!obj.has("command"))
					throw new JSONException("User sent no command payload.");

				String command = obj.getString("command");

				if (command.equals("JOIN_ROOM")) {
					this.room = obj.getInt("room");
					if (this.room <= 0)
						throw new JSONException("User sent an unknown room identifier.");
					
					this.server.join(this.room, this);
				} else if (command.equals("BROADCAST")) {
					if (this.room <= 0) { // We assume this is the result of some client race condition. Don't terminate the connection.
						System.err.println("User attempted to broadcast while not being in a room.");
						continue;
					}

					if (!obj.has("payload"))
						throw new JSONException("User attempted to broadcast but with no payload.");

					String payload = obj.getString("payload");
					this.server.broadcast(this.room, payload);
				} else
					throw new JSONException("Recieved unknown command payload.");
			}

			this.closeConnection();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			this.closeConnection();
		}
	}
}
