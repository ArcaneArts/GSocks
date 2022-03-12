package art.arcane.gsocks.pkt;

import art.arcane.gsocks.GSocksClient;
import art.arcane.gsocks.GSocksClientBoundConnection;
import art.arcane.gsocks.Packet;
import com.google.gson.Gson;
import lombok.Builder;

import java.util.UUID;

// client tells server to auth
@Builder
public class AuthenticationPacket implements Packet {
    private UUID playerId;
    private UUID authCode;

    @Override
    public void handleClientToServer(GSocksClientBoundConnection receiver) {
        System.out.println("Client sent to us (server) auth packet " + new Gson().toJson(this));
    }

    @Override
    public void handleServerToClient(GSocksClient receiver) {
        throw new UnsupportedOperationException();
    }
}
