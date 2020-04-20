# External Plugins for BlueLite

This is my repository for my own plugins for BlueLite.

You can join their Discord via the following URL: https://discord.gg/mezdbNZ

Please direct any help / support / inquires / requests there as well.

# Plugin Installation

1. Press ``Win + R``
2. Type in ``%USERPROFILE%/.runelite``
3. Create a folder named ``bexternalplugins`` if it does not already exist.
4. Drag plugins into that folder.
5. Restart your BlueLite.

# Socket

Socket is a peer to peer connection plugin.

Users join a server room and broadcast messages amongst each other.

- [Socket Client Plugin](#socket-client-plugin)
- [Socket Server](#socket-server)

## Socket Client Plugin

[Source Code](./socket)

### Configuration

![alt text](https://www.kthisiscvpv.com/RZ8xV15873467140DE6o.png "Plugin Settings")

**Server Address:**

> Description: The IP address or host name of the server to connect to.
> Default: socket.kthisiscvpv.com

**Server Port:**

> Description: The open port number of the server.
> Default: 25340

**Server Room:**

> Description: A valid positive integer (per Java's defination) that depicts a room number to join on the server.
> Default: 1

**Shared Password:**

> Description: A password shared between your peers to encrypt and decrypt the bounding packets.
> Default: Randomally Generated

### API Documentation

To listen to packets being sent on the network, create an event listener on ``SocketReceivePacket``.

```
    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            System.out.println(payload.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

To send packets to the network, invoke an event on ``SocketBroadcastPacket``.

```
	...

	JSONObject payload = new JSONObject();
	payload.put("my-name", "Charles");
	eventBus.post(new SocketBroadcastPacket(payload));
	
	...
```

## Socket Server

[Source Code](./SocketTunnel)

### Setup

1. Navigate to [TunnelServer.java](./SocketTunnel/src/com/kthisiscvpv/sockettunnel/TunnelServer.java)
2. Change the global variable ``public static final int SERVER_PORT`` to a port of your choice.
3. Ensure that the port is open on your network.
4. Start the server.
5. Retreive your IP address using any provider such as [IPChicken](https://www.ipchicken.com/).
6. Share that information with your friends.

# Sotetseg (Extended)

[Source Code](./sotetseg)

A standalone extension of the Theatre of Blood plugins. This plugin will send and receive all mazes amongst those in your domain.

![alt text](https://www.kthisiscvpv.com/vtNeo1587347912tMYaN.png "Plugin Visual")

### Dependencies

> Socket Client Plugin

### Configuration

![alt text](https://www.kthisiscvpv.com/DRySJ1587347714fWCmS.png "Plugin Settings")

**Maze Color:**

> Description: The color of the maze being sent and drawn.
> Default: GREEN