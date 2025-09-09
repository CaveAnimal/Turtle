# Code Talker - Semantic Code Search System
## Project Requirements Document (PRD)

> **File**: `TURTLE_PRD.md`  
> **Version**: 1.0  
> **Date**: September 2025  
> **Author**: Development Team

### Executive Summary
Code Talker is an intelligent code search system designed to help developers and business users navigate and understand a 1M+ line legacy Java codebase through semantic search capabilities. The system provides contextual code discovery, dependency analysis, and business logic location without requiring external dependencies or cloud services.

---

## 1. Project Overview

### 1.1 Problem Statement
- Large legacy Java 8 codebase (1M+ lines) is difficult to navigate
- Developers struggle to locate business logic and understand code relationships
- Business users cannot easily find where specific features are implemented
- Traditional text search is insufficient for semantic code discovery
- Need for on-premises solution with no external dependencies

### 1.2 Solution Overview
A locally-deployable semantic search system that:
- Indexes Java source code using modern embedding techniques
- Provides semantic search capabilities ("find authentication logic")
- Offers contextual code navigation and dependency analysis
- Serves both technical and business users through a web interface
- Runs entirely on-premises for security and privacy

### 1.3 Success Criteria
- **Developer Productivity**: Reduce code discovery time by 60%
- **Business Understanding**: Enable business users to locate feature implementations
- **System Performance**: Sub-second search response times
- **Deployment**: Single-command deployment on developer laptops and servers

---

## 2. Technical Requirements

### 2.1 Core Technology Stack
```
Backend Framework: Spring Boot 3.x (Java 21)
Vector Database: DataStax JVector (embedded)
Embedding Model: all-MiniLM-L6-v2 (ONNX format)
Web Framework: Spring MVC + Thymeleaf
Frontend: Bootstrap 5 + HTMX
Database: H2 (embedded) for metadata
Build System: Maven
Containerization: Docker (for server deployment)
```

### 2.2 Architecture Components

#### 2.2.1 Code Ingestion Pipeline
```java
// Component interfaces to implement
public interface CodeIndexer {
    void indexDirectory(Path sourceDirectory);
    void indexFile(Path javaFile);
    List<CodeChunk> chunkFile(String content, String filePath);
}

public interface EmbeddingGenerator {
    float[] generateEmbedding(String text);
    List<float[]> batchGenerateEmbeddings(List<String> texts);
}

public interface VectorStore {
    void storeEmbedding(String id, float[] embedding, Map<String, Object> metadata);
    List<SearchResult> search(float[] queryEmbedding, int maxResults, double threshold);
}
```

#### 2.2.2 Search Service Architecture
```java
@Service
public class CodeSearchService {
    // Core search functionality
    public SearchResults searchCode(String query, SearchOptions options);
    public List<CodeSnippet> findRelatedCode(String fileId, int contextLines);
    public DependencyGraph analyzeDependencies(String className);
}

@RestController
public class SearchController {
    // REST endpoints for search operations
    @GetMapping("/api/search")
    @PostMapping("/api/search/semantic")
    @GetMapping("/api/code/{fileId}")
}
```

### 2.3 Data Models

#### 2.3.1 Core Entities
```java
@Entity
public class CodeChunk {
    private String id;
    private String filePath;
    private String content;
    private ChunkType type; // METHOD, CLASS, PACKAGE, COMMENT
    private int startLine;
    private int endLine;
    private Map<String, Object> metadata;
}

@Entity
public class SearchResult {
    private String chunkId;
    private double relevanceScore;
    private String highlightedContent;
    private String filePath;
    private int lineNumber;
}

public enum ChunkType {
    METHOD, CLASS, INTERFACE, PACKAGE_INFO, 
    COMMENT, CONFIGURATION, BUILD_SCRIPT
}
```

