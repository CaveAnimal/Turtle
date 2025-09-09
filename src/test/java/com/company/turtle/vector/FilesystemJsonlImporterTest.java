package com.company.turtle.vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FilesystemJsonlImporterTest {

    @Test
    public void importRoundTrip() throws Exception {
        Path tmp = Path.of("target","test-vectors-import");
        Files.createDirectories(tmp);

        // create a JSONL file with one record
        Path jsonl = Path.of("target","test-vectors.jsonl");
        String json = "{\"id\":\"bar\",\"vector\":[1.0,2.0],\"metadata\":{\"tag\":\"x\"}}\n";
        Files.writeString(jsonl, json);

        FilesystemJsonlImporter.importTo(jsonl, tmp);

        assertTrue(Files.exists(tmp.resolve("bar.vec")));
        assertTrue(Files.exists(tmp.resolve("bar.meta.json")));

        byte[] bs = Files.readAllBytes(tmp.resolve("bar.vec"));
        ByteBuffer bb = ByteBuffer.wrap(bs).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1.0f, bb.getFloat(), 1e-6f);
        assertEquals(2.0f, bb.getFloat(), 1e-6f);
    }
}
