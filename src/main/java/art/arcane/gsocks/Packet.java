package art.arcane.gsocks;

import com.google.gson.Gson;

public interface Packet {
    void handleClientToServer(GSocksClientBoundConnection receiver);
    void handleServerToClient(GSocksClient receiver);
}
