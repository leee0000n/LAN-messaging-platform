# LAN-messaging-platform
A console messaging platform that works over LAN. Made in Java 

## How to use
To start the server, use `./gradlew.bat server --args "<port>"` replacing <port> with a valid port number

To start a client, use `./gradlew.bat client --args "localhost <port>"`, replacing <port> with a valid port number. localhost can also be replaced with a valid IP address.

## Current Issues
Issues with the client where users must send a message first to see messages from other clients.
