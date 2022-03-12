package art.arcane.gsocks;

@FunctionalInterface
public interface PacketListener<T> {
    void handlePacket(T receiver, Object packet);
}
