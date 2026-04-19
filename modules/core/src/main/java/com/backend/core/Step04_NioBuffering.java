package com.backend.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Step 04: NIO Buffering & Zero-Copy (L7 System Design)
 * 
 * L7 Principles:
 * 1. Kernel vs User Space: Reducing context switches and data copying.
 * 2. Memory-Mapped Files (MMAP): Direct file access via memory addresses.
 * 3. Buffer Management: Direct vs. Heap buffers for high-throughput IO.
 */
public class Step04_NioBuffering {

    public static void demonstrateMmap(Path tempFile) throws IOException {
        String data = "Netflix-Scale Large Data Stream Content...";
        Files.writeString(tempFile, data);

        try (FileChannel channel = FileChannel.open(tempFile, StandardOpenOption.READ)) {
            // Memory Map the file directly into kernel/user space shared memory
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            
            System.out.println("Reading from MMAP Buffer: ");
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }
            System.out.println("\nL5 Insight: Zero-copy eliminates the 'read' sys-call copying step.");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Step 04: NIO Buffering (High Throughput IO) ===");
        
        Path path = Files.createTempFile("nio_demo", ".txt");
        demonstrateMmap(path);
        Files.deleteIfExists(path);

        // Direct vs Heap demonstration
        ByteBuffer direct = ByteBuffer.allocateDirect(1024);
        System.out.println("Direct Buffer allocated: " + direct.isDirect() + " (Native memory outside GC)");
    }
}
