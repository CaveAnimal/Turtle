package com.company.turtle.vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FilesystemToJsonlExporterTest {

    @Test
    public void exportSmallVector() throws Exception {
        Path tmp = Path.of("target","test-vectors");
        Files.createDirectories(tmp);
        Path vec = tmp.resolve("foo.vec");
        Path meta = tmp.resolve("foo.meta.json");
        float[] v = new float[]{1.0f, 2.0f, 3.0f};
        ByteBuffer bb = ByteBuffer.allocate(4*v.length).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : v) bb.putFloat(f);
        Files.write(vec, bb.array());
        Files.writeString(meta, "{\"lang\":\"java\"}");

        Path out = Path.of("target","vectors-out.jsonl");
        FilesystemToJsonlExporter.export(tmp, out);
        assertTrue(Files.exists(out));
        long lines = Files.lines(out).count();
        assertEquals(1, lines);
        String line = Files.readString(out);
        assertTrue(line.contains("\"id\":\"foo\""));
        assertTrue(line.contains("\"lang\":\"java\""));
    }
}
