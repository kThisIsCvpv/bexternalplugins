package net.runelite.client.plugins.socket;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.socket.hash.AES256;
import net.runelite.client.plugins.socket.hash.SHA256;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONException;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketPlayerJoin;
import net.runelite.client.plugins.socket.packet.SocketPlayerLeave;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import static net.runelite.client.plugins.socket.SocketConfig.PASSWORD_SALT;

/**
 * Represents an instance of a socket connection to the server.
 */
@Slf4j
public class SocketConnection implements Runnable {

    private SocketPlugin plugin;
    private SocketConfig config;

    private Client client;
    private ClientThread clientThread;

    private EventBus eventBus;

    private String playerName;

    // Variable that identifies the state of the socket connection.
    @Getter(AccessLevel.PUBLIC)
    private SocketState state;

    // Socket IO Variables
    @Getter(AccessLevel.PUBLIC)
    private Socket socket;

    @Getter(AccessLevel.PUBLIC)
    private BufferedReader inputStream;

    @Getter(AccessLevel.PUBLIC)
    private PrintWriter outputStream;

    private long lastHeartbeat;

    public SocketConnection(SocketPlugin plugin, String playerName) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();

        this.client = this.plugin.getClient();
        this.clientThread = this.plugin.getClientThread();

        this.eventBus = this.plugin.getEventBus();

        this.playerName = playerName;
        this.lastHeartbeat = 0L;

        this.state = SocketState.DISCONNECTED;
    }

    @Override
    public void run() {
        if (this.state != SocketState.DISCONNECTED)
            return;

        final String secret = this.config.getPassword() + PASSWORD_SALT; // We're going to salt the password as well.

        this.state = SocketState.CONNECTING;
        log.info("Attempting to establish socket connection to {}:{}", this.config.getServerAddress(), this.config.getServerPort());

        try { // Attempt to establish a connection.
            InetSocketAddress address = new InetSocketAddress(this.config.getServerAddress(), this.config.getServerPort());

            this.socket = new Socket();
            this.socket.connect(address, 10000);

            this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.outputStream = new PrintWriter(this.socket.getOutputStream(), true);

            // Notify the server about your connection credentials.
            JSONObject joinPacket = new JSONObject();
            joinPacket.put("header", SocketPacket.JOIN);
            joinPacket.put("room", SHA256.encrypt(secret));
            joinPacket.put("name", AES256.encrypt(secret, this.playerName));
            this.outputStream.println(joinPacket.toString());

            while (true) {
                if (this.state == SocketState.DISCONNECTED || this.state == SocketState.TERMINATED)
                    break; // If object was terminated, stop loop.

                if (!this.socket.isConnected() || this.socket.isClosed())
                    break; // Socket was disconnected, stop loop.

                if (this.outputStream.checkError())
                    throw new IOException("Broken transmission stream");

                if (!this.inputStream.ready()) { // If there is no data ready, ping the server.
                    long elapsedTime = System.currentTimeMillis() - this.lastHeartbeat;
                    if (elapsedTime >= 30000) { // Maintain a heartbeat with the server every 30 seconds.
                        this.lastHeartbeat = System.currentTimeMillis();
                        synchronized (this.outputStream) {
                            this.outputStream.println();
                        }
                    }

                    Thread.sleep(20L);
                    continue;
                }

                String packet = this.inputStream.readLine();
                if (packet == null || packet.isEmpty())
                    continue;

                log.info("Received packet from server: {}", packet);

                JSONObject data;
                try {
                    data = new JSONObject(packet);
                    log.info("Decoded packet as JSON.");
                } catch (JSONException e) {
                    log.warn("Bad packet. Unable to decode: {}", packet);
                    continue;
                }

                if (!data.has("header"))
                    throw new NullPointerException("Packet missing header");

                String header = data.getString("header");

                try {
                    if (header.equals(SocketPacket.BROADCAST)) { // Player is broadcasting a packet to all members.
                        String message = AES256.decrypt(secret, data.getString("payload"));
                        JSONObject payload = new JSONObject(message);
                        this.clientThread.invoke(() -> eventBus.post(new SocketReceivePacket(payload)));

                    } else if (header.equals(SocketPacket.JOIN)) { // Player has joined the party.
                        String targetName = AES256.decrypt(secret, data.getString("player"));
                        this.logMessage(SocketLog.INFO, targetName + " has joined the party.");

                        if (targetName.equals(this.playerName)) { // You have joined the party.
                            this.state = SocketState.CONNECTED;
                            log.info("You have successfully joined the socket party.");
                        }

                        JSONArray membersArray = data.getJSONArray("party");
                        this.logMessage(SocketLog.INFO, this.mergeMembers(membersArray, secret));

                        try {
                            this.eventBus.post(new SocketPlayerJoin(targetName));
                        } catch (Exception ignorred) {
                        }

                    } else if (header.equals(SocketPacket.LEAVE)) { // Player has left the party.
                        String targetName = AES256.decrypt(secret, data.getString("player"));
                        this.logMessage(SocketLog.ERROR, targetName + " has left the party.");

                        JSONArray membersArray = data.getJSONArray("party");
                        this.logMessage(SocketLog.ERROR, this.mergeMembers(membersArray, secret));

                        try {
                            this.eventBus.post(new SocketPlayerLeave(targetName));
                        } catch (Exception ignorred) {
                        }

                    } else if (header.equals(SocketPacket.MESSAGE)) { // Socket server wishes to send you a message.
                        String message = data.getString("message");
                        this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null));
                    }
                } catch (JSONException e) {
                    log.info("Bad packet contents. Unable to decode.");
                    continue;
                }
            }
        } catch (Exception ex) { // Oh no, something went wrong!
            log.error("Unable to establish connection with the server.", ex);
            this.terminate(false);

            this.logMessage(SocketLog.ERROR, "Socket terminated. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());

            this.plugin.setNextConnection(System.currentTimeMillis() + 30000L);
            this.logMessage(SocketLog.ERROR, "Reconnecting in 30 seconds...");
            return;
        }
    }

    public void terminate(boolean verbose) {
        if (this.state == SocketState.TERMINATED)
            return;

        this.state = SocketState.TERMINATED;

        try { // Close the socket output stream.
            if (this.outputStream != null)
                this.outputStream.close();
        } catch (Exception ignorred) {
        }

        try { // Close the socket input stream.
            if (this.inputStream != null)
                this.inputStream.close();
        } catch (Exception ignorred) {
        }

        try { // Close the actual socket.
            if (this.socket != null) {
                this.socket.close();
                this.socket.shutdownOutput();
                this.socket.shutdownInput();
            }
        } catch (Exception ignorred) {
        }

        log.info("Terminated connections with the socket server.");
        if (verbose)
            this.logMessage(SocketLog.INFO, "Any active socket server connections were closed.");
    }

    private String mergeMembers(JSONArray membersArray, String secret) {
        int count = membersArray.length();
        String members = String.format("Member%s (%d): ", count != 1 ? "s" : "", count);
        for (int i = 0; i < count; i++) {
            if (i > 0)
                members += ", ";
            members += AES256.decrypt(secret, membersArray.getString(i));
        }

        return members;
    }

    /**
     * Logs a message inside the player's in-game chatbox.
     *
     * @param level   Log level, for color coding.
     * @param message The message to log, as a string.
     */
    private void logMessage(SocketLog level, String message) {
        this.clientThread.invoke(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", level.getPrefix() + message, null));
    }
}
