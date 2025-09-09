package com.company.turtle.indexing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SimpleCodeIndexer implements CodeIndexer {

    @Override
    public void indexDirectory(Path sourceDirectory) {
        try {
            Files.walk(sourceDirectory)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            indexFile(p);
                        } catch (Exception e) {
                            // ignore for now
                        }
                    });
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public List<CodeChunk> indexFile(Path javaFile) {
        try {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            CodeChunk chunk = new CodeChunk(UUID.randomUUID().toString(), javaFile.toString(), content, 1, content.split("\n").length);
            List<CodeChunk> out = new ArrayList<>();
            out.add(chunk);
            return out;
        } catch (IOException e) {
            return List.of();
        }
    }
}
