package com.company.turtle.vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Import JSONL produced by FilesystemToJsonlExporter back into vector files + metadata.
 */
public class FilesystemJsonlImporter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Path importTo(Path jsonl, Path outVectorsDir) throws IOException {
        Files.createDirectories(outVectorsDir);
        if (!Files.exists(jsonl)) return outVectorsDir;
        try (BufferedReader r = Files.newBufferedReader(jsonl)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                Map<String, Object> obj = mapper.readValue(line, Map.class);
                String id = String.valueOf(obj.get("id"));
                Object vecObj = obj.get("vector");
                float[] vec;
                if (vecObj instanceof List) {
                    List<?> list = (List<?>) vecObj;
                    vec = new float[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Number n = (Number) list.get(i);
                        vec[i] = n.floatValue();
                    }
                } else {
                    continue; // skip unknown format
                }
                // write vector
                Path vecPath = outVectorsDir.resolve(id + ".vec");
                ByteBuffer bb = ByteBuffer.allocate(4 * vec.length).order(ByteOrder.LITTLE_ENDIAN);
                for (float f : vec) bb.putFloat(f);
                Files.write(vecPath, bb.array(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                // write metadata
                Path metaPath = outVectorsDir.resolve(id + ".meta.json");
                Object metaObj = obj.get("metadata");
                if (metaObj == null) metaObj = Map.of();
                try (var os = Files.newOutputStream(metaPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    mapper.writeValue(os, metaObj);
                }
            }
        }
        return outVectorsDir;
    }
}
