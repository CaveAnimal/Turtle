# Turtle — Local Semantic Code Search

This repository implements Turtle, a local semantic code search system for large Java codebases.

Quick tasks covered by the repository:
- Code parsing and chunking (JavaParser)
- Embedding interface and a NoOp embedding fallback
- Filesystem vector store with JSONL exporter/importer
- Conditional Lucene skeleton (opt-in)

How to export vectors to JSONL
1. Ensure vectors are present under `data/vectors` (each id.vec + id.meta.json)
2. Set the property `turtle.vector.migrate-to-jsonl=true` in `application.yml` or pass `-Dturtle.vector.migrate-to-jsonl=true`
3. Run the app; the exporter will write `data/vectors-export.jsonl`.

How to import JSONL to filesystem vectors
1. Place the JSONL file at `data/vectors-export.jsonl`
2. Set `turtle.vector.import-jsonl=true` and run the app; the importer will populate `data/vectors`.

How to enable Lucene (experimental)
1. The Lucene vector store is conditional on `turtle.vector.use-lucene=true`.
2. Lucene dependencies are not included by default to keep CI/build portable. Add Lucene dependencies to `pom.xml` and ensure the environment can resolve them before enabling.

ONNX model
- The project includes a reflection-safe `OnnxEmbeddingService` that falls back to `NoOpEmbeddingService` if runtime or model not available. Real ONNX inference is TODO.

Development
```
mvn clean package
mvn -DskipTests=false test
```

Run the embedding demo (exec plugin)

You can run the small demo CLI that tokenizes input and attempts to call the ONNX runtime (if available) with the exec plugin:

```powershell
mvn exec:java -Dexec.mainClass="com.company.turtle.embedding.EmbeddingDemo" -Dexec.args="\"Example text to embed\""
# or use the helper script
.\scripts\demo-infer.ps1 -text "Example text to embed"
```

Contact
- Maintainer: project team
Turtle - Run instructions

CI inference job
----------------
The repository includes an optional CI job that runs inference. It is disabled by default; to run it from the GitHub Actions UI use the workflow_dispatch input `enable-inference: true` or modify the workflow to run by default. Ensure your runner has the ONNX runtime/native dependencies installed and `models/all-MiniLM-L6-v2.onnx` is present in the workspace before enabling the job.

Quick start (IntelliJ)

1. Open the project in IntelliJ IDEA.
2. Run | Edit Configurations...
3. Add an Application configuration with these values:
	- Name: TurtleApplication
	- Main class: com.company.turtle.TurtleApplication
	- Module: turtle
	- JRE: Project SDK (Java 21)
	- VM options: -Xmx512m -Dspring.profiles.active=dev
	- Environment variables: SERVER_PORT=8081
	- Working directory: $PROJECT_DIR$
	- Before launch: Make
4. Run the configuration. App will start on the port set by `SERVER_PORT` (example uses 8081).

Quick start (command line)

# Build
mvn -DskipTests package

# Run
java -jar target\\turtle-0.1.0-SNAPSHOT.jar

Health check

Visit:

http://localhost:8081/status

This returns a simple "Turtle is running" message for the scaffold.
