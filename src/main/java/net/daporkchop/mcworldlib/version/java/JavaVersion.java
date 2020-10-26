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

package net.daporkchop.mcworldlib.version.java;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.misc.InstancePool;
import net.daporkchop.lib.primitive.map.IntObjMap;
import net.daporkchop.lib.primitive.map.concurrent.IntObjConcurrentHashMap;
import net.daporkchop.lib.primitive.map.concurrent.ObjObjConcurrentHashMap;
import net.daporkchop.lib.primitive.map.open.IntObjOpenHashMap;
import net.daporkchop.mcworldlib.util.Util;
import net.daporkchop.mcworldlib.version.MinecraftEdition;
import net.daporkchop.mcworldlib.version.MinecraftVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * A version of Minecraft: Java Edition.
 *
 * @author DaPorkchop_
 */
@Getter
public final class JavaVersion extends MinecraftVersion {
    private static final Map<String, JavaVersion> NAME_CACHE = new ObjObjConcurrentHashMap<>(); //fast computeIfAbsent
    private static final IntObjMap<JavaVersion> DATA_VERSION_CACHE = new IntObjConcurrentHashMap<>();

    public static JavaVersion fromName(@NonNull String nameIn) {
        return NAME_CACHE.computeIfAbsent(nameIn.intern(), name -> {
            try (InputStream in = JavaVersion.class.getResourceAsStream("by_id/" + name + ".json")) {
                if (in == null) { //file wasn't found on disk
                    return new JavaVersion(name, -1L, -1, -1);
                } else {
                    ObjectNode obj = InstancePool.getInstance(JsonMapper.class).readTree(in).require();
                    return new JavaVersion(name, obj.findPath("releaseTime").require().asLong(), obj.findPath("protocolVersion").asInt(-1), obj.findPath("dataVersion").asInt(-1));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static JavaVersion fromDataVersion(int _dataVersion) {
        return DATA_VERSION_CACHE.computeIfAbsent(_dataVersion, dataVersion -> {
            String name = DataVersionToId.INSTANCE.get(dataVersion);
            return name != null ? fromName(name) : new JavaVersion(null, -1L, -1, dataVersion);
        });
    }

    /**
     * @return the {@link JavaVersion} representing the latest version of the game supported by this library
     */
    public static JavaVersion latest() {
        return LatestVersion.LATEST;
    }

    /**
     * @return the {@link JavaVersion} used for all versions of Java edition prior to snapshot 15w32a, in which data versions were first added
     */
    public static JavaVersion pre15w32a() {
        return OldVersion.OLD;
    }

    protected final int protocol;
    protected final int data;
    protected final String toString;

    protected JavaVersion(String name, long releaseTime) {
        this(name, releaseTime, -1, -1);
    }

    protected JavaVersion(String name, long releaseTime, int protocol, int data) {
        super(MinecraftEdition.JAVA, name != null ? name : "", releaseTime);

        this.protocol = protocol;
        this.data = data;

        if (name != null) {
            this.toString = "Java Edition " + name;
        } else if (data > 0) {
            this.toString = "Java Edition [unknown] (data version " + data + ')';
        } else if (protocol > 0) {
            this.toString = "Java Edition [unknown] (protocol " + data + ')';
        } else {
            this.toString = "Java Edition [unknown]";
        }
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (o instanceof JavaVersion) {
            JavaVersion j = (JavaVersion) o;
            if (this.data >= DataVersion.DATA_15w32a && j.data >= DataVersion.DATA_15w32a) { //compare by data version rather than by name
                return this.data - j.data;
            }
        }
        return super.compareTo(o);
    }

    @UtilityClass
    private static class OldVersion {
        private final JavaVersion OLD = new JavaVersion("[unknown] (≤ 1.8.9)", fromName("1.8.9").releaseTime + 1L, -1, -1);
    }

    @UtilityClass
    private static class LatestVersion {
        private final JavaVersion LATEST = fromName("1.16.1");
    }

    private static class DataVersionToId extends IntObjOpenHashMap<String> {
        private static final DataVersionToId INSTANCE = Util.parseJson(DataVersionToId.class, "data_version_to_id.json");

        @JsonAnySetter
        private void add(@NonNull String dataVersion, @NonNull String version) {
            this.put(Integer.parseUnsignedInt(dataVersion), version.intern());
        }
    }
}
