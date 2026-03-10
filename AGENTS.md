## 1. Project Identity & Vision
**Project Name:** Cerebro  
**Tagline:** The AI-Native Cognitive Gym (Active Learning & Desirable Difficulty).  
**Core Philosophy:** "Friction as a Feature." Automation is for clerical tasks (storage/indexing); manual effort is mandated for cognitive tasks (synthesis/reflection).  
**Methodologies:** PARA (Projects, Areas, Resources, Archives) and CODE (Capture, Organize, Distill, Express).

---

## 2. Technical Stack (The "Brain" Specs)
* **Runtime:** Tauri (Rust Backend + React/Tailwind Frontend).
* **Storage:** Local-first Markdown (`.md`) files.
* **Intelligence:** Local LLM via `Ollama`/`llama.cpp` (No cloud by default).
* **Vector DB:** `sqlite-vec` (Local vector search & semantic embeddings).
* **Performance Target:** UI latency for auto-linking/search $< 200ms$.

---

## 3. Operational Guardrails (The "Friction" Logic)
Agents must implement and respect these non-negotiable logic flows:

1.  **The Resonance Filter:** Block "Silent Saves." Any new capture requires a minimum character count of "Original Thought" or a 3-question quiz completion to be committed to the vault.
2.  **Progressive Distillation (Fog of War):** On the second viewing of a note, mask 50% of the text. Force the user to bold key sentences to "unlock" the full view.
3.  **Semantic Decay:** Resources not linked or accessed in 90 days must be flagged for "Synaptic Pruning" (Archive trigger).
4.  **No Passive Summaries:** If a user requests a summary, the AI must provide a quiz instead. Passing the quiz unlocks the summary.

---

## 4. Execution Roadmap (Current Status)

### Milestone 1: The Synapse Core (ACTIVE FOCUS)
* Initialize Tauri + Rust backend with local file-system watcher.
* Integrate `sqlite-vec` and `all-MiniLM-L6-v2` for local RAG.
* Establish semantic search over the local Markdown vault.

### Milestone 2: The Cognitive Gym
* Build the "High-Friction" Capture Modal.
* Implement "Fog of War" masking logic for note rendering.
* Develop PARA suggestion engine based on local context.

### Milestone 3: The Neural Bridge
* Typed Bidirectional Links (e.g., `[[note]]` with labels like `[Contradicts]`).
* Graph Visualization with a "Decay Heatmap" (active links glow, unused fade).

---

## 5. Domain Context for AI Agents
When generating code or features, refer to these note states:
* **Dormant:** Raw capture, low-effort input, hidden from main feed for 48h.
* **Active:** Linked to an Active Project, contains user-generated summary.
* **Mastered:** High-density link count, passed active recall quizzes.

---

## 6. Definition of Done (DoD)
* **Privacy:** Feature works 100% offline; zero network calls for note content.
* **Speed:** Retrieval and embedding generation must not block the main UI thread.
* **Integrity:** Maintain human-readable Markdown files; do not store core knowledge exclusively in the database.

---
