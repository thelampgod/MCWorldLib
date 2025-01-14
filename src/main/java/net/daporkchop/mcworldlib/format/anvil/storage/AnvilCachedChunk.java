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

package net.daporkchop.mcworldlib.format.anvil.storage;

import lombok.NonNull;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.lib.nbt.tag.DoubleTag;
import net.daporkchop.lib.nbt.tag.ListTag;
import net.daporkchop.mcworldlib.format.java.JavaFixers;
import net.daporkchop.mcworldlib.format.java.decoder.JavaLevelDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.JavaSectionDecoder;
import net.daporkchop.mcworldlib.util.dirty.AbstractReleasableDirtiable;
import net.daporkchop.mcworldlib.util.nbt.AllocatedNBTHelper;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.Chunk;
import net.daporkchop.mcworldlib.world.World;
import net.daporkchop.mcworldlib.world.section.Section;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * In-memory representation of a chunk cached by {@link AnvilWorldStorage}.
 * <p>
 * This class is thread-safe.
 *
 * @author DaPorkchop_
 */
public abstract class AnvilCachedChunk extends AbstractReleasableDirtiable {
    public abstract Chunk chunk();

    public abstract Section section(int y);

    public static class ReadOnlyEmpty extends AnvilCachedChunk {
        @Override
        public Chunk chunk() {
            return null;
        }

        @Override
        public Section section(int y) {
            return null;
        }

        @Override
        protected void doRelease() {
            //no-op
        }
    }

    public static class ReadOnly extends AnvilCachedChunk {
        protected final Chunk chunk;
        protected final Section[] sections = new Section[24];

        public ReadOnly(@NonNull CompoundTag tag, @NonNull JavaVersion version, @NonNull JavaFixers fixers, @NonNull World world) {
            this.chunk = fixers.chunk().ceilingEntry(version).getValue()
                    .decode(tag, version, world);

            JavaLevelDecoder levelDecoder = fixers.level().ceilingEntry(version).getValue();
            CompoundTag levelTag = levelDecoder.decode(tag, version);
            boolean newNames = version.compareTo(JavaVersion.fromName("1.19.4")) >= 0;

            JavaSectionDecoder sectionDecoder = fixers.section().ceilingEntry(version).getValue();
            for (CompoundTag sectionTag : levelTag.getList((newNames ? "sections" : "Sections"), CompoundTag.class)) {
                Section section = sectionDecoder.decode(sectionTag, version, world, this.chunk.x(), this.chunk.z());
                checkState(this.sections[section.y()] == null, "duplicate section at y=%d!", section.y());
                this.sections[section.y()] = section;
            }

            ListTag<CompoundTag> tileEntities = levelTag.getList((newNames ? "block_entities" : "TileEntities"), CompoundTag.class);
            for (CompoundTag tileEntity : tileEntities) {
                int x = tileEntity.getInt("x");
                int y = tileEntity.getInt("y");
                int z = tileEntity.getInt("z");
                this.sections[y >> 4].setTileEntity(x & 0xF, y & 0xF, z & 0xF, AllocatedNBTHelper.toNormalAndRelease(tileEntity));
            }
            tileEntities.list().clear();

//            //TODO: i should probably make entities be their own thing, because 1.17 stores them separately
//            ListTag<CompoundTag> entities = levelTag.getList("Entities", CompoundTag.class);
//            for (CompoundTag entity : entities) {
//                ListTag<DoubleTag> pos = entity.getList("Pos", DoubleTag.class);
//                double y = pos.list().get(1).doubleValue();
//                //TODO: entities might be in an empty chunk section
//                this.sections[clamp(floorI(y) >> 4, 0, 15)].addEntity(AllocatedNBTHelper.toNormalAndRelease(entity));
//            }
//            entities.list().clear();
        }

        @Override
        public Chunk chunk() {
            return this.chunk.retain();
        }

        @Override
        public Section section(int y) {
            Section section = this.sections[y];
            return section != null ? section.retain() : null;
        }

        @Override
        protected void doRelease() {
            this.chunk.release();
            for (Section section : this.sections) {
                if (section != null) {
                    section.release();
                }
            }
        }
    }

    //TODO
    /*public static class ReadWrite extends AnvilCachedChunk {
        protected final JavaVersion version;

        protected CompoundTag chunkTag;
        protected final CompoundTag[] sectionTags = new CompoundTag[16];
        protected final CompoundTag[][] sectionBlockEntityTags = new CompoundTag[16][];

        public ReadWrite(@NonNull JavaVersion version) {
            super(version);
        }

        @Override
        protected void doRelease() {
            this.chunkTag.release();
            for (CompoundTag sectionTag : this.sectionTags) {
                if (sectionTag != null) {
                    sectionTag.release();
                }
            }
            for (CompoundTag[] blockEntityTags : this.sectionBlockEntityTags) {
                if (blockEntityTags != null) {
                    for (CompoundTag blockEntityTag : blockEntityTags) {
                        blockEntityTag.release();
                    }
                }
            }
        }
    }*/
}
