import { useEffect, useRef, useState } from "react";
import { createNote } from "@/services/tauri.ts";
import type { NoteEntry } from "@/services/tauri.ts";

type NoteType = "daily" | "resource" | "project";

interface NoteTypeOption {
  type: NoteType;
  label: string;
  description: string;
  icon: string;
  folder: string;
  color: string;
}

const NOTE_TYPES: NoteTypeOption[] = [
  {
    type: "daily",
    label: "Daily Note",
    description: "Auto-named with today's date. Opens if already exists.",
    icon: "calendar_today",
    folder: "daily-notes",
    color: "text-emerald-400",
  },
  {
    type: "resource",
    label: "Resource Note",
    description: "Articles, books, references and raw captures.",
    icon: "library_books",
    folder: "resource-notes",
    color: "text-sky-400",
  },
  {
    type: "project",
    label: "Project Note",
    description: "Linked to an active execution folder.",
    icon: "rocket_launch",
    folder: "project-notes",
    color: "text-violet-400",
  },
];

function todayDateName(): string {
  return new Date().toISOString().slice(0, 10);
}

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: (note: NoteEntry) => void;
}

export function CreateNoteModal({ open, onClose, onCreated }: Props) {
  const [selected, setSelected] = useState<NoteType | null>(null);
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!open) {
      setSelected(null);
      setName("");
      setError("");
      setLoading(false);
    }
  }, [open]);

  useEffect(() => {
    if (selected && selected !== "daily") {
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [selected]);

  useEffect(() => {
    if (!open) return;
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open, onClose]);

  const handleConfirm = async () => {
    if (!selected) return;
    const option = NOTE_TYPES.find((o) => o.type === selected)!;
    const noteName = selected === "daily" ? todayDateName() : name.trim();

    if (!noteName) {
      setError("Please enter a name for the note.");
      inputRef.current?.focus();
      return;
    }

    setError("");
    setLoading(true);
    try {
      const entry = await createNote(option.folder, noteName);
      onCreated(entry);
      onClose();
    } catch (e) {
      setError(String(e));
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleConfirm();
  };

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" />

      <div className="relative z-10 w-full max-w-md mx-4 bg-background-dark border border-synapse-border rounded-2xl shadow-2xl shadow-black/60 overflow-hidden animate-in">
        <div className="px-6 pt-6 pb-4 border-b border-synapse-border/50 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-xl text-primary">add_circle</span>
            <h2 className="text-base font-bold text-white tracking-tight">New Note</h2>
          </div>
          <button
            onClick={onClose}
            className="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-white/5 text-slate-500 hover:text-slate-300 transition-colors"
            aria-label="Close"
          >
            <span className="material-symbols-outlined text-lg">close</span>
          </button>
        </div>

        <div className="px-6 pt-5 pb-2">
          <p className="text-xs font-semibold uppercase tracking-widest text-slate-500 mb-3">Select type</p>
          <div className="space-y-2">
            {NOTE_TYPES.map((opt) => (
              <button
                key={opt.type}
                onClick={() => setSelected(opt.type)}
                className={`w-full flex items-center gap-4 p-3.5 rounded-xl border text-left transition-all duration-150 ${
                  selected === opt.type
                    ? "border-primary/60 bg-primary/8 shadow-sm shadow-primary/10"
                    : "border-synapse-border hover:border-white/15 hover:bg-white/3"
                }`}
              >
                <span className={`material-symbols-outlined text-2xl shrink-0 ${opt.color}`}>{opt.icon}</span>
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-white leading-tight">{opt.label}</p>
                  <p className="text-xs text-slate-500 mt-0.5 leading-snug">{opt.description}</p>
                </div>
                <span
                  className={`ml-auto shrink-0 w-4 h-4 rounded-full border-2 flex items-center justify-center transition-all ${
                    selected === opt.type ? "border-primary bg-primary" : "border-slate-600"
                  }`}
                >
                  {selected === opt.type && (
                    <span className="w-1.5 h-1.5 rounded-full bg-white block" />
                  )}
                </span>
              </button>
            ))}
          </div>
        </div>

        <div className="px-6 pt-4 pb-6">
          {selected && selected !== "daily" && (
            <div className="mb-4">
              <label className="block text-xs font-semibold uppercase tracking-widest text-slate-500 mb-2">
                Note name
              </label>
              <input
                ref={inputRef}
                value={name}
                onChange={(e) => { setName(e.target.value); setError(""); }}
                onKeyDown={handleKeyDown}
                placeholder={
                  selected === "resource" ? "e.g., Atomic Habits Summary" : "e.g., Project Phoenix"
                }
                className="w-full bg-black/30 border border-synapse-border rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-600 focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary/60 transition-all"
              />
            </div>
          )}

          {selected === "daily" && (
            <div className="mb-4 flex items-center gap-2 px-3 py-2 bg-emerald-500/8 border border-emerald-500/20 rounded-lg">
              <span className="material-symbols-outlined text-sm text-emerald-400">event</span>
              <span className="text-xs text-emerald-300 font-medium">
                Will create or open <span className="font-bold">{todayDateName()}.md</span>
              </span>
            </div>
          )}

          {error && (
            <p className="mb-3 text-xs text-red-400 flex items-center gap-1.5">
              <span className="material-symbols-outlined text-sm">error</span>
              {error}
            </p>
          )}

          <div className="flex gap-3">
            <button
              onClick={onClose}
              className="flex-1 py-2 rounded-lg border border-synapse-border text-sm text-slate-400 hover:border-white/20 hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              disabled={!selected || loading}
              className="flex-1 py-2 rounded-lg bg-primary text-white text-sm font-semibold hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-md shadow-primary/20 flex items-center justify-center gap-2"
            >
              {loading ? (
                <span className="material-symbols-outlined text-base animate-spin">progress_activity</span>
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">add</span>
                  Create
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
