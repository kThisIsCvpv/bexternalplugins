/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.socket.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.runelite.client.plugins.socket.messages.ClientMessage;
import net.runelite.client.plugins.socket.messages.ClientsocketMessage;
import net.runelite.client.plugins.socket.messages.server.Join;
import net.runelite.client.plugins.socket.messages.ServerMessage;
import net.runelite.client.plugins.socket.messages.server.Leave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClientsocketGsonFactory
{
	private static final Collection<Class<? extends ClientsocketMessage>> MESSAGES;

	static
	{
		final List<Class<? extends ClientsocketMessage>> messages = new ArrayList<>();
		messages.add(Join.class);
		messages.add(Leave.class);
		messages.add(ClientMessage.class);
		messages.add(ServerMessage.class);
		MESSAGES = messages;
	}

	public static RuntimeTypeAdapterFactory<ClientsocketMessage> factory(final Collection<Class<? extends ClientsocketMessage>> messages)
	{
		final RuntimeTypeAdapterFactory<ClientsocketMessage> factory = RuntimeTypeAdapterFactory.of(ClientsocketMessage.class);

		for (Class<? extends ClientsocketMessage> message : MESSAGES)
		{
			factory.registerSubtype(message);
		}

		for (Class<? extends ClientsocketMessage> message : messages)
		{
			factory.registerSubtype(message);
		}

		return factory;
	}

	public static Gson build(final RuntimeTypeAdapterFactory<ClientsocketMessage> factory)
	{
		return new GsonBuilder()
			.registerTypeAdapterFactory(factory)
			.create();
	}

	public static Gson build()
	{
		return build(factory(Collections.emptyList()));
	}
}
