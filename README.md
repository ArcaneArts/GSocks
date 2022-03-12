# GSocks
Simple Client &amp; Server socket server for serializing objects based on gson

## Get It

```gradle
repositories {
    maven { url "https://dl.cloudsmith.io/public/arcane/archive/maven/" }
}
```

```gradle
dependencies {
    implementation 'art.arcane:GSocks:<VERSION>'
}
```

## An example packet
These should be shared in both client & server projects 

```java
// It's wise to use lombok here builders recommended
@Builder
public class AuthenticationPacket implements Packet {
    // Define packet properties
    private UUID playerId;
    private UUID authCode;

    // This is called when the client sends the server an auth packet. the server needs to handel this
    @Override
    public void handleClientToServer(GSocksClientBoundConnection receiver) {
        System.out.println("Client sent to us (server) auth packet " + new Gson().toJson(this));
    }

    // The client should do nothing when receiving this packet as its not part of the "protocol"
    @Override
    public void handleServerToClient(GSocksClient receiver) {
        throw new UnsupportedOperationException();
    }
}

```

## Make a Server
```java
// Make the server bind to port 12345
GSocksServer server = GSocksServer.start(12345);

// Handle incoming packets
// clnt is the client connection from the server side
// pkt is an object deserialized from gson
server.setHandler((clnt, pkt) -> {
    if(pkt instanceof Packet p) {
        p.handleClientToServer(clnt); // Call the handle method
    }
});
```

## Make a Client
```java
// Make the client bind to the server address & port
GSocksClient client = new GSocksClient("localhost", 12345);

// Handle the incoming packets from the server
// clnt is the same as client, just pass it through, im lazy
// pkt is the object deserialized from the server
client.setHandler((clnt, pkt) -> {
    if(pkt instanceof Packet p) {
        p.handleServerToClient(clnt); // Call the handle method
    }
});
```

## Send a packet to the server as the client
```java
client.send(AuthenticationPacket.builder()
        .authCode(UUID.randomUUID())
        .playerId(UUID.randomUUID())
        .build());
```

## Find clients
Clients are given connection UUIDs, use these to distinguish your clients and access them faster
```java
for(GSocksClientBoundConnection i : server.getClientList())
{
    // This is a client connection from the server side
    i.getConnectionId(); // this is their connection id
}

// Get a client by their connection uuid
server.getClient(theirConnectionUUID);
```

## Send packets to clients
```java
server.getClient(clientUUID).send(AuthenticationPacket.builder()
        .authCode(UUID.randomUUID())
        .playerId(UUID.randomUUID())
        .build());
```
