package net.runelite.client.plugins.socket.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
public class ClientMessage extends ClientsocketMessage
{
	private String encryptedMessage;
}
