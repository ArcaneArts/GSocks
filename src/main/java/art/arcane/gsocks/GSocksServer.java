package art.arcane.gsocks;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class GSocksServer extends Thread {
    private final ServerSocket socket;
    private final Map<UUID, GSocksClientBoundConnection> clients;
    private PacketListener<GSocksClientBoundConnection> handler;

    private GSocksServer(ServerSocket socket) throws SocketException {
        this.socket = socket;
        socket.setSoTimeout(5000);
        clients = new HashMap<>();
        start();
    }

    public void shutdown()
    {
        interrupt();
    }

    public GSocksClientBoundConnection getClient(UUID id)
    {
        return clients.get(id);
    }

    public List<GSocksClientBoundConnection> getClientList()
    {
        return new ArrayList<>(clients.values());
    }

    public void run()
    {
        while(!Thread.interrupted())
        {
            try
            {
                Socket connection = socket.accept();
                GSocksClientBoundConnection client = new GSocksClientBoundConnection(connection);
                clients.put(client.getConnectionId(), client);
                client.setHandler(getHandler());
            }

            catch(SocketTimeoutException ignored) {}

            catch(Throwable e)
            {
                e.printStackTrace();
            }

            for(GSocksClientBoundConnection i : new ArrayList<>(clients.values()))
            {
                if(!i.isAlive() || i.isInterrupted())
                {
                    clients.remove(i.getConnectionId());
                }
            }
        }

        for(GSocksClientBoundConnection i : clients.values())
        {
            try {
                i.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        clients.clear();

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final GSocksServer start(int bindPort) throws IOException {
        return new GSocksServer(new ServerSocket(bindPort));
    }
}
