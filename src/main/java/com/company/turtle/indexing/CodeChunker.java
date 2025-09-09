package com.company.turtle.indexing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

@Service
public class CodeChunker {

    public List<CodeChunk> chunkFile(Path javaFile) {
        List<CodeChunk> out = new ArrayList<>();
        try {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            CompilationUnit cu = StaticJavaParser.parse(content);

            // class-level chunks
            for (ClassOrInterfaceDeclaration c : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                int begin = c.getBegin().map(p -> p.line).orElse(1);
                int end = c.getEnd().map(p -> p.line).orElse(begin);
                CodeChunk chunk = new CodeChunk(UUID.randomUUID().toString(), javaFile.toString(), c.toString(), begin, end);
                chunk.getMetadata().put("type", "class");
                chunk.getMetadata().put("name", c.getNameAsString());
                out.add(chunk);
            }

            // method-level chunks
            for (MethodDeclaration m : cu.findAll(MethodDeclaration.class)) {
                int begin = m.getBegin().map(p -> p.line).orElse(1);
                int end = m.getEnd().map(p -> p.line).orElse(begin);
                CodeChunk chunk = new CodeChunk(UUID.randomUUID().toString(), javaFile.toString(), m.toString(), begin, end);
                chunk.getMetadata().put("type", "method");
                chunk.getMetadata().put("name", m.getNameAsString());
                out.add(chunk);
            }

            // if nothing found, add whole file
            if (out.isEmpty()) {
                out.add(new CodeChunk(UUID.randomUUID().toString(), javaFile.toString(), content, 1, content.split("\n").length));
            }

            // dedupe by full content string (preserve insertion order)
            java.util.Map<String, CodeChunk> map = new java.util.LinkedHashMap<>();
            for (CodeChunk c : out) {
                String key = c.getContent();
                if (!map.containsKey(key)) map.put(key, c);
            }
            out = new ArrayList<>(map.values());
        } catch (IOException e) {
            // ignore
        }
        return out;
    }
}
