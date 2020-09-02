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

package net.daporkchop.mcworldlib.format.anvil;

import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.mcworldlib.block.BlockRegistry;
import net.daporkchop.mcworldlib.block.java.JavaBlockRegistry;
import net.daporkchop.mcworldlib.format.common.AbstractSave;
import net.daporkchop.mcworldlib.format.common.DefaultDimension;
import net.daporkchop.mcworldlib.registry.Registries;
import net.daporkchop.mcworldlib.registry.java.JavaRegistries;
import net.daporkchop.mcworldlib.save.Save;
import net.daporkchop.mcworldlib.save.SaveOptions;
import net.daporkchop.mcworldlib.version.MinecraftVersion;
import net.daporkchop.mcworldlib.version.java.JavaVersion;
import net.daporkchop.mcworldlib.world.Dimension;
import net.daporkchop.lib.nbt.NBTOptions;
import net.daporkchop.lib.nbt.tag.CompoundTag;
import net.daporkchop.mcworldlib.world.common.IWorld;

import java.io.File;

import static net.daporkchop.lib.common.util.PValidation.checkArg;

/**
 * @author DaPorkchop_
 */
public abstract class AnvilSave<I extends Save, W extends IWorld<W, ?, I>> extends AbstractSave<JavaVersion, I, W> {
    @Getter
    protected final NBTOptions chunkNBTOptions;

    public AnvilSave(@NonNull File root, @NonNull SaveOptions options, @NonNull CompoundTag levelDat, @NonNull JavaVersion version) {
        super(options, root);

        this.chunkNBTOptions = NBTOptions.DEFAULT
                .withByteAlloc(options.get(SaveOptions.BYTE_ALLOC))
                .withIntAlloc(options.get(SaveOptions.INT_ALLOC))
                .withLongAlloc(options.get(SaveOptions.LONG_ALLOC));
                //.withObjectParser(null); //TODO

        this.version = version;

        //find worlds
        this.putWorld(new DefaultDimension(Dimension.ID_OVERWORLD, 0, true, true));
        if (PFiles.checkDirectoryExists(new File(this.root, "DIM-1"))) {
            this.putWorld(new DefaultDimension(Dimension.ID_NETHER, -1, false, false));
        }
        if (PFiles.checkDirectoryExists(new File(this.root, "DIM1"))) {
            this.putWorld(new DefaultDimension(Dimension.ID_END, 1, false, false));
        }

        this.validateState();
    }

    protected JavaVersion extractVersion(@NonNull CompoundTag levelData)   {
        CompoundTag versionTag = levelData.getCompound("Data").getCompound("Version", null);
        if (versionTag == null) { //older than 15w32a
            return JavaVersion.pre15w32a();
        }
        return JavaVersion.fromName(versionTag.getString("Name"));
    }
}
