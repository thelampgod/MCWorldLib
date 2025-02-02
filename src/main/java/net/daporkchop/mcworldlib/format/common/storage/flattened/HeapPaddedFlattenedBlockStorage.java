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

package net.daporkchop.mcworldlib.format.common.storage.flattened;

import lombok.NonNull;
import net.daporkchop.lib.binary.bit.BitArray;
import net.daporkchop.lib.binary.bit.padded.PaddedBitArray;
import net.daporkchop.lib.common.math.PMath;
import net.daporkchop.lib.common.pool.array.ArrayAllocator;
import net.daporkchop.mcworldlib.format.java.storage.NullablePaddedBitArray;
import net.daporkchop.mcworldlib.util.palette.state.StatePalette;
import net.daporkchop.mcworldlib.world.storage.BlockStorage;
import net.daporkchop.mcworldlib.world.storage.FlattenedBlockStorage;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link BlockStorage} which uses a palette.
 *
 * @author DaPorkchop_
 */
public class HeapPaddedFlattenedBlockStorage extends AbstractHeapFlattenedBlockStorage {
    public HeapPaddedFlattenedBlockStorage() {
        super();
    }

    public HeapPaddedFlattenedBlockStorage(ArrayAllocator<long[]> alloc) {
        super(alloc);
    }

    public HeapPaddedFlattenedBlockStorage(@NonNull BitArray array, @NonNull StatePalette palette) {
        super(array, palette);
    }


    public HeapPaddedFlattenedBlockStorage(ArrayAllocator<long[]> alloc, @NonNull BitArray array, @NonNull StatePalette palette) {
        super(alloc, array, palette);
    }

    @Override
    protected BitArray createArray() {
        return this.alloc != null
                ? new PaddedBitArray(this.bits, 4096, this.alloc.atLeast(toInt(PMath.roundUp(4096L * (long) this.bits, 64L) >>> 6L)))
                : new PaddedBitArray(this.bits, 4096);
    }

    @Override
    public FlattenedBlockStorage clone() {
        throw new UnsupportedOperationException(); //TODO
    }
}
