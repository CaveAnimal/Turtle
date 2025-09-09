# Contributing & Local run instructions

These notes show how to run the export/import runners and run tests on Windows (PowerShell).

Prerequisites
- Java 21
- Maven 3.8+
- Git

Run unit tests

Open PowerShell in the repository root and run:

```powershell
mvn test
```

Run the JSONL exporter (writes `data/vectors-export.jsonl`)

Two options:
- Run the app with the property enabled for the one-time exporter:

```powershell
mvn spring-boot:run -Dturtle.vector.migrate-to-jsonl=true
```

- Or use the helper script (which runs the Maven command):

```powershell
.\scripts\export.ps1
```

Run the JSONL importer (reads `data/vectors-export.jsonl` and writes files to `data/vectors`)

```powershell
mvn spring-boot:run -Dturtle.vector.import-jsonl=true
# or
.\scripts\import.ps1
```

Enable Lucene import (experimental)
- The project includes a Lucene import runner skeleton. To enable it you must add Lucene dependencies to `pom.xml` and set the property:

```powershell
# add the property when running the app
mvn spring-boot:run -Dturtle.vector.import-to-lucene=true
```

Notes
- The `OnnxEmbeddingService` is reflection-safe and falls back to a NoOp embedding if the ONNX runtime or model are not available.
- Lucene integration is intentionally optional to keep CI and development machines free from large native dependencies.

CI and troubleshooting

- Running in CI: ensure Maven and Java 21 are installed on the CI runner. A simple GitHub Actions job example:

```yaml
name: CI
on: [push, pull_request]
jobs:
	build:
		runs-on: ubuntu-latest
		steps:
			- uses: actions/checkout@v4
			- name: Set up JDK 21
				uses: actions/setup-java@v4
				with:
					java-version: '21'
			- name: Build and test
				run: mvn -B -DskipTests=false test
```

- Troubleshooting tips:
	- If `mvn test` fails with missing Lucene artifacts, make sure you did not enable `turtle.vector.use-lucene` in CI or add lucene dependencies to `pom.xml`.
	- If ONNX model loading fails, verify `turtle.embedding.model-path` points to an existing `.onnx` file and that `onnxruntime` is available on the runner. The `OnnxEmbeddingService` logs reflection init status at startup.
	- If the exporters/importers don't run, confirm the properties `turtle.vector.migrate-to-jsonl` and `turtle.vector.import-jsonl` are set to `true` when starting the app.

Contact
- For issues with the build or runtime, open an issue in the repo with logs from `mvn -DskipTests=false test` and `java -jar target/*.jar` output.

