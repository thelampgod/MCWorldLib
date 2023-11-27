package net.daporkchop.mcworldlib.format.java.decoder.level;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.version.java.JavaVersion;

public class LevelDecoder1_19_4 extends LegacyLevelDecoder {

    public static final JavaVersion VERSION = JavaVersion.fromName("1.19.4");

    @Override
    public CompoundTag decode(@NonNull CompoundTag tag, @NonNull JavaVersion version) {
        return tag;
    }
}
