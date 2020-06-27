package com.kthisiscvpv.sockettunnel.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
// Encrypted message for end to end encrypted communication.
// The server will just pass it on to the other clients in the room.
public class ClientMessage extends ClientsocketMessage
{
	private String encryptedMessage;
}
