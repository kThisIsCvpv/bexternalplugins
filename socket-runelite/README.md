1.

Go to https://github.com/kThisIsCvpv/bexternalplugins/tree/master/socket-runelite
Download that entire folder ``socker-runelite``. This is the base plugin that your plugin will need to reference.
Save it under ``package net.runelite.client.plugins.socket``
When you make an external, no need to include that (you could), but it's safe to just assume that all your users have socket installed.

2.

You don't need ``package net.runelite.client.plugins.socket.plugins``.
That's just a folder for my socket plugins as a sample, so go ahead and delete it.

3.

The API is developed in such a way that there's really only methods you need to be concerned about (easy right?).

SocketBroadcastPacket - YOU broadcast a packet to the server.
SocketReceivePacket - THE SERVER broadcasts a packet to your client.

The insecurity about this method is that all plugins can see the packets being recieved from your client, so some filtering must be done inside your plugin.

Let's dissect Sotetseg.

```
...
JSONObject payload = new JSONObject();
payload.put("sotetseg-extended", data);
eventBus.post(new SocketBroadcastPacket(payload)); <-- YOU invoke a new broadcast event, so SOCKET can pick up on it.
```

```
@Subscribe
public void onSocketReceivePacket(SocketReceivePacket event) { <-- SOCKET invokes a new broadcast event so YOUR PLUGIN can pick up on it.
	JSONObject payload = event.getPayload();
	if (!payload.has("sotetseg-extended"))
		return;

	JSONArray data = payload.getJSONArray("sotetseg-extended");
	...
}
```

The transport method that Socket uses is JSON. If you want to transport binary, encode your stream in Base64 before passing it onto Socket as a JSON string value.

To know that which payloads are mine, notice how I load the payload in a key ``sotetseg-extended`` before passing into socket?

When I get the same payload back, I check if ``sotetseg-extended`` is a key before parsing. Now I have all my original ``data`` in both instances.

Obviously, this is not necessary, but hopefully developers using socket will avoid collisions by this method.
