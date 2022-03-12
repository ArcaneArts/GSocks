package art.arcane.gsocks;

import com.google.gson.Gson;
import lombok.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class GSocksClient extends Thread{
    private static final Gson gson = new Gson();
    private final Socket socket;
    private final DataOutputStream serverBound;
    private final DataInputStream clientBound;
    private final Queue<Object> inbox;
    private PacketListener<GSocksClient> handler;

    public GSocksClient(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.serverBound = new DataOutputStream(socket.getOutputStream());
        this.clientBound = new DataInputStream(socket.getInputStream());
        this.inbox = new LinkedBlockingQueue<>();
        start();
    }

    public synchronized void send(Object object) throws IOException {
        serverBound.writeUTF(object.getClass().getCanonicalName());
        serverBound.writeUTF(gson.toJson(object));
        serverBound.flush();
    }

    public void run()
    {
        while(!Thread.interrupted() && socket.isConnected())
        {
            try {
                String type = clientBound.readUTF();
                boolean read = false;
                try
                {
                    Class<?> clazz = Class.forName(type);
                    String data = clientBound.readUTF();
                    read = true;
                    inbox.add(gson.fromJson(data, clazz));
                }

                catch(Throwable e) {
                    if (!read)
                    {
                        clientBound.readUTF();
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
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
