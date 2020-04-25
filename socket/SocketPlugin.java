package net.runelite.client.plugins.socket;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.socket.messages.server.Join;
import net.runelite.client.plugins.socket.messages.server.Leave;
import net.runelite.client.plugins.socket.socket.CWSClient;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(
        name = "Socket",
        description = "Socket connection for broadcasting messages across clients.",
        tags = {"socket", "server", "discord", "connection", "broadcast"},
        enabledByDefault = false
)
public class SocketPlugin extends Plugin
{
	@Inject
	private CWSClient cwsClient;

	@Inject
	private SocketConfig config;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService executorService;

	@Provides
	SocketConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketConfig.class);
	}

	private void connect()
	{
		String name = "Unknown";
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null && !client.getLocalPlayer().getName().isEmpty())
		{
			name = client.getLocalPlayer().getName();
		}
		cwsClient.createConnection(config.getServerAddress(), config.getSalt(), config.getServerPort(),
				config.getRoom(), name,
				() -> clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
						"<col=008000>Connection to the socket server was successfully established.", null)));
	}

	@Override
	protected void startUp() throws Exception
	{
		if (client.getGameState() == GameState.LOGGED_IN && !cwsClient.isConnected())
		{
			executorService.schedule(() -> clientThread.invokeLater(this::connect), 500, TimeUnit.MILLISECONDS);
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && !cwsClient.isConnected())
		{
			executorService.schedule(() -> clientThread.invokeLater(this::connect), 500, TimeUnit.MILLISECONDS);
		}
	}

	@Subscribe
	protected void onJoin(Join join)
	{
		clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				String.format("<col=ff0000>%s has joined the room", join.getName()), null));
	}

	@Subscribe
	protected void onLeave(Leave leave)
	{
		clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				String.format("<col=ff0000>%s has left the room", leave.getName()), null));
	}

	@Override
	protected void shutDown() throws Exception
	{
		cwsClient.close();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("SocketPlugin"))
		{
			clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Configuration changed. Please restart the plugin to see updates.", null));
		}
	}
}