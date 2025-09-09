package com.company.turtle.vector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FilesystemToCsvExporter {
    public static Path export(Path vectorsDir, Path outCsv) throws IOException {
        Files.createDirectories(outCsv.getParent());
        var sb = new StringBuilder();
        for (Path p : Files.list(vectorsDir).collect(Collectors.toList())) {
            String name = p.getFileName().toString();
            if (!name.endsWith(".vec")) continue;
            String id = name.substring(0, name.length()-4);
            byte[] bs = Files.readAllBytes(p);
            // write as base64 to keep safe, metadata can be exported separately
            sb.append(id).append(',').append(bs.length).append('\n');
        }
        Files.writeString(outCsv, sb.toString(), StandardCharsets.UTF_8);
        return outCsv;
    }
}
