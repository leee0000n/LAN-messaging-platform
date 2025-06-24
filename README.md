# LAN-messaging-platform
A simple TCP console-based messaging platform that works over a Local Area Network (LAN), built in Java.  
This is a small learning project focused on understanding how LAN communication and basic client-server networking works in Java.

## How to use
To start the server, use `./gradlew.bat server --args "<port>"` replacing <port> with a valid port number

To start a client, use `./gradlew.bat client --args "localhost <port>"`, replacing <port> with a valid port number. localhost can also be replaced with a valid IP address.

## Current Issues
Issues with the client where users must send a message first to see messages from other clients.
