# Agent Operations: Synapse

Welcome, Agent. This repository is for **Synapse**, a local-first note-taking system focused on interconnected knowledge and semantic discovery.

## 🧠 Project Vision & Philosophy

Synapse is built to facilitate deep thinking through association. Its primary goal is to turn a collection of isolated notes into a cohesive, navigable knowledge graph.

*   **Bidirectional Linking:** Notes are connected via semantic links, creating a web of information.
*   **Graph Visualization:** A visual representation of the note network to identify clusters and gaps in knowledge.
*   **Semantic Discovery:** Beyond simple keyword search, Synapse uses vector embeddings for semantic search to find related concepts.
*   **AI Integration (RAG):** AI agents (local via Ollama or cloud-based) use the local vault as context for Retrieval-Augmented Generation (RAG).

## 🛠 Tech Stack

*   **Runtime:** Kotlin Multiplatform (KMP) with Compose Multiplatform.
*   **Storage:** Local-first Markdown (`.md`) files.
*   **Intelligence:** 
    *   Local: Ollama / llama.cpp.
    *   Cloud: Integration with standard AI providers.
*   **Vector DB:** `sqlite-vec` for local semantic search and embeddings.
*   **Performance:** High-performance, low-latency UI for rapid navigation and search.

## 📂 Repository Map

*   `shared/`: Core business logic, domain models, and cross-platform code.
*   `desktopApp/`: Desktop-specific implementation and UI entry point.
*   `docs/`: Project documentation and architecture records.
*   `.agents/`: Agent skills, workflows, and automation logic.
*   `.scratch/`: Local issue tracker and PRD storage.

## 🤖 Agent Workflows

### 1. Issue Tracking
Issues and tasks are tracked as local Markdown files in the `.scratch/` directory.
*   **Conventions:** See [issue-tracker.md](docs/agents/issue-tracker.md).
*   **Status:** Use labels like `Status: ready-for-agent` to signify triage state.

### 2. Triage & Roles
We use a standard set of triage labels to manage work handoffs between humans and agents.
*   **Vocabulary:** See [triage-labels.md](docs/agents/triage-labels.md).

### 3. Domain Documentation
Engineering agents should adhere to the domain language defined in the project's context.
*   **Strategy:** See [domain.md](docs/agents/domain.md).
*   **Constraint:** Use terms from the glossary in `CONTEXT.md` (once established).

## 🛠 Available Skills
Agents have access to specialized skills in `.agents/skills/`:
*   `kotlin-specialist`: Idiomatic Kotlin and Coroutines patterns.
*   `compose-multiplatform-patterns`: UI best practices for the Synapse editor.
*   `grill-with-docs`: For refining plans against the project's domain model.
*   `diagnose`: For debugging and performance analysis.
