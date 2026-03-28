export interface NoteDTO {
  id: string;
  title: string;
  content: string;
  tags: string[];
  createdAt: string;
  lastAccessedAt: string;
  state: string;
}

export const notesApi = {
  getAll: async (): Promise<NoteDTO[]> => {
    const res = await fetch('/api/v1/notes');
    if (!res.ok) throw new Error('Failed to fetch notes');
    return res.json();
  },
  getOne: async (id: string): Promise<NoteDTO> => {
    const res = await fetch(`/api/v1/notes/${id}`);
    if (!res.ok) throw new Error('Failed to fetch note');
    return res.json();
  },
  create: async (title: string, content: string): Promise<NoteDTO> => {
    const res = await fetch('/api/v1/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, content }),
    });
    if (!res.ok) throw new Error('Failed to create note');
    return res.json();
  },
  update: async (id: string, title: string, content: string): Promise<NoteDTO> => {
    const res = await fetch(`/api/v1/notes/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, content }),
    });
    if (!res.ok) throw new Error('Failed to update note');
    return res.json();
  },
  delete: async (id: string): Promise<void> => {
    const res = await fetch(`/api/v1/notes/${id}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete note');
  },
  createFolder: async (path: string): Promise<void> => {
    const res = await fetch('/api/v1/notes/folders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ path }),
    });
    if (!res.ok) throw new Error('Failed to create folder');
  },
  getFolders: async (): Promise<string[]> => {
    const res = await fetch('/api/v1/notes/folders');
    if (!res.ok) throw new Error('Failed to fetch folders');
    return res.json();
  }
};
