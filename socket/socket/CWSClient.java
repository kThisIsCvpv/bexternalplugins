package net.runelite.client.plugins.socket.socket;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.socket.SocketAESEncryption;
import net.runelite.client.plugins.socket.messages.ClientMessage;
import net.runelite.client.plugins.socket.messages.ClientsocketMessage;
import net.runelite.client.plugins.socket.messages.server.Join;
import net.runelite.client.plugins.socket.messages.ServerMessage;
import org.java_websocket.handshake.ServerHandshake;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

@Singleton
@Slf4j
public class CWSClient implements SocketListener, AutoCloseable
{
	private final EventBus eventBus;
	private final Collection<Class<? extends ClientsocketMessage>> messages = new HashSet<>();

	private volatile Gson gson;
	protected Clientsocket clientsocket;
	private String secret;

	@Inject
	private CWSClient(EventBus eventBus)
	{
		this.eventBus = eventBus;
		this.gson = ClientsocketGsonFactory.build(ClientsocketGsonFactory.factory(messages));
	}

	public void registerMessage(final Class<? extends ClientsocketMessage> message)
	{
		if (messages.add(message))
		{
			gson = ClientsocketGsonFactory.build(ClientsocketGsonFactory.factory(messages));
		}
	}

	public void unregisterMessage(final Class<? extends ClientsocketMessage> message)
	{
		if (messages.remove(message))
		{
			gson = ClientsocketGsonFactory.build(ClientsocketGsonFactory.factory(messages));
		}
	}

	public boolean isConnected()
	{
		return clientsocket != null && clientsocket.isOpen();
	}

	public void createConnection(String address, String secret, int port, int room, String name)
	{
		createConnection(address, secret, port, room, name, null);
	}

	public void createConnection(String address, String secret, int port, int room, String name, Runnable callback)
	{
		try
		{
			clientsocket = new Clientsocket(new URI("ws://" + address + ":" + port), this);
		}
		catch (URISyntaxException e)
		{
			log.debug("Error setting up connection", e);
			return;
		}
		this.secret = secret;
		new Thread(() ->
		{
			try
			{
				clientsocket.connectBlocking();
				sendServerMessage(new Join(room, name));
				if (callback != null)
				{
					callback.run();
				}
			}
			catch (InterruptedException e)
			{
				log.debug("Error setting up connection", e);
			}
		}).start();
	}

	public void sendServerMessage(ClientsocketMessage message)
	{
		send(new ServerMessage(gson.toJson(message, ClientsocketMessage.class)));
	}

	public void sendEndToEndEncrypted(ClientsocketMessage message)
	{
		String json = gson.toJson(message, ClientsocketMessage.class);
		try
		{
			String encrypted = SocketAESEncryption.encrypt(secret, json);
			send(new ClientMessage(encrypted));
		}
		catch (Exception exception)
		{
			log.warn("Error encrypting", exception);
		}
	}

	public void send(ClientsocketMessage message)
	{
		if (clientsocket != null)
		{
			final String json = gson.toJson(message, ClientsocketMessage.class);
			clientsocket.send(json);
			log.debug("Sent: {}", json);
		}
	}

	@Override
	public void close()
	{
		if (clientsocket != null)
		{
			clientsocket.close(1000, null);
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		log.info("Websocket {} opened", clientsocket);
	}

	@Override
	public void onMessage(String message)
	{
		final ClientsocketMessage clientsocketMessage;

		try
		{
			clientsocketMessage = gson.fromJson(message, ClientsocketMessage.class);
		}
		catch (JsonParseException e)
		{
			log.debug("Failed to deserialize message", e);
			return;
		}

		ClientsocketMessage _message = null;

		if (clientsocketMessage instanceof ClientMessage)
		{
			final String decryptedMessage;
			try
			{
				decryptedMessage = SocketAESEncryption.decrypt(secret, ((ClientMessage) clientsocketMessage).getEncryptedMessage());
			}
			catch (Exception exception)
			{
				log.debug("Error decrypting message", exception);
				return;
			}
			_message = gson.fromJson(decryptedMessage, ClientsocketMessage.class);
		}
		else if (clientsocketMessage instanceof ServerMessage)
		{
			_message = gson.fromJson(((ServerMessage) clientsocketMessage).getMessage(), ClientsocketMessage.class);
		}

		if (_message == null)
		{
			return;
		}

		log.debug("Got: {}", _message);
		eventBus.post(_message);

	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		log.info("Websocket {} closed: {}/{}", clientsocket, code, reason);
		clientsocket = null;
	}

	@Override
	public void onError(Exception ex)
	{
		log.warn("Error in websocket", ex);
		clientsocket = null;
	}
}
