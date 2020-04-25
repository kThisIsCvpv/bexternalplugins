package net.runelite.client.plugins.socket.socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

class Clientsocket extends WebSocketClient
{
	private SocketListener listener;

	public Clientsocket(URI serverUri, SocketListener listener)
	{
		super(serverUri);
		this.listener = listener;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		listener.onOpen(handshakedata);
	}

	@Override
	public void onMessage(String message)
	{
		listener.onMessage(message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		listener.onClose(code, reason, remote);
	}

	@Override
	public void onError(Exception ex)
	{
		listener.onError(ex);
	}
}