### 2.4 Performance Requirements
- **Index Build Time**: Complete indexing of 1M lines in < 30 minutes
- **Search Response**: < 500ms for semantic queries
- **Memory Usage**: < 4GB RAM for complete system
- **Startup Time**: < 60 seconds from cold start
- **Concurrent Users**: Support 20+ simultaneous users

### 2.5 Deployment Requirements

#### 2.5.1 Local Development
```dockerfile
# Dockerfile for local deployment
# (artifact name uses Turtle)
FROM openjdk:21-jdk-slim
COPY target/Turtle-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx3g", "-jar", "/app.jar"]
```

#### 2.5.2 Server Deployment
```yaml
# docker-compose.yml for server deployment
version: '3.8'
services:
  Turtle:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./data:/app/data
      - ./indexes:/app/indexes
    environment:
      - SPRING_PROFILES_ACTIVE=production
```

---

## 3. Functional Requirements

### 3.1 Core Features

#### 3.1.1 Code Indexing
- **FR-001**: Index Java source files (.java) recursively from specified directories
- **FR-002**: Parse and chunk code at multiple granularities (method, class, package)
- **FR-003**: Extract metadata (class names, method signatures, annotations)
- **FR-004**: Support incremental indexing for changed files
- **FR-005**: Index related files (XML configs, properties, SQL scripts)

#### 3.1.2 Semantic Search
- **FR-006**: Accept natural language queries ("find user authentication logic")
- **FR-007**: Return ranked results with relevance scores
- **FR-008**: Provide syntax-highlighted code snippets with context
- **FR-009**: Support exact keyword searches for identifiers
- **FR-010**: Enable filtering by file type, package, or date modified

#### 3.1.3 Code Navigation
- **FR-011**: Display file path and line numbers for all results
- **FR-012**: Provide "View in Context" with surrounding code
- **FR-013**: Show class hierarchy and inheritance relationships
- **FR-014**: Generate dependency graphs for classes and packages
- **FR-015**: Enable cross-reference navigation (find usages, find declarations)

### 3.2 User Interface Requirements

#### 3.2.1 Search Interface
```html
<!-- Main search interface components -->
<div class="search-container">
    <input type="text" id="searchQuery" placeholder="Search code: 'find authentication logic'">
    <div class="search-filters">
        <select id="searchType">
            <option value="semantic">Semantic Search</option>
            <option value="exact">Exact Match</option>
            <option value="hybrid">Hybrid</option>
        </select>
        <select id="fileFilter">
            <option value="all">All Files</option>
            <option value="java">Java Only</option>
            <option value="config">Config Files</option>
        </select>
    </div>
    <button onclick="performSearch()">Search</button>
</div>
```

#### 3.2.2 Results Display
- **FR-016**: Display results in paginated list with previews
- **FR-017**: Show relevance scores and match highlighting
- **FR-018**: Provide expandable code context views
- **FR-019**: Enable result export (PDF, text, markdown)
- **FR-020**: Support result sharing via permanent links

### 3.3 Business User Features
- **FR-021**: Simple search interface without technical jargon
- **FR-022**: Business-friendly result descriptions
- **FR-023**: Feature mapping ("Where is invoice generation?")
- **FR-024**: Workflow visualization for business processes
- **FR-025**: Glossary of technical terms found in codebase

---

## 4. Non-Functional Requirements

### 4.1 Performance
- **NFR-001**: Search response time < 500ms for 95% of queries
- **NFR-002**: System startup time < 60 seconds
- **NFR-003**: Support concurrent indexing and searching
- **NFR-004**: Memory usage < 4GB for 1M line codebase
- **NFR-005**: Index size < 2GB on disk

### 4.2 Scalability
- **NFR-006**: Handle codebases up to 5M lines
- **NFR-007**: Support 50+ concurrent users
- **NFR-008**: Horizontal scaling capability for larger deployments
- **NFR-009**: Incremental index updates without full rebuild
- **NFR-010**: Plugin architecture for additional file types

