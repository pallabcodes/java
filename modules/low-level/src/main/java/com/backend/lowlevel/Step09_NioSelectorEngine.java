package com.backend.lowlevel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Step 09: NIO Selector Engine (L7 System Architecture)
 * 
 * L7 Principles:
 * 1. Multiplexing: Handling thousands of connections with a single thread.
 * 2. Event-Loop (Reactor Pattern): The core logic powering Zuul, Nginx, and Netty.
 * 3. Non-blocking IO: Avoiding high context-switching costs of thread-per-connection.
 */
public class Step09_NioSelectorEngine {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Step 09: NIO Selector Engine (Event-Loop Mastery) ===");

        // Opening the 'Oracle' of our engine
        Selector selector = Selector.open();

        // Opening a non-blocking TCP server channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost", 8080));
        serverChannel.configureBlocking(false);

        // Register the server with the selector for 'Accept' events
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("L7 Simulator: Event-Loop running on port 8080...");

        // Concept: A single thread spinning and waiting for events (Reactor Pattern)
        // In a real demo, we'd run this in a loop, but here we'll just explain the logic.
        
        System.out.println("\nExecution Loop Logic:");
        System.out.println("1. selector.select() -> wait for OS notification (Epoll/Kqueue).");
        System.out.println("2. SelectionKey[] keys = selector.selectedKeys().");
        System.out.println("3. Iterator over keys -> handle Accept, Read, or Write.");
        
        System.out.println("\nL5 Insight: This is the exact mechanism that allows Netflix's Zuul to handle massive concurrent traffic.");
        
        selector.close();
        serverChannel.close();
    }
}
