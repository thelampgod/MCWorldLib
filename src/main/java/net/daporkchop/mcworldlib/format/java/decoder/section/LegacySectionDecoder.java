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

package net.daporkchop.mcworldlib.format.java.decoder.section;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.ByteArrayTag;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.format.common.nibble.HeapNibbleArray;
import net.daporkchop.mcworldlib.format.common.nibble.NibbleArray;
import net.daporkchop.mcworldlib.format.common.section.legacy.DefaultLegacySection;
import net.daporkchop.mcworldlib.format.common.storage.legacy.HeapLegacyBlockStorage;
import net.daporkchop.mcworldlib.format.java.decoder.JavaSectionDecoder;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.World;
import net.daporkchop.mcworldlib.world.section.Section;
import net.daporkchop.mcworldlib.world.storage.LegacyBlockStorage;

/**
 * @author DaPorkchop_
 */
public class LegacySectionDecoder implements JavaSectionDecoder {
    public static final JavaVersion VERSION = JavaVersion.fromName("1.12.2");

    @Override
    public Section decode(@NonNull CompoundTag tag, @NonNull JavaVersion version, @NonNull World world, int x, int z) {
        int y = tag.getByte("Y") & 0xFF;
        LegacyBlockStorage blocks = this.parseBlockStorage(tag);

        NibbleArray blockLight = this.parseNibbleArray(tag, "BlockLight");
        NibbleArray skyLight = this.parseNibbleArray(tag, "SkyLight");
        return new DefaultLegacySection(version, x, y, z, blocks, blockLight, skyLight);
    }

    protected LegacyBlockStorage parseBlockStorage(@NonNull CompoundTag tag) {
        ByteArrayTag blocksTag = tag.getTag("Blocks");
        ByteArrayTag dataTag = tag.getTag("Data");
        ByteArrayTag addTag = tag.getTag("Add", null);
        if (addTag == null) {
            if (blocksTag.handle() != null) { //assume that all tags have handles
                return new HeapLegacyBlockStorage(blocksTag.handle(), dataTag.handle());
            } else {
                return new HeapLegacyBlockStorage(blocksTag.value(), 0, dataTag.value(), 0);
            }
        } else {
            if (blocksTag.handle() != null) { //assume that all tags have handles
                return new HeapLegacyBlockStorage.Add(blocksTag.handle(), dataTag.handle(), addTag.handle());
            } else {
                return new HeapLegacyBlockStorage.Add(blocksTag.value(), 0, dataTag.value(), 0, addTag.value(), 0);
            }
        }
    }

    protected NibbleArray parseNibbleArray(@NonNull CompoundTag tag, @NonNull String name) {
        ByteArrayTag data = tag.getTag(name, null);
        if (data == null) {
            return null;
        }
        return data.handle() != null
                ? new HeapNibbleArray.YZX(data.handle())
                : new HeapNibbleArray.YZX(data.value(), 0);
    }
}