### 4.3 Reliability
- **NFR-011**: 99.9% uptime for production deployments
- **NFR-012**: Graceful degradation when indexes are unavailable
- **NFR-013**: Automatic recovery from corrupted indexes
- **NFR-014**: Comprehensive logging and monitoring
- **NFR-015**: Health check endpoints for monitoring

### 4.4 Security
- **NFR-016**: All processing occurs on-premises
- **NFR-017**: No external network dependencies for core functionality
- **NFR-018**: Support for authentication integration (LDAP, OAuth)
- **NFR-019**: Audit logging for search activities
- **NFR-020**: Configurable access controls by code sections

---

## 5. Implementation Specifications

### 5.1 Project Structure
```
Turtle/
├── src/main/java/com/company/turtle/
│   ├── config/           # Spring configuration
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic
│   ├── repository/       # Data access
│   ├── model/           # Entity classes
│   ├── indexing/        # Code parsing and indexing
│   ├── search/          # Search implementation
│   └── embedding/       # Embedding generation
├── src/main/resources/
│   ├── static/          # CSS, JS, images
│   ├── templates/       # Thymeleaf templates
│   └── application.yml  # Configuration
├── src/test/java/       # Unit and integration tests
├── docs/               # Documentation
└── scripts/            # Deployment scripts
```

### 5.2 Key Dependencies (Maven)
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- JVector for embeddings -->
    <dependency>
        <groupId>com.datastax.astra</groupId>
        <artifactId>jvector</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- ONNX Runtime for embeddings -->
    <dependency>
        <groupId>com.microsoft.onnxruntime</groupId>
        <artifactId>onnxruntime</artifactId>
        <version>1.16.0</version>
    </dependency>
    
    <!-- Java Parser for AST -->
    <dependency>
        <groupId>com.github.javaparser</groupId>
        <artifactId>javaparser-core</artifactId>
        <version>3.25.0</version>
    </dependency>
    
    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.220</version>
    </dependency>
</dependencies>
```

### 5.3 Configuration Properties
```yaml
# application.yml
turtle:
  indexing:
    source-directories:
      - "/path/to/legacy/codebase"
    chunk-size: 512
    overlap: 64
    batch-size: 100
  
  embedding:
    model-path: "models/all-MiniLM-L6-v2.onnx"
    dimension: 384
    
  vector-store:
    index-path: "./data/indexes"
    max-connections: 20
    
  search:
    max-results: 50
    similarity-threshold: 0.7
    
  server:
    port: 8080
    context-path: /Turtle
```

---

## 6. Development Phases

### Phase 1: Core Infrastructure (Weeks 1-2)
- **Deliverables**: 
  - Basic Spring Boot application structure
  - Code parsing and chunking pipeline
  - ONNX embedding model integration
  - JVector index creation and storage

- **Key Components**:
  ```java
  @Service
  public class IndexingService {
      public void buildInitialIndex(List<Path> sourcePaths);
  }
  
  @Component
  public class EmbeddingService {
      public float[] generateEmbedding(String text);
  }
  ```

### Phase 2: Search Implementation (Weeks 3-4)
- **Deliverables**:
  - Semantic search functionality
  - REST API endpoints
  - Basic web interface
  - Result ranking and filtering

- **Key Components**:
  ```java
  @RestController
  public class SearchApiController {
      @GetMapping("/api/search")
      public ResponseEntity<SearchResults> search(@RequestParam String query);
  }
  ```

### Phase 3: User Interface (Weeks 5-6)
- **Deliverables**:
  - Complete web UI with Thymeleaf templates
  - Interactive search with HTMX
  - Code syntax highlighting
  - Result pagination and filtering

### Phase 4: Advanced Features (Weeks 7-8)
- **Deliverables**:
  - Dependency analysis
  - Code context views
  - Business user interface
  - Performance optimizations

### Phase 5: Deployment & Documentation (Weeks 9-10)
- **Deliverables**:
  - Docker containerization
  - Deployment scripts
  - User documentation
  - Administrator guide

---

## 7. Testing Strategy

### 7.1 Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class CodeSearchServiceTest {
    @Mock
    private VectorStore vectorStore;
    
    @Test
    void shouldReturnRelevantResults() {
        // Test semantic search functionality
    }
}
```

