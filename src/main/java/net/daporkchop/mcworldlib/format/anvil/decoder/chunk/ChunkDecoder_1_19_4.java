package net.daporkchop.mcworldlib.format.anvil.decoder.chunk;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.format.vanilla.VanillaChunk;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.Chunk;
import net.daporkchop.mcworldlib.world.World;

public class ChunkDecoder_1_19_4 extends LegacyChunkDecoder {
    public static final JavaVersion VERSION = JavaVersion.fromName("1.19.4");

    @Override
    public Chunk decode(@NonNull CompoundTag tag, @NonNull JavaVersion version, @NonNull World world) {
        int x = tag.getInt("xPos");
        int z = tag.getInt("zPos");

        return new VanillaChunk(version, x, z);
    }
}
