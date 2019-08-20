/*
 * Copyright 2019 Mark Adamcin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.adamcin.oakpal.core;

import org.apache.jackrabbit.oak.api.Blob;
import org.apache.jackrabbit.oak.plugins.blob.BlobStoreBlob;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.apache.jackrabbit.oak.spi.blob.FileBlobStore;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.spi.state.ProxyNodeStore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Custom node store which combines a MemoryNodeStore with a FileBlobStore.
 */
public final class MemoryNodeStoreWithFileBlobStore extends ProxyNodeStore {

    private final NodeStore nodeStore;
    private final FileBlobStore blobStore;

    private MemoryNodeStoreWithFileBlobStore(File tempDir) {
        this(tempDir.getAbsolutePath());
    }

    private MemoryNodeStoreWithFileBlobStore(String tempDirPath) {
        this.nodeStore = new MemoryNodeStore();
        this.blobStore = new FileBlobStore(tempDirPath);
    }

    @Override
    protected NodeStore getNodeStore() {
        return nodeStore;
    }

    @Override
    public Blob createBlob(InputStream inputStream) throws IOException {
        return new BlobStoreBlob(blobStore, blobStore.writeBlob(inputStream));
    }

    @Override
    public Blob getBlob(@NotNull String reference) {
        return new BlobStoreBlob(blobStore, reference);
    }

    /**
     * Create a Supplier for use with OakMachine which supplies a new NodeStore instance.
     *
     * Note - cleaning up the temp directory is the responsibility of the caller.
     *
     * @param tempDir the path for the blob store
     * @return a Supplier
     */
    public static Supplier<NodeStore> createSupplier(File tempDir) {
        return () -> new MemoryNodeStoreWithFileBlobStore(tempDir);
    }

    /**
     * Create a Supplier for use with OakMachine which supplies a new NodeStore instance.
     *
     * Note - cleaning up the temp directory is the responsibility of the caller.
     *
     * @param tempDirPath the path for the blob store
     * @return a Supplier
     */
    public static Supplier<NodeStore> createSupplier(String tempDirPath) {
        return () -> new MemoryNodeStoreWithFileBlobStore(tempDirPath);
    }

}