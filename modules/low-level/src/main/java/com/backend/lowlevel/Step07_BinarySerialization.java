package com.backend.lowlevel;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Step 07: Binary Serialization (L7 IPC Math)
 * 
 * L7 Principles:
 * 1. Payload Efficiency: Reducing byte size for better networking throughput.
 * 2. CPU Overhead: Standard Java Serialization is slow and insecure.
 * 3. Protobuf/Manual layout: Using 'ByteBuffer' to pack data tightly.
 */
public class Step07_BinarySerialization {

    public record UserPayload(long id, String username) {}

    /**
     * Simulation of a high-performance manual binary serializer.
     */
    public static byte[] serialize(UserPayload user) {
        byte[] nameBytes = user.username().getBytes(StandardCharsets.UTF_8);
        
        // Layout: [ID (8 bytes)] [NameLength (4 bytes)] [NameBytes (N bytes)]
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + nameBytes.length);
        buffer.putLong(user.id());
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        
        return buffer.array();
    }

    public static UserPayload deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        long id = buffer.getLong();
        int nameLen = buffer.getInt();
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);
        
        return new UserPayload(id, new String(nameBytes, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        System.out.println("=== Step 07: Binary Serialization (IPC Math) ===");

        UserPayload original = new UserPayload(42L, "L7-Engineer");
        
        byte[] binaryData = serialize(original);
        System.out.println("Serialized Binary Size: " + binaryData.length + " bytes");

        UserPayload decoded = deserialize(binaryData);
        System.out.println("Decoded User: ID=" + decoded.id() + ", Name=" + decoded.username());

        System.out.println("\nL5 Insight: Manual binary layout is 10x faster and 5x smaller than Java Serialization.");
    }
}
