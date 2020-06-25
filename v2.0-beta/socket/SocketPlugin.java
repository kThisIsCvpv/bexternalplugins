package net.runelite.client.plugins.socket;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.socket.hash.AES256;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketPlayerJoin;
import net.runelite.client.plugins.socket.packet.SocketPlayerLeave;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;

import javax.inject.Inject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import static net.runelite.client.plugins.socket.SocketConfig.PASSWORD_SALT;

@Slf4j
@PluginDescriptor(
        name = "Socket",
        description = "Socket connection for broadcasting messages across clients.",
        tags = {"socket", "server", "discord", "connection", "broadcast"},
        enabledByDefault = false
)
public class SocketPlugin extends Plugin {

    @Inject
    @Getter(AccessLevel.PUBLIC)
    private Client client;

    @Inject
    @Getter(AccessLevel.PUBLIC)
    private EventBus eventBus;

    @Inject
    @Getter(AccessLevel.PUBLIC)
    private ClientThread clientThread;

    @Inject
    @Getter(AccessLevel.PUBLIC)
    private SocketConfig config;

    @Provides
    SocketConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SocketConfig.class);
    }

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private long nextConnection;

    private SocketConnection connection = null;

    @Override
    protected void startUp() {
        this.nextConnection = 0L;

        eventBus.register(SocketReceivePacket.class);
        eventBus.register(SocketBroadcastPacket.class);

        eventBus.register(SocketPlayerJoin.class);
        eventBus.register(SocketPlayerLeave.class);
    }

    @Override
    protected void shutDown() {
        eventBus.unregister(SocketReceivePacket.class);
        eventBus.unregister(SocketBroadcastPacket.class);

        eventBus.unregister(SocketPlayerJoin.class);
        eventBus.unregister(SocketPlayerLeave.class);

        if (this.connection != null)
            this.connection.terminate(true);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Attempt connecting, or re-establishing connection to the socket server, only when the user is logged in.
        if (client.getGameState() == GameState.LOGGED_IN) {
            if (this.connection != null) {
                SocketState state = this.connection.getState();
                if (state == SocketState.CONNECTING || state == SocketState.CONNECTED)
                    return;
            }

            if (System.currentTimeMillis() >= this.nextConnection) {
                this.nextConnection = System.currentTimeMillis() + 30000L;
                this.connection = new SocketConnection(this, this.client.getLocalPlayer().getName());
                new Thread(this.connection).start();
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("SocketPlugin"))
            this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=b4281e>Configuration changed. Please restart the plugin to see updates.", null));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        // Terminate all connections to the socket server when the user logs out.
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
            if (this.connection != null)
                this.connection.terminate(false);
        }
    }

    @Subscribe
    public void onSocketBroadcastPacket(SocketBroadcastPacket packet) {
        try {
            if (this.connection == null || this.connection.getState() != SocketState.CONNECTED)
                return;

            String data = packet.getPayload().toString();
            log.info("Deploying packet from client: {}", data);

            String secret = this.config.getPassword() + PASSWORD_SALT;

            JSONObject payload = new JSONObject();
            payload.put("header", SocketPacket.BROADCAST);
            payload.put("payload", AES256.encrypt(secret, data)); // Payload is now an encrypted string.

            PrintWriter outputStream = this.connection.getOutputStream();
            synchronized (outputStream) {
                outputStream.println(payload.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
