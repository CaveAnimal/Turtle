package com.company.turtle.vector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Export filesystem vector store into a JSONL file where each line is:
 * {"id": "<id>", "vector": [..], "metadata": {..}}
 */
public class FilesystemToJsonlExporter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Path export(Path vectorsDir, Path outFile) throws IOException {
        Files.createDirectories(outFile.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(outFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (!Files.exists(vectorsDir)) return outFile;
            for (Path p : Files.list(vectorsDir).collect(Collectors.toList())) {
                String name = p.getFileName().toString();
                if (!name.endsWith(".vec")) continue;
                String id = name.substring(0, name.length() - 4);
                byte[] bs = Files.readAllBytes(p);
                if (bs.length % 4 != 0) continue;
                ByteBuffer bb = ByteBuffer.wrap(bs).order(ByteOrder.LITTLE_ENDIAN);
                int len = bs.length / 4;
                float[] vec = new float[len];
                for (int i = 0; i < len; i++) vec[i] = bb.getFloat();

                Map<String, Object> meta = Map.of();
                Path metaPath = vectorsDir.resolve(id + ".meta.json");
                if (Files.exists(metaPath)) {
                    try (InputStream is = Files.newInputStream(metaPath)) {
                        meta = mapper.readValue(is, Map.class);
                    } catch (Exception ex) {
                        meta = Map.of();
                    }
                }

                Map<String, Object> obj = new HashMap<>();
                obj.put("id", id);
                obj.put("vector", vec);
                obj.put("metadata", meta);
                w.write(mapper.writeValueAsString(obj));
                w.newLine();
            }
        }
        return outFile;
    }
}