### 7.2 Integration Tests
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SearchIntegrationTest {
    @Test
    void shouldIndexAndSearchCodeSuccessfully() {
        // Test complete indexing and search pipeline
    }
}
```

### 7.3 Performance Tests
- Load testing with JMeter for concurrent users
- Memory profiling during large indexing operations
- Search response time benchmarking

---

## 8. Deployment Instructions

### 8.1 Local Development Setup
```bash
# Clone repository
git clone <repository-url>
cd Turtle

# Build application
mvn clean package

# Download embedding model
mkdir -p models
wget -O models/all-MiniLM-L6-v2.onnx <model-url>

# Run application
java -jar target/Turtle-1.0.0.jar

# Access application
open http://localhost:8080/Turtle
```

### 8.2 Server Deployment
```bash
# Build Docker image
docker build -t Turtle:latest .

# Deploy with docker-compose
docker-compose up -d

# Verify deployment
curl http://localhost:8080/Turtle/actuator/health
```

---

## 9. Future Enhancements

### 9.1 Optional LLM Integration
- **Enhancement**: Add Ollama integration for code explanations
- **Benefit**: Natural language code summaries and explanations
- **Implementation**: Optional service layer with feature toggle

### 9.2 IDE Integration
- **Enhancement**: VS Code extension for direct IDE search
- **Benefit**: Search without leaving development environment
- **Implementation**: REST API client extension

### 9.3 Advanced Analytics
- **Enhancement**: Code complexity analysis and hotspot detection
- **Benefit**: Identify refactoring opportunities
- **Implementation**: Additional indexing pipeline for metrics

### 9.4 Multi-Language Support
- **Enhancement**: Support for other languages (Python, JavaScript, etc.)
- **Benefit**: Unified search across polyglot codebases
- **Implementation**: Pluggable parser architecture

---

## 10. Risk Assessment

### 10.1 Technical Risks
- **Risk**: ONNX model compatibility issues
- **Mitigation**: Test on target deployment environments early

- **Risk**: Memory usage with large codebases
- **Mitigation**: Implement streaming indexing and pagination

### 10.2 Performance Risks
- **Risk**: Slow indexing performance
- **Mitigation**: Parallel processing and incremental updates

- **Risk**: Search accuracy issues
- **Mitigation**: Hybrid search combining semantic and keyword approaches

### 10.3 Deployment Risks
- **Risk**: Java version compatibility across different environments
- **Mitigation**: Clear documentation of Java 21 requirements and testing

- **Risk**: Resource constraints on older servers
- **Mitigation**: Configurable memory limits and graceful degradation

- **Risk**: File system permissions on server deployment
- **Mitigation**: Clear setup instructions and permission requirements

---

## 11. Success Metrics

### 11.1 Developer Metrics
- Time to find specific code functionality (target: < 30 seconds)
- Number of successful searches per developer per day
- Reduction in "ask a colleague" interactions

### 11.2 System Metrics
- Search response time (target: < 500ms)
- Index build time (target: < 30 minutes for 1M lines)
- System uptime (target: > 99.5%)

### 11.3 Business Metrics
- Business user adoption rate
- Accuracy of feature location requests
- Reduction in development onboarding time

---

## Appendices

### A. Glossary
- **Embedding**: Numerical vector representation of text
- **Semantic Search**: Search based on meaning rather than exact text matching
- **Vector Database**: Database optimized for similarity search on high-dimensional vectors
- **ONNX**: Open Neural Network Exchange format for ML models

### B. References
- JVector Documentation: https://github.com/datastax/jvector
- Sentence Transformers: https://www.sbert.net/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- ONNX Runtime Java: https://onnxruntime.ai/docs/get-started/with-java.html