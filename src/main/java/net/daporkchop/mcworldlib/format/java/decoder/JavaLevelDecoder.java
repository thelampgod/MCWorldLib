package net.daporkchop.mcworldlib.format.java.decoder;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.version.java.JavaVersion;

/**
 * A function for decoding a level from NBT data.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface JavaLevelDecoder {
    /**
     * Decodes a chunk.
     *
     * @param tag     the root {@link CompoundTag}
     * @param version the version of the chunk data
     * @return the decoded tag
     */
    CompoundTag decode(@NonNull CompoundTag tag, @NonNull JavaVersion version);
}
