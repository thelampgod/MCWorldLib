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

package net.daporkchop.mcworldlib.world.section;

import lombok.NonNull;
import net.daporkchop.lib.common.misc.refcount.RefCounted;
import net.daporkchop.lib.math.access.IntHolderXYZ;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.lib.unsafe.util.exception.AlreadyReleasedException;
import net.daporkchop.mcworldlib.block.access.LightAccess;
import net.daporkchop.mcworldlib.format.common.nibble.NibbleArray;
import net.daporkchop.mcworldlib.version.MinecraftVersion;

import java.util.Collection;

/**
 * Representation of a Minecraft chunk section, consisting of a 16³ volume of blocks, along with light levels for block and (optionally) sky light.
 *
 * @author DaPorkchop_
 * @see LegacySection
 * @see FlattenedSection
 */
public interface Section extends LightAccess, IntHolderXYZ, RefCounted {
    /**
     * @return the version of Minecraft that this section was last saved in
     */
    MinecraftVersion version();

    /**
     * @return this section's X coordinate
     */
    @Override
    int x();

    /**
     * @return this section's Y coordinate
     */
    @Override
    int y();

    /**
     * @return this section's Z coordinate
     */
    @Override
    int z();

    /**
     * @return the {@link NibbleArray} used by this section for storing block light data
     * @throws UnsupportedOperationException if this section does not have block light (see {@link #hasSkyLight()})
     */
    NibbleArray blockLightStorage();

    /**
     * @return the {@link NibbleArray} used by this section for storing sky light data
     * @throws UnsupportedOperationException if this section does not have sky light (see {@link #hasSkyLight()})
     */
    NibbleArray skyLightStorage();

    /**
     * Gets the tile entity at the given coordinates.
     *
     * @param x the X coordinate of the tile entity to get
     * @param y the Y coordinate of the tile entity to get
     * @param z the Z coordinate of the tile entity to get
     * @return the tile entity at the given coordinates, or {@code null} if there is none present
     */
    CompoundTag getTileEntity(int x, int y, int z);

    /**
     * Sets the tile entity at the given coordinates.
     *
     * @param x          the X coordinate of the tile entity to set
     * @param y          the Y coordinate of the tile entity to set
     * @param z          the Z coordinate of the tile entity to set
     * @param tileEntity the new tile entity. If {@code null}, the tile entity will be removed
     */
    void setTileEntity(int x, int y, int z, CompoundTag tileEntity);

    /**
     * @return a view of the tile entities in this section
     */
    Collection<CompoundTag> tileEntities();

    /**
     * Adds the given entity to this section.
     *
     * @param entity the entity to add
     */
    void addEntity(@NonNull CompoundTag entity);

    /**
     * Removes the given entity from this section.
     *
     * @param entity the entity to remove
     */
    void removeEntity(@NonNull CompoundTag entity);

    /**
     * @return a view of the entities in this section
     */
    Collection<CompoundTag> entities();

    @Override
    Section retain() throws AlreadyReleasedException;
}
