# Socket Server

Socket server is the middleman for socket plugins. Individuals using the socket plugin must establish a connection to a common socket server in order to broadcast data to amongst each other.

For convenience, a public server is hosted on ``socket.kthisiscvpv.com`` on port ``26388``.

## Setup

Use the following tutorial to set up your own socket server.

### Via Command Line

1. Download [Socket Server Standalone](https://github.com/kThisIsCvpv/bexternalplugins/releases/download/2.0.7/socket-server-standalone.zip).
2. Unzip the project into a folder.
3. Launch ``run.bat``.
4. Retreive your IP address using any provider such as [IPChicken](https://www.ipchicken.com/).
5. Share that IP address and the default port ``26388`` with your friends.
6. Alternatively, you can run ``java -jar socket-server-standalone.jar [PORT_NUMBER]`` to specify your own port number.

### Via IDE

1. Clone [/socket-server-standalone](./)
2. Navigate to [App.java](./src/com/kthisiscvpv/App.java).
3. Execute. Change variables as necessary.
4. Same as above, follow from Step 5 above to retrieve your IP address.

## Tips

### Connection Testing

If you are hosting the server on your own computer, you can test whether or not the server is up by connecting to ``localhost``.

![alt text](https://kthisiscvpv.com/SQWqyDg3A22MpI5eKH.png)

### Port Forwarding

If you are able to connect to the server locally, then you also need to port forward your router so others can connect to your home infrastructure from the outside.
