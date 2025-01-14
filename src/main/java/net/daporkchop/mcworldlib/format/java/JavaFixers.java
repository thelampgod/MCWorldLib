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

package net.daporkchop.mcworldlib.format.java;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.mcworldlib.format.anvil.decoder.chunk.ChunkDecoder_1_19_4;
import net.daporkchop.mcworldlib.format.anvil.decoder.chunk.FlattenedChunkDecoder;
import net.daporkchop.mcworldlib.format.anvil.decoder.chunk.LegacyChunkDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.JavaChunkDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.JavaLevelDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.JavaSectionDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.level.LegacyLevelDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.level.LevelDecoder1_19_4;
import net.daporkchop.mcworldlib.format.java.decoder.section.LegacySectionDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.section.PackedFlattenedSectionDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.section.PaddedFlattenedSectionDecoder;
import net.daporkchop.mcworldlib.format.java.decoder.section.SectionDecoder1_19_4;
import net.daporkchop.mcworldlib.version.java.JavaVersion;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class JavaFixers {
    /**
     * @return the default {@link JavaFixers}
     */
    public static JavaFixers defaultFixers() {
        return Default.DEFAULT_JAVA_FIXERS;
    }

    @NonNull
    protected final NavigableMap<JavaVersion, JavaChunkDecoder> chunk;
    @NonNull
    protected final NavigableMap<JavaVersion, JavaSectionDecoder> section;
    @NonNull
    protected final NavigableMap<JavaVersion, JavaLevelDecoder> level;

    @RequiredArgsConstructor
    public static final class MapBuilder<K, V, M extends Map<K, V>> {
        @NonNull
        protected final M map;

        public MapBuilder<K, V, M> put(K key, V value) {
            this.map.put(key, value);
            return this;
        }

        public M build() {
            return this.map;
        }
    }

    private static final class Default {
        private static final JavaFixers DEFAULT_JAVA_FIXERS = new JavaFixers(
                new MapBuilder<>(new TreeMap<JavaVersion, JavaChunkDecoder>())
                        .put(LegacyChunkDecoder.VERSION, new LegacyChunkDecoder())
                        .put(FlattenedChunkDecoder.VERSION, new FlattenedChunkDecoder())
                        .put(ChunkDecoder_1_19_4.VERSION, new ChunkDecoder_1_19_4())
                        .build(),
                new MapBuilder<>(new TreeMap<JavaVersion, JavaSectionDecoder>())
                        .put(LegacySectionDecoder.VERSION, new LegacySectionDecoder())
                        .put(PackedFlattenedSectionDecoder.VERSION, new PackedFlattenedSectionDecoder())
                        .put(PaddedFlattenedSectionDecoder.VERSION, new PaddedFlattenedSectionDecoder())
                        .put(SectionDecoder1_19_4.VERSION, new SectionDecoder1_19_4())
                        .build(),
                new MapBuilder<>(new TreeMap<JavaVersion, JavaLevelDecoder>())
                        .put(LegacyLevelDecoder.VERSION, new LegacyLevelDecoder())
                        .put(LevelDecoder1_19_4.VERSION, new LevelDecoder1_19_4())
                        .build());
    }
}
