package net.runelite.client.plugins.specialcounterextended;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.client.plugins.socket.messages.ClientsocketMessage;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SpecialAttackMessage extends ClientsocketMessage
{
	private String player;
	private int target;
	private int weapon;
	private int hit;
}
