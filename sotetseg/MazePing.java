package net.runelite.client.plugins.sotetseg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.client.plugins.socket.messages.ClientsocketMessage;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MazePing extends ClientsocketMessage
{
	@Data
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	static
	class MazeTile extends ClientsocketMessage
	{
		private int x;
		private int y;
		private int plane;
	}

	private MazeTile[] mazeTiles;
}
