package net.daporkchop.mcworldlib.format.java.storage;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.daporkchop.lib.binary.bit.BitArray;
import net.daporkchop.lib.common.misc.refcount.AbstractRefCounted;
import net.daporkchop.lib.common.pool.array.ArrayAllocator;
import net.daporkchop.lib.unsafe.util.exception.AlreadyReleasedException;

import static net.daporkchop.lib.common.util.PValidation.*;

@Getter
@Accessors(fluent = true)
public class NullablePaddedBitArray extends AbstractRefCounted implements BitArray {
    protected static final int[] MASK_LOOKUP = {
            0XFFFFFFFF, 0XFFFFFFFF, 0X00000000,
            0X80000000, 0X00000000, 0X00000000,
            0X55555555, 0X55555555, 0X00000000,
            0X80000000, 0X00000000, 0X00000001,
            0X33333333, 0X33333333, 0X00000000,
            0X2AAAAAAA, 0X2AAAAAAA, 0X00000000,
            0X24924924, 0X24924924, 0X00000000,
            0X80000000, 0X00000000, 0X00000002,
            0X1C71C71C, 0X1C71C71C, 0X00000000,
            0X19999999, 0X19999999, 0X00000000,
            0X1745D174, 0X1745D174, 0X00000000,
            0X15555555, 0X15555555, 0X00000000,
            0X13B13B13, 0X13B13B13, 0X00000000,
            0X12492492, 0X12492492, 0X00000000,
            0X11111111, 0X11111111, 0X00000000,
            0X80000000, 0X00000000, 0X00000003,
            0X0F0F0F0F, 0X0F0F0F0F, 0X00000000,
            0X0E38E38E, 0X0E38E38E, 0X00000000,
            0X0D79435E, 0X0D79435E, 0X00000000,
            0X0CCCCCCC, 0X0CCCCCCC, 0X00000000,
            0X0C30C30C, 0X0C30C30C, 0X00000000,
            0X0BA2E8BA, 0X0BA2E8BA, 0X00000000,
            0X0B21642C, 0X0B21642C, 0X00000000,
            0X0AAAAAAA, 0X0AAAAAAA, 0X00000000,
            0X0A3D70A3, 0X0A3D70A3, 0X00000000,
            0X09D89D89, 0X09D89D89, 0X00000000,
            0X097B425E, 0X097B425E, 0X00000000,
            0X09249249, 0X09249249, 0X00000000,
            0X08D3DCB0, 0X08D3DCB0, 0X00000000,
            0X08888888, 0X08888888, 0X00000000,
            0X08421084, 0X08421084, 0X00000000,
            0X80000000, 0X00000000, 0X00000004,
            0X07C1F07C, 0X07C1F07C, 0X00000000,
            0X07878787, 0X07878787, 0X00000000,
            0X07507507, 0X07507507, 0X00000000,
            0X071C71C7, 0X071C71C7, 0X00000000,
            0X06EB3E45, 0X06EB3E45, 0X00000000,
            0X06BCA1AF, 0X06BCA1AF, 0X00000000,
            0X06906906, 0X06906906, 0X00000000,
            0X06666666, 0X06666666, 0X00000000,
            0X063E7063, 0X063E7063, 0X00000000,
            0X06186186, 0X06186186, 0X00000000,
            0X05F417D0, 0X05F417D0, 0X00000000,
            0X05D1745D, 0X05D1745D, 0X00000000,
            0X05B05B05, 0X05B05B05, 0X00000000,
            0X0590B216, 0X0590B216, 0X00000000,
            0X0572620A, 0X0572620A, 0X00000000,
            0X05555555, 0X05555555, 0X00000000,
            0X05397829, 0X05397829, 0X00000000,
            0X051EB851, 0X051EB851, 0X00000000,
            0X05050505, 0X05050505, 0X00000000,
            0X04EC4EC4, 0X04EC4EC4, 0X00000000,
            0X04D4873E, 0X04D4873E, 0X00000000,
            0X04BDA12F, 0X04BDA12F, 0X00000000,
            0X04A7904A, 0X04A7904A, 0X00000000,
            0X04924924, 0X04924924, 0X00000000,
            0X047DC11F, 0X047DC11F, 0X00000000,
            0X0469EE58, 0X0469EE58, 0X00000000,
            0X0456C797, 0X0456C797, 0X00000000,
            0X04444444, 0X04444444, 0X00000000,
            0X04325C53, 0X04325C53, 0X00000000,
            0X04210842, 0X04210842, 0X00000000,
            0X04104104, 0X04104104, 0X00000000,
            0X80000000, 0X00000000, 0X00000005
    };

