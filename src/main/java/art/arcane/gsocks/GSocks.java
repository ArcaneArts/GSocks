package art.arcane.gsocks;

import art.arcane.gsocks.pkt.AuthenticationPacket;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class GSocks {
    public static void main(String[] a) throws IOException, InterruptedException {
        // Setup server
        GSocksServer server = GSocksServer.start(12345);
        server.setHandler((clnt, pkt) -> {
            if(pkt instanceof Packet p) {
                p.handleClientToServer(clnt);
            }
        });

        // Setup client
        GSocksClient client = new GSocksClient("localhost", 12345);
        client.setHandler((clnt, pkt) -> {
            if(pkt instanceof Packet p) {
                p.handleServerToClient(clnt);
            }
        });

        client.send(AuthenticationPacket.builder()
                .authCode(UUID.randomUUID())
                .playerId(UUID.randomUUID())
                .build());
        Thread.sleep(10000);


        client.send(AuthenticationPacket.builder()
                .authCode(UUID.randomUUID())
                .playerId(UUID.randomUUID())
                .build());
        Thread.sleep(10000);


        server.shutdown();
    }
}
