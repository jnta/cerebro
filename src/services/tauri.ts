import { invoke } from "@tauri-apps/api/core";

export interface NoteEntry {
  name: string;
  folder: string;
  modified: number;
}

export interface VaultNotes {
  daily: NoteEntry[];
  resources: NoteEntry[];
  projects: NoteEntry[];
}

export function listVaultNotes(): Promise<VaultNotes> {
  return invoke<VaultNotes>("list_vault_notes");
}

export function readNote(folder: string, name: string): Promise<string> {
  return invoke<string>("read_note", { folder, name });
}

export function searchNotes(query: string, limit: number): Promise<[string, number][]> {
  return invoke<[string, number][]>("search_notes", { query, limit });
}

export function createNote(folder: string, name: string): Promise<NoteEntry> {
  return invoke<NoteEntry>("create_note", { folder, name });
}
