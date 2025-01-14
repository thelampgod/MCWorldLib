/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2020-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.mcworldlib.format.anvil.storage;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.common.math.BinMath;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.misc.refcount.AbstractRefCounted;
import net.daporkchop.lib.common.pool.handle.Handle;
import net.daporkchop.lib.common.pool.handle.HandledPool;
import net.daporkchop.lib.compression.context.PInflater;
import net.daporkchop.lib.compression.zlib.Zlib;
import net.daporkchop.lib.compression.zlib.ZlibMode;
import net.daporkchop.lib.compression.zlib.options.ZlibInflaterOptions;
import net.daporkchop.lib.concurrent.PFuture;
import net.daporkchop.lib.concurrent.PFutures;
import net.daporkchop.lib.nbt.NBTFormat;
import net.daporkchop.lib.nbt.NBTOptions;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.lib.primitive.lambda.LongObjObjFunction;
import net.daporkchop.lib.primitive.map.LongObjMap;
import net.daporkchop.lib.primitive.map.concurrent.LongObjConcurrentHashMap;
import net.daporkchop.lib.unsafe.PUnsafe;
import net.daporkchop.lib.unsafe.util.exception.AlreadyReleasedException;
import net.daporkchop.mcworldlib.format.anvil.AnvilSaveOptions;
import net.daporkchop.mcworldlib.format.anvil.world.AbstractAnvilWorld;
import net.daporkchop.mcworldlib.format.anvil.region.RawChunk;
import net.daporkchop.mcworldlib.format.anvil.region.RegionConstants;
import net.daporkchop.mcworldlib.format.anvil.region.RegionFile;
import net.daporkchop.mcworldlib.format.anvil.region.RegionFileCache;
import net.daporkchop.mcworldlib.format.java.JavaFixers;
import net.daporkchop.mcworldlib.format.java.storage.AbstractJavaWorldStorage;
import net.daporkchop.mcworldlib.save.SaveOptions;
import net.daporkchop.mcworldlib.util.WriteAccess;
import net.daporkchop.mcworldlib.util.nbt.AllocatedNBTHelper;
import net.daporkchop.mcworldlib.version.java.DataVersion;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.Chunk;
import net.daporkchop.mcworldlib.world.WorldStorage;
import net.daporkchop.mcworldlib.world.section.Section;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;
import java.util.concurrent.Executor;
import java.util.function.LongFunction;

/**
 * Implementation of {@link WorldStorage} for the Anvil save format.
 * <p>
 * Since Anvil compresses sections and chunks together, but the {@link WorldStorage} interface requires chunks and sections to be handled individually,
 * this interface uses a more complex caching mechanism to improve performance and reduce allocations.
 *
 * @author DaPorkchop_
 */
public class AnvilWorldStorage extends AbstractJavaWorldStorage {
    protected final LongObjMap<AnvilCachedChunk> cachedChunks = new LongObjConcurrentHashMap<>();
    protected final LongFunction<AnvilCachedChunk> loadFunction = l -> {
        try {
            return this.load(BinMath.unpackX(l), BinMath.unpackY(l));
        } catch (IOException e) {
            PUnsafe.throwException(e);
            throw new RuntimeException(e);
        }
    };
    protected final RegionFile regionCache;

    public AnvilWorldStorage(@NonNull File root, @NonNull AbstractAnvilWorld world) {
        super(root, world);

        this.regionCache = new RegionFileCache(world.options(), new File(root, "region"));
    }

    @Override
    public Chunk loadChunk(int x, int z) throws IOException {
        return this.cachedChunks.computeIfAbsent(BinMath.packXY(x, z), this.loadFunction).chunk();
    }

    @Override
    public Section loadSection(int x, int y, int z) throws IOException {
        if (y < 0) {
            y += 24;
        }
        return this.cachedChunks.computeIfAbsent(BinMath.packXY(x, z), this.loadFunction).section(y);
    }

