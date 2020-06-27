# External Plugins for BlueLite

This is my repository for my own plugins for BlueLite.

You can join their Discord via the following URL: https://discord.gg/mezdbNZ

Please direct any help / support / inquires / requests there as well.

# Plugin Installation

## Windows

1. Press ``Win + R``
2. Type in ``%USERPROFILE%/.runelite``
3. Create a folder named ``bexternalplugins`` if it does not already exist.
4. Drag plugins into that folder.
5. Restart your BlueLite.

## Mac (Untested)

1. Open Terminal.
2. Run ``cd ~/.runelite``
3. Run ``mkdir -p bexternalplugins``
4. Run ``open .``
5. Drag plugins into ``bexternalplugins``
6. Restart your BlueLite.

# Socket

Socket is a peer to peer connection plugin.

Users join a server room and broadcast messages amongst each other.

- [Socket Client Plugin](#socket-client-plugin)
- [Socket Server](#socket-server)

## Socket Client Plugin

[Source Code](./socket)

depends on:
- https://github.com/TooTallNate/Java-WebSocket
- gson
- slf4j

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

## Socket Server

[Source Code](./SocketTunnel)
depends on:
- https://github.com/TooTallNate/Java-WebSocket
- gson
- slf4j

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

**Stroke Size:**

> Description: The size of the stroke when drawing the tiles.
> Default: 1

# Special Counter (Extended)

[Source Code](./specialcounterextended)

A standalone extension of the default ``Special Attack Counter`` plugin. This plugin will send and receive all special attacks amongst those in your domain.

![alt text](https://www.kthisiscvpv.com/azgkm1587524036HW5Vh.gif "Plugin Visual")

![alt text](https://www.kthisiscvpv.com/jIOBV1587559442NdOUj.gif "Plugin Visual")

### Dependencies

> Socket Client Plugin

### Exclusions (TURN THESE OFF)

> Special Attack Counter

### Configuration

![alt text](https://www.kthisiscvpv.com/Rexft1587523779KWOPt.png "Plugin Settings")

**Hit Overlay:**

> Description: Show the special attack overlay.
> Default: true

**Fade Delay:**

> Description: Delay, in milliseconds, until the icon disappears.
> Default: 5000ms

**Travel Height:**

> Description: Maximum height, in pixels, for the icon to travel.
> Default: 200px