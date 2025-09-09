package com.company.turtle.indexing;

public class CodeChunk {
    private String id;
    private String filePath;
    private String content;
    private int startLine;
    private int endLine;
    private java.util.Map<String,Object> metadata = new java.util.HashMap<>();

    public CodeChunk() {}

    public CodeChunk(String id, String filePath, String content, int startLine, int endLine) {
        this.id = id;
        this.filePath = filePath;
        this.content = content;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    // getters
    public String getId() { return id; }
    public String getFilePath() { return filePath; }
    public String getContent() { return content; }
    public int getStartLine() { return startLine; }
    public int getEndLine() { return endLine; }
    public java.util.Map<String,Object> getMetadata() { return metadata; }

    // setters
    public void setId(String id) { this.id = id; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setContent(String content) { this.content = content; }
    public void setStartLine(int startLine) { this.startLine = startLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
    public void setMetadata(java.util.Map<String,Object> metadata) { this.metadata = metadata; }
}

