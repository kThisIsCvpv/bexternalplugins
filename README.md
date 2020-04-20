# External Plugins for BlueLite

This is my repository for my own plugins for BlueLite.

You can join their Discord via the following URL: https://discord.gg/mezdbNZ

Please direct any help / support / inquires / requests there as well.

# Socket

Socket is a peer to peer connection plugin.

Users join a server room and broadcast messages amongst each other.

- [Socket Client Plugin](#socket-client-plugin)
- [Socket Server](#socket-server)

## Socket Client Plugin

[Source Code](./socket)

### Configuration

![alt text](https://www.kthisiscvpv.com/RZ8xV15873467140DE6o.png "Plugin Settings")

Server Address:

> The IP address or host name of the server to connect to.

Server Port:

> The open port number of the server.

Server Room:

> A valid positive integer (per Java's defination) that depicts a room number to join on the server.

Shared Password:

> A password shared between your peers to encrypt and decrypt the bounding packets.

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

A standalone extension of the Theatre of Blood plugins. This plugin will send and receive all mazes amongst those in your domain.

![alt text](https://www.kthisiscvpv.com/vtNeo1587347912tMYaN.png "Plugin Visual")

### Dependencies

> Socket Client Plugin

### Configuration

![alt text](https://www.kthisiscvpv.com/DRySJ1587347714fWCmS.png "Plugin Settings")

Maze Color:

> The color of the maze tile you wish to draw.