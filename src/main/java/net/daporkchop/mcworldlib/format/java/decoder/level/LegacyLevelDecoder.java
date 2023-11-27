package net.daporkchop.mcworldlib.format.java.decoder.level;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.format.java.decoder.JavaLevelDecoder;
import net.daporkchop.mcworldlib.version.java.JavaVersion;

public class LegacyLevelDecoder implements JavaLevelDecoder {
    public static final JavaVersion VERSION = JavaVersion.fromName("1.16.1");

    @Override
    public CompoundTag decode(@NonNull CompoundTag tag, @NonNull JavaVersion version) {
        return tag.getCompound("Level");
    }
}