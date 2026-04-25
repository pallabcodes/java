package com.backend.lowlevel;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

/**
 * Step 03: Foreign Memory & Functions (Panama API)
 * 
 * L7 Mastery:
 * 1. Off-heap Memory: Memory managed outside the JVM Heap (no GC tax).
 * 2. Arena: A deterministic lifecycle for memory segments (no manual 'free' errors).
 * 3. Linker: Calling native C functions (Downcalls) without JNI boilerplate.
 */
public class Step03_ForeignMemory {

    public static void demonstrateMemoryAllocation() {
        System.out.println(">>> Off-heap Memory Allocation");
        // Using 'Arena.ofConfined()' ensures memory is freed when the block ends
        try (Arena arena = Arena.ofConfined()) {
            
            // Allocate 1KB off-heap
            MemorySegment segment = arena.allocate(1024);

            // Writing and reading values
            long offset = 0;
            segment.set(ValueLayout.JAVA_INT, offset, 777);
            
            int value = segment.get(ValueLayout.JAVA_INT, offset);
            System.out.println("Retrieved off-heap value: " + value);
        } 
        System.out.println("Off-heap memory freed successfully.");
    }

    public static void demonstrateForeignFunction() throws Throwable {
        System.out.println("\n>>> Foreign Function Call (Linker)");
        
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();
        
        // 1. Call getpid() -> returns int
        Optional<MemorySegment> getpidSymbol = stdlib.find("getpid");
        if (getpidSymbol.isPresent()) {
            MethodHandle getpid = linker.downcallHandle(
                getpidSymbol.get(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            int pid = (int) getpid.invokeExact();
            System.out.println("OS Process ID (via C getpid): " + pid);
        }

        // 2. Call strlen(const char*) -> returns size_t (long)
        Optional<MemorySegment> strlenSymbol = stdlib.find("strlen");
        if (strlenSymbol.isPresent()) {
            MethodHandle strlen = linker.downcallHandle(
                strlenSymbol.get(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
            );

            try (Arena arena = Arena.ofConfined()) {
                // L7 Note: In this JDK version, we use allocateUtf8String
                MemorySegment cString = arena.allocateUtf8String("Java 21 Panama API");
                long len = (long) strlen.invokeExact(cString);
                System.out.println("strlen('Java 21 Panama API'): " + len);
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("=== Step 03: Foreign Memory & Functions (Panama - Java 21) ===");
        demonstrateMemoryAllocation();
        demonstrateForeignFunction();
        
        System.out.println("\nL7 Tip: Panama is the performant successor to JNI, providing mechanical sympathy with native libs.");
    }
}
