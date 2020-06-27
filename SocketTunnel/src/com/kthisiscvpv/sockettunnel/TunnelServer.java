package com.kthisiscvpv.sockettunnel;

import com.google.gson.Gson;
import com.kthisiscvpv.sockettunnel.messages.ClientMessage;
import com.kthisiscvpv.sockettunnel.messages.ClientsocketMessage;
import com.kthisiscvpv.sockettunnel.messages.server.Join;
import com.kthisiscvpv.sockettunnel.messages.server.Leave;
import com.kthisiscvpv.sockettunnel.messages.ServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TunnelServer extends WebSocketServer
{
	private final Gson gson = ClientsocketGsonFactory.build();
	private final ConcurrentHashMap<Object, HashSet<WebSocket>> roomClient = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<WebSocket, Client> clients = new ConcurrentHashMap<>();

	public TunnelServer(int port)
	{
		super(new InetSocketAddress(port));
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		log.debug("{} has connected", conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	private void removeClient(WebSocket conn)
	{
		Client client = clients.get(conn);
		if (client != null)
		{
			Object room = client.getRoom();
			if (room != null)
			{
				HashSet<WebSocket> clients = roomClient.get(room);
				if (clients != null)
				{
					clients.remove(conn);
				}
			}
		}
		this.clients.remove(conn);
	}

	private void addClient(WebSocket conn, Client client)
	{
		clients.put(conn, client);
		HashSet<WebSocket> clients = roomClient.getOrDefault(client.getRoom(), new HashSet<>());
		clients.add(conn);
		roomClient.put(client.getRoom(), clients);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		Client client = clients.get(conn);
		if (client != null)
		{
			removeClient(conn);
			sendToRoom(client.getRoom(), gson.toJson(new ServerMessage(gson.toJson(new Leave(client.getName()), ClientsocketMessage.class))));

			log.debug("{} has disconnected", client.getName());
		}
		else
		{
			log.debug("{} has disconnected", conn);
		}
	}


	private void sendToRoom(WebSocket conn, String message)
	{
		Client client = clients.get(conn);
		if (client != null)
		{
			Object room = client.getRoom();
			if (room != null)
			{
				sendToRoom(room, message);
			}
		}
	}

	private void sendToRoom(Object room, String message)
	{
		HashSet<WebSocket> clients = roomClient.get(room);
		if (clients != null)
		{
			clients.forEach(client -> client.send(message));
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		ClientsocketMessage _message = gson.fromJson(message, ClientsocketMessage.class);

		if (_message instanceof ServerMessage)
		{
			log.debug("Received from {} : {}", conn.getRemoteSocketAddress().getAddress().getHostAddress(), ((ServerMessage) _message).getMessage());
			ClientsocketMessage serverMessage = gson.fromJson(((ServerMessage) _message).getMessage(), ClientsocketMessage.class);
			if (serverMessage instanceof Join)
			{
				Join join = (Join) serverMessage;
				Object room = join.getRoom();
				if (clients.containsKey(conn))
				{
					removeClient(conn);
				}
				addClient(conn, new Client(join.getName(), join.getRoom()));
				sendToRoom(room, gson.toJson(new ServerMessage(gson.toJson(new Join(room, join.getName()), ClientsocketMessage.class)), ClientsocketMessage.class));
			}
		}
		else if (_message instanceof ClientMessage)
		{
			log.debug("broadcasting message from {}", conn.getRemoteSocketAddress().getAddress().getHostAddress());
			sendToRoom(conn, message);
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException
	{
		int port = 25340;
		try
		{
			port = Integer.parseInt(args[0]);
		}
		catch (Exception ignored)
		{
		}
		TunnelServer s = new TunnelServer(port);
		s.start();
		log.info("ChatServer started on port: " + s.getPort());
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		log.warn("Error in server socket", ex);
		if (conn != null)
		{
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart()
	{
		log.info("Server started!");
		setConnectionLostTimeout(100);
	}
}
