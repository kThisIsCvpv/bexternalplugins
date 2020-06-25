package net.runelite.client.plugins.socket.packet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SocketPlayerLeave {

    @Getter(AccessLevel.PUBLIC)
    private String playerName;

}
