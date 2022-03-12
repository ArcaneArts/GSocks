package art.arcane.gsocks;

import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

@EqualsAndHashCode(callSuper = true)
@Data
public class GSocksClientBoundConnection extends Thread{
    private static final Gson gson = new Gson();
    private final UUID connectionId;
    private final Socket client;
    private final DataOutputStream clientBound;
    private final DataInputStream serverBound;
    private final Queue<Object> inbox;
    private PacketListener<GSocksClientBoundConnection> handler;

    public GSocksClientBoundConnection(Socket client) throws IOException {
        this.client = client;
        connectionId = UUID.randomUUID();
        clientBound = new DataOutputStream(client.getOutputStream());
        serverBound = new DataInputStream(client.getInputStream());
        inbox = new LinkedBlockingQueue<>();
        start();
    }

    public synchronized void send(Object object) throws IOException {
        clientBound.writeUTF(object.getClass().getCanonicalName());
        clientBound.writeUTF(gson.toJson(object));
        clientBound.flush();
    }

    public void disconnect() throws IOException {
        interrupt();
        client.close();
    }

    public void run()
    {
        while(!Thread.interrupted() && client.isConnected())
        {
            try {
                String type = serverBound.readUTF();
                boolean read = false;
                try
                {
                    Class<?> clazz = Class.forName(type);
                    String data = serverBound.readUTF();
                    read = true;
                    inbox.add(gson.fromJson(data, clazz));
                }

                catch(Throwable e) {
                    if (!read)
                    {
                        serverBound.readUTF();
                    }

                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            while(handler != null && !inbox.isEmpty())
            {
                handler.handlePacket(this, inbox.poll());
            }
        }

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
