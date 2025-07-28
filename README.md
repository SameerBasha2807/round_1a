# 📘 Adobe Hackathon Round 1A – Structured Outline Extraction (Java)

This solution extracts structured outlines from documents (PDF, DOCX, PPTX), identifying:
- **Title**
- **H1, H2, H3** level headings
- Along with **page numbers** and **hierarchical structure**

---

## 🚀 How to Compile and Run (Java)

### 🧪 Step 1: Compile Java Code

```bash
javac -cp "lib/*" -d classes src/Filename.java
```

> Replace `Filename.java` with your actual source filename.

### ▶️ Step 2: Run the Program

```bash
java -cp "lib/*;classes" ClassName
```

> Replace `ClassName` with your Java class containing the `main()` method.

---

## 📦 Required Java Libraries (JARs)

Make sure all the following `.jar` files are in your `lib/` folder:

| JAR File | Purpose |
|----------|---------|
| `pdfbox-*.jar` | Core PDF parsing (Apache PDFBox) |
| `fontbox-*.jar` | Font support for PDFBox |
| `commons-logging-*.jar` | Logging framework used by PDFBox |
| `poi-*.jar` | Apache POI core for DOCX/PPTX |
| `poi-ooxml-*.jar` | OpenXML format support (DOCX/PPTX) |
| `poi-ooxml-schemas-*.jar` | Schema definitions for DOCX/PPTX |
| `xmlbeans-*.jar` | XML parser for OOXML |
| `ooxml-schemas-*.jar` | Advanced schema support |
| `curvesapi-*.jar` | Improves handling of curved shapes |
| `commons-collections4-*.jar` | Collections used by POI |

---

## 📂 Typical Project Structure

```
.
├── lib/                  # All required JAR files
├── src/                 # Java source files
│   └── Filename.java
├── classes/             # Compiled .class files
├── input/               # PDF/DOCX/PPTX input files
├── output/              # Output JSON files
```

---

## 🐳 Docker

You can run the Java solution in a Docker container to simulate the competition environment.

### 🛠 Build Docker Image

```bash
docker build --platform linux/amd64 -t mysolutionname:uniqueid .
```

### 🚀 Run in Docker

```bash
docker run --rm -v $(pwd)/input:/app/input -v $(pwd)/output:/app/output --network none mysolutionname:uniqueid
```

---

## ✅ Output Format

The output is a `JSON` file like:

```json
{
  "title": "Understanding AI",
  "outline": [
    { "level": "H1", "text": "Introduction", "page": 1 },
    { "level": "H2", "text": "What is AI?", "page": 2 },
    { "level": "H3", "text": "History of AI", "page": 3 }
  ]
}
```

Each document from the `/input` directory will generate a corresponding `.json` in `/output`.
-------------------------------------------------------------------------------------------------------------
## execution steps

 Build Docker Image

```bash
docker build --platform linux/amd64 -t mysolutionname:uniqueid .
```

 Run in Docker

```bash
docker run --rm -v $(pwd)/input:/app/input -v $(pwd)/output:/app/output --network none mysolutionname:uniqueid
```