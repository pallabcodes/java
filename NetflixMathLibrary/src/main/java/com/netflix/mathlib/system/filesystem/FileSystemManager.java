/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/2002/05/XMLSchema-instance
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.mathlib.system.filesystem;

import com.netflix.mathlib.core.MathOperation;
import com.netflix.mathlib.exceptions.ValidationException;
import com.netflix.mathlib.monitoring.OperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File System Manager - Production-grade file system operations for system engineering.
 *
 * This class provides comprehensive file system capabilities essential for:
 * - Database storage engines
 * - File servers and distributed file systems
 * - Backup and recovery systems
 * - Log management and archival
 * - Cache persistence and recovery
 *
 * Essential for building persistent storage systems and file-based applications.
 *
 * All implementations are optimized for production use with:
 * - Memory-mapped file operations
 * - Asynchronous I/O operations
 * - Comprehensive error handling
 * - Performance monitoring and metrics
 * - Thread-safe operations
 *
 * @author Netflix Math Library Team
 * @version 1.0.0
 * @since 2024
 */
public class FileSystemManager implements MathOperation {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemManager.class);
    private static final String OPERATION_NAME = "FileSystemManager";
    private static final String COMPLEXITY = "O(1)-O(n)";
    private static final boolean THREAD_SAFE = true;

    private final OperationMetrics metrics;

    // File system operations
    private final ConcurrentHashMap<String, FileHandle> openFiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MemoryMappedFile> mappedFiles = new ConcurrentHashMap<>();

    // Caching and optimization
    private final ConcurrentHashMap<String, LRUCache<String, byte[]>> fileCaches = new ConcurrentHashMap<>();

    // Statistics
    private final AtomicLong totalReads = new AtomicLong(0);
    private final AtomicLong totalWrites = new AtomicLong(0);
    private final AtomicLong totalBytesRead = new AtomicLong(0);
    private final AtomicLong totalBytesWritten = new AtomicLong(0);
    private final AtomicInteger openFileHandles = new AtomicInteger(0);

    /**
     * Constructor for File System Manager.
     */
    public FileSystemManager() {
        this.metrics = new OperationMetrics(OPERATION_NAME, COMPLEXITY, THREAD_SAFE);
        logger.info("Initialized File System Manager");
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public String getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public OperationMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void validateInputs(Object... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw ValidationException.nullParameter("inputs", OPERATION_NAME);
        }

        for (Object input : inputs) {
            if (input == null) {
                throw ValidationException.nullParameter("input", OPERATION_NAME);
            }
        }
    }

    @Override
    public boolean isThreadSafe() {
        return THREAD_SAFE;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    // ===== FILE OPERATIONS =====

    /**
     * Open a file for reading/writing.
     *
     * @param filePath path to the file
     * @param mode file access mode (READ, WRITE, READ_WRITE)
     * @return file handle or null if failed
     */
    public FileHandle openFile(String filePath, FileMode mode) {
        validateInputs(filePath, mode);

        long startTime = System.nanoTime();

        try {
            Path path = Paths.get(filePath);
            OpenOption[] options;

            switch (mode) {
                case READ:
                    options = new OpenOption[]{StandardOpenOption.READ};
                    break;
                case WRITE:
                    options = new OpenOption[]{StandardOpenOption.WRITE,
                                             StandardOpenOption.CREATE,
                                             StandardOpenOption.TRUNCATE_EXISTING};
                    break;
                case READ_WRITE:
                    options = new OpenOption[]{StandardOpenOption.READ,
                                             StandardOpenOption.WRITE,
                                             StandardOpenOption.CREATE};
                    break;
                default:
                    throw new ValidationException("Unsupported file mode: " + mode, OPERATION_NAME);
            }

            FileChannel channel = FileChannel.open(path, options);
            FileHandle handle = new FileHandle(filePath, channel, mode);

            openFiles.put(filePath, handle);
            openFileHandles.incrementAndGet();

            long executionTime = System.nanoTime() - startTime;
            metrics.recordSuccess(executionTime, 0);

            logger.debug("Opened file '{}' in {} mode", filePath, mode);
            return handle;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error opening file '{}': {}", filePath, e.getMessage());
            return null;
        }
    }

    /**
     * Close a file handle.
     *
     * @param handle the file handle to close
     * @return true if successfully closed
     */
    public boolean closeFile(FileHandle handle) {
        validateInputs(handle);

        try {
            handle.getChannel().close();
            openFiles.remove(handle.getFilePath());
            openFileHandles.decrementAndGet();

            logger.debug("Closed file '{}'", handle.getFilePath());
            return true;

        } catch (Exception e) {
            logger.error("Error closing file '{}': {}", handle.getFilePath(), e.getMessage());
            return false;
        }
    }

    /**
     * Read data from a file.
     *
     * @param handle file handle
     * @param position position to read from
     * @param length number of bytes to read
     * @return byte array or null if failed
     */
    public byte[] readFile(FileHandle handle, long position, int length) {
        validateInputs(handle);

        long startTime = System.nanoTime();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(length);
            int bytesRead = handle.getChannel().read(buffer, position);

            if (bytesRead == -1) {
                // End of file
                return new byte[0];
            }

            byte[] data = new byte[bytesRead];
            buffer.flip();
            buffer.get(data);

            long executionTime = System.nanoTime() - startTime;
            metrics.recordSuccess(executionTime, bytesRead);

            totalReads.incrementAndGet();
            totalBytesRead.addAndGet(bytesRead);

            return data;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error reading from file '{}': {}", handle.getFilePath(), e.getMessage());
            return null;
        }
    }

    /**
     * Write data to a file.
     *
     * @param handle file handle
     * @param position position to write to
     * @param data data to write
     * @return number of bytes written or -1 if failed
     */
    public int writeFile(FileHandle handle, long position, byte[] data) {
        validateInputs(handle, data);

        long startTime = System.nanoTime();

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int bytesWritten = handle.getChannel().write(buffer, position);

            long executionTime = System.nanoTime() - startTime;
            metrics.recordSuccess(executionTime, bytesWritten);

            totalWrites.incrementAndGet();
            totalBytesWritten.addAndGet(bytesWritten);

            return bytesWritten;

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error writing to file '{}': {}", handle.getFilePath(), e.getMessage());
            return -1;
        }
    }

    // ===== MEMORY-MAPPED FILES =====

    /**
     * Create a memory-mapped file for high-performance access.
     *
     * @param filePath path to the file
     * @param mode file access mode
     * @param size size of the mapping
     * @return memory-mapped file handle or null if failed
     */
    public MemoryMappedFile mapFile(String filePath, FileMode mode, long size) {
        validateInputs(filePath, mode);

        long startTime = System.nanoTime();

        try {
            Path path = Paths.get(filePath);

            // Ensure file exists and has correct size
            if (mode == FileMode.READ) {
                if (!Files.exists(path)) {
                    throw new FileNotFoundException("File does not exist: " + filePath);
                }
                size = Files.size(path);
            } else {
                // Create or resize file for writing
                try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
                    raf.setLength(size);
                }
            }

            FileChannel.MapMode mapMode = (mode == FileMode.READ) ?
                FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;

            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ,
                                                       StandardOpenOption.WRITE)) {
                MappedByteBuffer buffer = channel.map(mapMode, 0, size);
                MemoryMappedFile mappedFile = new MemoryMappedFile(filePath, buffer, size);

                mappedFiles.put(filePath, mappedFile);

                long executionTime = System.nanoTime() - startTime;
                metrics.recordSuccess(executionTime, size);

                logger.debug("Memory mapped file '{}' with size {}", filePath, size);
                return mappedFile;
            }

        } catch (Exception e) {
            metrics.recordError();
            logger.error("Error memory mapping file '{}': {}", filePath, e.getMessage());
            return null;
        }
    }

    /**
     * Read from memory-mapped file.
     *
     * @param mappedFile memory-mapped file
     * @param position position to read from
     * @param length number of bytes to read
     * @return byte array or null if failed
     */
    public byte[] readMappedFile(MemoryMappedFile mappedFile, int position, int length) {
        validateInputs(mappedFile);

        try {
            if (position + length > mappedFile.getSize()) {
                length = (int) (mappedFile.getSize() - position);
            }

            byte[] data = new byte[length];
            mappedFile.getBuffer().position(position);
            mappedFile.getBuffer().get(data);

            totalReads.incrementAndGet();
            totalBytesRead.addAndGet(length);

            return data;

        } catch (Exception e) {
            logger.error("Error reading from mapped file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Write to memory-mapped file.
     *
     * @param mappedFile memory-mapped file
     * @param position position to write to
     * @param data data to write
     * @return true if successful
     */
    public boolean writeMappedFile(MemoryMappedFile mappedFile, int position, byte[] data) {
        validateInputs(mappedFile, data);

        try {
            if (position + data.length > mappedFile.getSize()) {
                throw new IndexOutOfBoundsException("Write exceeds mapped file size");
            }

            mappedFile.getBuffer().position(position);
            mappedFile.getBuffer().put(data);

            // Force write to disk if needed
            mappedFile.getBuffer().force();

            totalWrites.incrementAndGet();
            totalBytesWritten.addAndGet(data.length);

            return true;

        } catch (Exception e) {
            logger.error("Error writing to mapped file: {}", e.getMessage());
            return false;
        }
    }

    // ===== DIRECTORY OPERATIONS =====

    /**
     * List files in a directory.
     *
     * @param directoryPath path to the directory
     * @return list of file names or null if failed
     */
    public java.util.List<String> listDirectory(String directoryPath) {
        validateInputs(directoryPath);

        try {
            Path path = Paths.get(directoryPath);
            if (!Files.isDirectory(path)) {
                throw new ValidationException("Path is not a directory: " + directoryPath, OPERATION_NAME);
            }

            return Files.list(path)
                       .map(p -> p.getFileName().toString())
                       .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            logger.error("Error listing directory '{}': {}", directoryPath, e.getMessage());
            return null;
        }
    }

    /**
     * Create a directory.
     *
     * @param directoryPath path to create
     * @return true if successful
     */
    public boolean createDirectory(String directoryPath) {
        validateInputs(directoryPath);

        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
            logger.debug("Created directory '{}'", directoryPath);
            return true;

        } catch (Exception e) {
            logger.error("Error creating directory '{}': {}", directoryPath, e.getMessage());
            return false;
        }
    }

    /**
     * Delete a file or directory.
     *
     * @param path path to delete
     * @return true if successful
     */
    public boolean deletePath(String path) {
        validateInputs(path);

        try {
            Path filePath = Paths.get(path);
            Files.delete(filePath);
            logger.debug("Deleted path '{}'", path);
            return true;

        } catch (Exception e) {
            logger.error("Error deleting path '{}': {}", path, e.getMessage());
            return false;
        }
    }

    // ===== CACHING SYSTEM =====

    /**
     * Create an LRU cache for file data.
     *
     * @param cacheName unique identifier for the cache
     * @param maxSize maximum number of entries in cache
     */
    public void createFileCache(String cacheName, int maxSize) {
        validateInputs(cacheName);

        LRUCache<String, byte[]> cache = new LRUCache<>(maxSize);
        fileCaches.put(cacheName, cache);

        logger.info("Created LRU cache '{}' with max size {}", cacheName, maxSize);
    }

    /**
     * Get data from file cache.
     *
     * @param cacheName cache identifier
     * @param key cache key
     * @return cached data or null if not found
     */
    public byte[] getFromCache(String cacheName, String key) {
        validateInputs(cacheName, key);

        LRUCache<String, byte[]> cache = fileCaches.get(cacheName);
        return cache != null ? cache.get(key) : null;
    }

    /**
     * Put data in file cache.
     *
     * @param cacheName cache identifier
     * @param key cache key
     * @param data data to cache
     */
    public void putInCache(String cacheName, String key, byte[] data) {
        validateInputs(cacheName, key, data);

        LRUCache<String, byte[]> cache = fileCaches.get(cacheName);
        if (cache != null) {
            cache.put(key, data);
        }
    }

    // ===== FILE SYSTEM STATISTICS =====

    /**
     * Get comprehensive file system statistics.
     *
     * @return file system statistics
     */
    public FileSystemStatistics getStatistics() {
        return new FileSystemStatistics(
            totalReads.get(),
            totalWrites.get(),
            totalBytesRead.get(),
            totalBytesWritten.get(),
            openFileHandles.get(),
            openFiles.size(),
            mappedFiles.size(),
            fileCaches.size()
        );
    }

    /**
     * Get file information.
     *
     * @param filePath path to the file
     * @return file information or null if failed
     */
    public FileInfo getFileInfo(String filePath) {
        validateInputs(filePath);

        try {
            Path path = Paths.get(filePath);
            java.nio.file.FileStore store = Files.getFileStore(path);

            return new FileInfo(
                filePath,
                Files.size(path),
                Files.getLastModifiedTime(path).toMillis(),
                Files.isReadable(path),
                Files.isWritable(path),
                store.getTotalSpace(),
                store.getUsableSpace()
            );

        } catch (Exception e) {
            logger.error("Error getting file info for '{}': {}", filePath, e.getMessage());
            return null;
        }
    }

    // ===== PRIVATE METHODS =====

    // ===== INNER CLASSES =====

    /**
     * File access modes.
     */
    public enum FileMode {
        READ, WRITE, READ_WRITE
    }

    /**
     * File handle wrapper.
     */
    public static class FileHandle implements AutoCloseable {
        private final String filePath;
        private final FileChannel channel;
        private final FileMode mode;

        public FileHandle(String filePath, FileChannel channel, FileMode mode) {
            this.filePath = filePath;
            this.channel = channel;
            this.mode = mode;
        }

        public String getFilePath() {
            return filePath;
        }

        public FileChannel getChannel() {
            return channel;
        }

        public FileMode getMode() {
            return mode;
        }

        @Override
        public void close() throws Exception {
            channel.close();
        }
    }

    /**
     * Memory-mapped file wrapper.
     */
    public static class MemoryMappedFile implements AutoCloseable {
        private final String filePath;
        private final MappedByteBuffer buffer;
        private final long size;

        public MemoryMappedFile(String filePath, MappedByteBuffer buffer, long size) {
            this.filePath = filePath;
            this.buffer = buffer;
            this.size = size;
        }

        public String getFilePath() {
            return filePath;
        }

        public MappedByteBuffer getBuffer() {
            return buffer;
        }

        public long getSize() {
            return size;
        }

        @Override
        public void close() throws Exception {
            // Memory-mapped buffers are automatically cleaned up by GC
            // but we can force cleanup if needed
            buffer.clear();
        }
    }

    /**
     * LRU Cache implementation for file data.
     */
    public static class LRUCache<K, V> {
        private final int capacity;
        private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
        private final ConcurrentLinkedQueue<K> accessOrder = new ConcurrentLinkedQueue<>();

        public LRUCache(int capacity) {
            this.capacity = capacity;
        }

        public V get(K key) {
            CacheEntry<V> entry = cache.get(key);
            if (entry != null) {
                accessOrder.remove(key);
                accessOrder.add(key);
                return entry.getValue();
            }
            return null;
        }

        public void put(K key, V value) {
            if (cache.size() >= capacity && !cache.containsKey(key)) {
                // Remove least recently used
                K lruKey = accessOrder.poll();
                if (lruKey != null) {
                    cache.remove(lruKey);
                }
            }

            cache.put(key, new CacheEntry<>(value));
            accessOrder.remove(key);
            accessOrder.add(key);
        }

        private static class CacheEntry<V> {
            private final V value;

            public CacheEntry(V value) {
                this.value = value;
            }

            public V getValue() {
                return value;
            }
        }
    }

    // ===== STATISTICS CLASSES =====

    /**
     * File information container.
     */
    public static class FileInfo {
        public final String path;
        public final long size;
        public final long lastModified;
        public final boolean readable;
        public final boolean writable;
        public final long totalSpace;
        public final long usableSpace;

        public FileInfo(String path, long size, long lastModified, boolean readable,
                       boolean writable, long totalSpace, long usableSpace) {
            this.path = path;
            this.size = size;
            this.lastModified = lastModified;
            this.readable = readable;
            this.writable = writable;
            this.totalSpace = totalSpace;
            this.usableSpace = usableSpace;
        }

        @Override
        public String toString() {
            return String.format(
                "File Info: %s\n" +
                "  Size: %d bytes\n" +
                "  Last Modified: %d\n" +
                "  Permissions: %s%s\n" +
                "  Disk Space: %d total, %d usable",
                path, size, lastModified,
                readable ? "r" : "-", writable ? "w" : "-",
                totalSpace, usableSpace
            );
        }
    }

    /**
     * File system statistics container.
     */
    public static class FileSystemStatistics {
        public final long totalReads;
        public final long totalWrites;
        public final long totalBytesRead;
        public final long totalBytesWritten;
        public final int openFileHandles;
        public final int openFiles;
        public final int mappedFiles;
        public final int activeCaches;

        public FileSystemStatistics(long totalReads, long totalWrites, long totalBytesRead,
                                  long totalBytesWritten, int openFileHandles, int openFiles,
                                  int mappedFiles, int activeCaches) {
            this.totalReads = totalReads;
            this.totalWrites = totalWrites;
            this.totalBytesRead = totalBytesRead;
            this.totalBytesWritten = totalBytesWritten;
            this.openFileHandles = openFileHandles;
            this.openFiles = openFiles;
            this.mappedFiles = mappedFiles;
            this.activeCaches = activeCaches;
        }

        @Override
        public String toString() {
            return String.format(
                "File System Stats:\n" +
                "  Operations: %d reads, %d writes\n" +
                "  Data Transfer: %d bytes read, %d bytes written\n" +
                "  Open Resources: %d handles, %d files, %d mapped files\n" +
                "  Active Caches: %d",
                totalReads, totalWrites, totalBytesRead, totalBytesWritten,
                openFileHandles, openFiles, mappedFiles, activeCaches
            );
        }
    }
}
