package com.kthisiscvpv.sockettunnel.messages.server;

import com.kthisiscvpv.sockettunnel.messages.ClientsocketMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
public class Join extends ClientsocketMessage
{
	private Object room;
	private String name;
}
