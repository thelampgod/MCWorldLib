package net.daporkchop.mcworldlib.format.java.decoder.section;

import lombok.NonNull;
import net.daporkchop.lib.binary.bit.padded.PaddedBitArray;
import net.daporkchop.lib.common.math.BinMath;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.lib.nbt.tag.ListTag;
import net.daporkchop.mcworldlib.format.common.nibble.NibbleArray;
import net.daporkchop.mcworldlib.format.common.section.flattened.SingleLayerFlattenedSection;
import net.daporkchop.mcworldlib.format.common.storage.flattened.HeapPaddedFlattenedBlockStorage;
import net.daporkchop.mcworldlib.util.nbt.AllocatedLongArrayTag;
import net.daporkchop.mcworldlib.util.palette.state.StatePalette;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.World;
import net.daporkchop.mcworldlib.world.section.Section;
import net.daporkchop.mcworldlib.world.storage.FlattenedBlockStorage;

public class SectionDecoder1_19_4 extends PackedFlattenedSectionDecoder {
    public static final JavaVersion VERSION = JavaVersion.fromName("1.19.4");

    @Override
    public Section decode(CompoundTag tag, JavaVersion version, World world, int x, int z) {
        int y = tag.getByte("Y") & 0xFF;
        FlattenedBlockStorage blocks = this.parseBlockStorage(tag);

        NibbleArray blockLight = this.parseNibbleArray(tag, "BlockLight");
        NibbleArray skyLight = this.parseNibbleArray(tag, "SkyLight");
        return new SingleLayerFlattenedSection(version, x, y, z, blocks, blockLight, skyLight);
    }

    @Override
    protected FlattenedBlockStorage parseBlockStorage(@NonNull CompoundTag tag) {
        CompoundTag block_states = tag.getCompound("block_states");
        ListTag<CompoundTag> paletteTag = block_states.getList("palette", CompoundTag.class);
        AllocatedLongArrayTag blockStatesTag = block_states.remove("data");

        int bits = Math.max(BinMath.getNumBitsNeededFor(paletteTag.size()), 4);
        StatePalette palette = this.parseBlockPalette(bits, paletteTag);

        return new HeapPaddedFlattenedBlockStorage(new PaddedBitArray(bits, 4096, blockStatesTag.value(), blockStatesTag.alloc()), palette);
    }
}