    @Override
    public void save(@NonNull Iterable<Chunk> chunks, @NonNull Iterable<Section> sections) throws IOException {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public PFuture<Void> saveAsync(@NonNull Iterable<Chunk> chunks, @NonNull Iterable<Section> sections) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public PFuture<Void> flushAsync() {
        return PFutures.successful(null, this.ioExecutor);
    }

    @Override
    public Spliterator<Chunk> allChunks() throws IOException {
        return this.readOnly && this.options.get(SaveOptions.SPLITERATOR_CACHE)
                ? new CachedAnvilSpliterator.OfChunk(this)
                : new UncachedAnvilSpliterator.OfChunk(this);
    }

    @Override
    public Spliterator<Section> allSections() throws IOException {
        return this.readOnly && this.options.get(SaveOptions.SPLITERATOR_CACHE)
                ? new CachedAnvilSpliterator.OfSection(this)
                : new UncachedAnvilSpliterator.OfSection(this);
    }

    @Override
    protected void doRelease() {
        try {
            this.flush();
            this.cachedChunks.forEach((l, cached) -> cached.release());
            this.cachedChunks.clear();
            this.regionCache.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected File[] listRegions() {
        return this.regionCache.file().listFiles(f -> f.isFile() && RegionConstants.REGION_PATTERN.matcher(f.getName()).matches());
    }

    /**
     * Loads an entire chunk from disk for caching.
     * <p>
     * If the world is open in read-write mode, all data stored in the chunk will also be upgraded to the world version.
     *
     * @param x the X coordinate of the chunk to cache
     * @param z the Z coordinate of the chunk to cache
     * @return the cached chunk, or {@code null} if the chunk doesn't exist
     */
    protected AnvilCachedChunk load(int x, int z) throws IOException {
        return this.load(this.regionCache, x, z);
    }

    protected boolean prefetch(int x, int z) throws IOException {
        return this.cachedChunks.computeIfAbsent(BinMath.packXY(x, z), this.loadFunction) != null;
    }

    protected AnvilCachedChunk load(@NonNull RegionFile region, int x, int z) throws IOException {
        CompoundTag tag = null;
        try {
            ByteBuf uncompressed = null;
            try {
                try (RawChunk chunk = region.read(x, z)) {
                    if (chunk == null) { //chunk doesn't exist on disk
                        return this.readOnly
                                ? new AnvilCachedChunk.ReadOnlyEmpty()
                                : null; //TODO
                    }
                    uncompressed = this.options.get(SaveOptions.NETTY_ALLOC).ioBuffer(1 << 18); //256 KiB
                    try (Handle<PInflater> handle = inflater(chunk.data().readByte() & 0xFF)) {
                        handle.get().decompress(chunk.data(), uncompressed);
                    }
                } //release compressed chunk data before parsing NBT
                tag = NBTFormat.BIG_ENDIAN.readCompound(DataIn.wrap(uncompressed, false), this.nbtOptions);
            } finally { //release uncompressed chunk data before constructing chunk instance
                if (uncompressed != null) {
                    uncompressed.release();
                }
            }
            int dataVersion = tag.getInt("DataVersion", 0);
            JavaVersion version = dataVersion < DataVersion.DATA_15w32a ? JavaVersion.pre15w32a() : JavaVersion.fromDataVersion(dataVersion);
            return this.readOnly
                    ? new AnvilCachedChunk.ReadOnly(tag, version, this.fixers, this.world)
                    : null; //TODO
        } finally {
            if (tag != null) {
                AllocatedNBTHelper.release(tag);
            }
        }
    }

    public interface ChunkUpdater extends LongObjObjFunction<AnvilCachedChunk, AnvilCachedChunk> {
        @Override
        default AnvilCachedChunk apply(long l, AnvilCachedChunk chunk) {
            try {
                return this.applyThrowing(BinMath.unpackX(l), BinMath.unpackY(l), chunk);
            } catch (IOException e) {
                PUnsafe.throwException(e);
                throw new RuntimeException(e);
            }
        }

        AnvilCachedChunk applyThrowing(int x, int z, AnvilCachedChunk chunk) throws IOException;
    }
}