    protected static int minLength(int bits, int size) {
        checkIndex(bits > 0 && bits <= 32, "bits (%d) must be in range [1-32]", bits);
        notNegative(size, "size");
        return (size + (Long.SIZE / bits) - 1) / (Long.SIZE / bits);
    }

    protected final long[] internalDataArray;

    protected final int bits;
    protected final int size;

    protected final int indexScale;

    private final int indexFactor;
    private final int indexOffset;
    private final int indexShift;

    protected final ArrayAllocator<long[]> alloc;

    public NullablePaddedBitArray(int bits, int size) {
        this(bits, size, new long[minLength(bits, size)], null);
    }

    public NullablePaddedBitArray(int bits, int size, long[] arr) {
        this(bits, size, arr, null);
    }

    public NullablePaddedBitArray(int bits, int size, long[] arr, ArrayAllocator<long[]> alloc) {
        int minLength = minLength(bits, size);
        if (arr != null) {
            checkArg(arr.length >= minLength, "length (%d) must be at least %d!", arr.length, minLength);
        }

        this.indexScale = Long.SIZE / bits;
        int i = 3 * (this.indexScale - 1);
        this.indexFactor = MASK_LOOKUP[i + 0];
        this.indexOffset = MASK_LOOKUP[i + 1];
        this.indexShift = MASK_LOOKUP[i + 2];

        this.internalDataArray = arr;
        this.alloc = alloc;
        this.bits = bits;
        this.size = size;
    }

    public NullablePaddedBitArray(int bits, int size, @NonNull ArrayAllocator<long[]> alloc) {
        this.internalDataArray = alloc.atLeast(minLength(bits, size));
        this.alloc = alloc;

        this.indexScale = Long.SIZE / bits;
        int i = 3 * (this.indexScale - 1);
        this.indexFactor = MASK_LOOKUP[i + 0];
        this.indexOffset = MASK_LOOKUP[i + 1];
        this.indexShift = MASK_LOOKUP[i + 2];

        this.bits = bits;
        this.size = size;
    }

    private int toInternalIndex(int rawIndex) {
        long factor = Integer.toUnsignedLong(this.indexFactor);
        long offset = Integer.toUnsignedLong(this.indexOffset);
        return (int) ((long) rawIndex * factor + offset >> 32 >> this.indexShift);
    }

    @Override
    public int get(int i) {
        if (this.internalDataArray == null) {
            return 0;
        }
        final long[] arr = this.internalDataArray;
        final int bits = this.bits;
        final int size = this.size;
        checkIndex(size, i);

        int internalIndex = this.toInternalIndex(i);
        int relPos = (i - internalIndex * this.indexScale) * this.bits;
        return (int) ((arr[internalIndex] >> relPos) & ((1 << bits) - 1));
    }

    @Override
    public void set(int i, int value) {
        if (this.internalDataArray == null) {
            throw new UnsupportedOperationException();
        }
        final long[] arr = this.internalDataArray;
        final int bits = this.bits;
        final int size = this.size;
        checkIndex(size, i);
        int mask = (1 << bits) - 1;
        checkArg((value & mask) == value, value);

        int internalIndex = this.toInternalIndex(i);
        int relPos = (i - internalIndex * this.indexScale) * this.bits;
        arr[internalIndex] = (arr[internalIndex] & ~((long) mask << relPos)) | ((long) value << relPos);
    }

    @Override
    public int replace(int i, int value) {
        if (this.internalDataArray == null) {
            throw new UnsupportedOperationException();
        }
        final long[] arr = this.internalDataArray;
        final int bits = this.bits;
        final int size = this.size;
        checkIndex(size, i);
        int mask = (1 << bits) - 1;
        checkArg((value & mask) == value, value);

        int internalIndex = this.toInternalIndex(i);
        int relPos = (i - internalIndex * this.indexScale) * this.bits;
        int old = (int) ((arr[internalIndex] >> relPos) & mask);
        arr[internalIndex] = (arr[internalIndex] & ~((long) mask << relPos)) | ((long) value << relPos);
        return old;
    }

    @Override
    public BitArray clone() {
        long[] clonedDataArray;
        if (this.alloc != null) {
            int minLength = minLength(this.bits, this.size);
            System.arraycopy(this.internalDataArray, 0, clonedDataArray = this.alloc.atLeast(minLength), 0, minLength);
        } else {
            clonedDataArray = this.internalDataArray.clone();
        }
        return new NullablePaddedBitArray(this.bits, this.size, clonedDataArray, this.alloc);
    }

    @Override
    public BitArray retain() throws AlreadyReleasedException {
        super.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (this.alloc != null) {
            this.alloc.release(this.internalDataArray);
        }
    }
}