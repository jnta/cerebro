# Synapse 🧠
> Intelligent Note-Taking & Knowledge Graphing.

Synapse is a local-first, AI-native knowledge management system focused on interconnected thoughts. It leverages semantic search and graph visualization to help you discover hidden connections in your local note vault.

## 🚀 Getting Started

### Prerequisites
- **JDK 21** (Required for Hot Reload)
- **Kotlin 2.1.20**
- **Gradle 8.10.2**

### Running the Application

To run the application with **Hot Reload** (supported on Desktop/JVM):

```bash
./run-hot.sh
```

Or using Gradle directly:

```bash
./gradlew :desktopApp:hotRunJvm
```

### Key Commands
- `./gradlew :desktopApp:run` - Standard run (no hot reload).
- `./gradlew :desktopApp:hotRunJvm` - Run with real-time UI updates on save.

## 🛠 Tech Stack
- **UI:** Compose Multiplatform (Kotlin)
- **Backend:** Kotlin Multiplatform / JVM
- **Database:** Local Markdown + `sqlite-vec` (Semantic Search)
- **AI:** RAG capabilities with support for local (Ollama / llama.cpp) and cloud AI providers.

## ✨ Key Features
- **Bidirectional Linking:** Connect notes and navigate your knowledge web.
- **Graph Visualization:** See your notes as a living, breathing network.
- **Semantic Search:** Find notes based on meaning, not just keywords.
- **AI-Powered RAG:** Use your local notes as a private brain for AI interactions.

---
*Built for knowledge synthesis and discovery.*