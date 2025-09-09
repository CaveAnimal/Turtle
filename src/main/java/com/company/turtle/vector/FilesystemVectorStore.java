package com.company.turtle.vector;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FilesystemVectorStore implements VectorStore {

    private final Path storage = Path.of("data","vectors");
    private final ObjectMapper mapper = new ObjectMapper();

    public FilesystemVectorStore() {
        try { Files.createDirectories(storage); } catch (IOException e) {}
    }

    @Override
    public void store(String id, float[] vector, Map<String, Object> metadata) {
        try {
            Path vecPath = storage.resolve(id + ".vec");
            try (OutputStream os = Files.newOutputStream(vecPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer bb = ByteBuffer.allocate(4 * vector.length).order(ByteOrder.LITTLE_ENDIAN);
                for (float f : vector) bb.putFloat(f);
                os.write(bb.array());
            }
            Path metaPath = storage.resolve(id + ".meta.json");
            Map<String,Object> meta = metadata == null ? new HashMap<>() : metadata;
            meta.put("id", id);
            try (OutputStream os = Files.newOutputStream(metaPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                mapper.writeValue(os, meta);
            }
        } catch (IOException e) {
            // ignore for now
        }
    }

    @Override
    public List<SearchResult> search(float[] query, int topK) {
        List<SearchResult> results = new ArrayList<>();
        try {
            if (!Files.exists(storage)) return results;
            for (Path p : Files.list(storage).toList()) {
                try {
                    String name = p.getFileName().toString();
                    if (!name.endsWith(".vec")) continue;
                    String id = name.substring(0, name.length() - 4);
                    float[] vec = readVector(p);
                    if (vec == null || vec.length != query.length) continue;
                    double score = cosineSimilarity(query, vec);
                    Map<String,Object> meta = readMeta(storage.resolve(id + ".meta.json"));
                    results.add(new SearchResult(id, score, meta));
                } catch (Exception ex) {
                    // ignore this file and continue
                }
            }
        } catch (IOException e) {
            // ignore
        }
        results.sort((a,b) -> Double.compare(b.score, a.score));
        if (results.size() > topK) return results.subList(0, topK);
        return results;
    }

    private float[] readVector(Path p) {
        try (InputStream is = Files.newInputStream(p)) {
            long size = Files.size(p);
            if (size % 4 != 0) return null;
            int len = (int)(size / 4);
            byte[] bs = is.readAllBytes();
            ByteBuffer bb = ByteBuffer.wrap(bs).order(ByteOrder.LITTLE_ENDIAN);
            float[] out = new float[len];
            for (int i = 0; i < len; i++) out[i] = bb.getFloat();
            return out;
        } catch (IOException e) { return null; }
    }

    private Map<String,Object> readMeta(Path p) {
        try (InputStream is = Files.newInputStream(p)) {
            return mapper.readValue(is, Map.class);
        } catch (IOException e) { return Map.of(); }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) { dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i]; }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na)*Math.sqrt(nb));
    }

    @Override
    public void delete(String id) {
        try { Files.deleteIfExists(storage.resolve(id + ".vec")); Files.deleteIfExists(storage.resolve(id + ".meta.json")); } catch (IOException e) {}
    }
}
