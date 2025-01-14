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

package net.daporkchop.mcworldlib.format.common.section.flattened;

import lombok.NonNull;
import net.daporkchop.lib.unsafe.util.exception.AlreadyReleasedException;
import net.daporkchop.mcworldlib.block.BlockState;
import net.daporkchop.mcworldlib.format.common.nibble.NibbleArray;
import net.daporkchop.mcworldlib.format.common.section.AbstractSection;
import net.daporkchop.mcworldlib.version.MinecraftVersion;
import net.daporkchop.mcworldlib.world.section.FlattenedSection;
import net.daporkchop.mcworldlib.world.storage.FlattenedBlockStorage;

/**
 * @author DaPorkchop_
 */
public class SingleLayerFlattenedSection extends AbstractSection implements FlattenedSection {
    protected final FlattenedBlockStorage blocks;

    public SingleLayerFlattenedSection(MinecraftVersion version, int x, int y, int z, @NonNull FlattenedBlockStorage blocks, NibbleArray blockLight, NibbleArray skyLight) {
        super(version, x, y, z, blockLight, skyLight);

        this.blocks = blocks;
    }

    @Override
    public FlattenedSection retain() throws AlreadyReleasedException {
        super.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        super.doRelease();

        this.blocks.release();
    }

    @Override
    public FlattenedBlockStorage blockStorage() {
        return this.blocks;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return this.blocks.getBlockState(x, y, z);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState state) {
        this.blocks.setBlockState(x, y, z, state);
    }
}
