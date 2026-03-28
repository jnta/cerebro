import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Tab } from '@/core/types/Tab'
import { notesApi } from '@/features/notes/api/notesApi'
import type { NoteDTO } from '@/features/notes/api/notesApi'

interface EditorState {
  notes: NoteDTO[]
  folders: string[]
  tabs: Tab[]
  activeId: string | null
  contents: Record<string, string>
  expandedFolders: string[]
  
  fetchNotes: () => Promise<void>
  openNote: (id: string) => Promise<void>
  createNewNote: (title?: string) => Promise<void>
  createFolder: (path?: string) => Promise<void>
  saveNote: (id: string, title: string, content: string) => Promise<void>
  deleteNote: (id: string) => Promise<void>
  
  closeTab: (id: string) => void
  setActiveId: (id: string) => void
  setContent: (id: string, content: string) => void
  toggleFolder: (path: string) => void
  collapseAllFolders: () => void
}

export const useEditorStore = create<EditorState>()(
  immer((set, get) => ({
    notes: [],
    folders: [],
    tabs: [],
    activeId: null,
    contents: {},
    expandedFolders: [],

    fetchNotes: async () => {
      try {
        const [notes, folders] = await Promise.all([
          notesApi.getAll(),
          notesApi.getFolders()
        ])
        set((state) => { 
          state.notes = notes 
          state.folders = folders
        })
      } catch (err) {
        console.error('Failed to fetch notes or folders', err)
      }
    },

    openNote: async (id: string) => {
      if (get().tabs.find(t => t.id === id)) {
        set((state) => { state.activeId = id })
        return
      }

      try {
        const note = await notesApi.getOne(id)
        set((state) => {
          state.tabs.push({ id: note.id, name: note.title })
          state.activeId = note.id
          state.contents[note.id] = note.content
        })
      } catch (err) {
        console.error('Failed to open note', err)
      }
    },

    createNewNote: async (providedTitle?: string) => {
      try {
        const title = providedTitle || window.prompt('Enter note path/title (e.g., Folder/MyNote):')
        if (!title) return // user cancelled
        
        const content = '# ' + title.split('/').pop() + '\n\n'
        const note = await notesApi.create(title, content)
        
        set((state) => {
          state.notes.push(note)
          state.tabs.push({ id: note.id, name: note.title })
          state.activeId = note.id
          state.contents[note.id] = note.content
          
          // Auto-expand the path created
          const parts = note.id.split('/')
          let currentPath = ''
          for (let i = 0; i < parts.length - 1; i++) {
            currentPath = currentPath ? currentPath + '/' + parts[i] : parts[i]
            if (!state.expandedFolders.includes(currentPath)) {
              state.expandedFolders.push(currentPath)
            }
          }
        })
      } catch (err) {
        console.error('Failed to create new note', err)
      }
    },

    createFolder: async (providedPath?: string) => {
      try {
        const path = providedPath || window.prompt('Enter folder path (e.g., Projects/Web):')
        if (!path) return
        
        await notesApi.createFolder(path)
        set((state) => {
          if (!state.folders.includes(path)) {
            state.folders.push(path)
          }
          if (!state.expandedFolders.includes(path)) {
            state.expandedFolders.push(path)
          }
        })
      } catch (err) {
        console.error('Failed to create folder', err)
      }
    },

    saveNote: async (id: string, title: string, content: string) => {
      try {
        const updated = await notesApi.update(id, title, content)
        set((state) => {
          const tab = state.tabs.find(t => t.id === id)
          if (tab) tab.name = updated.title
          
          const noteIndex = state.notes.findIndex(n => n.id === id)
          if (noteIndex !== -1) state.notes[noteIndex] = updated

          state.contents[id] = updated.content
        })
      } catch (err) {
        console.error('Failed to save note', err)
      }
    },

    deleteNote: async (id: string) => {
      try {
        await notesApi.delete(id)
        set((state) => {
          state.notes = state.notes.filter(n => n.id !== id)
          
          const idx = state.tabs.findIndex((t) => t.id === id)
          if (idx !== -1) {
            state.tabs.splice(idx, 1)
            if (state.activeId === id) {
              state.activeId = state.tabs.length > 0 ? state.tabs[Math.max(0, idx - 1)].id : null
            }
          }
          delete state.contents[id]
        })
      } catch (err) {
        console.error('Failed to delete note', err)
      }
    },

    closeTab: (id) =>
      set((state) => {
        const idx = state.tabs.findIndex((t) => t.id === id)
        if (idx !== -1) {
          state.tabs.splice(idx, 1)
          if (state.activeId === id) {
            state.activeId = state.tabs.length > 0 ? state.tabs[Math.max(0, idx - 1)].id : null
          }
          delete state.contents[id]
        }
      }),

    setActiveId: (id) =>
      set((state) => {
        state.activeId = id
      }),

    setContent: (id, content) =>
      set((state) => {
        state.contents[id] = content
      }),
      
    toggleFolder: (path: string) =>
      set((state) => {
        const idx = state.expandedFolders.indexOf(path)
        if (idx === -1) {
          state.expandedFolders.push(path)
        } else {
          state.expandedFolders.splice(idx, 1)
        }
      }),
      
    collapseAllFolders: () =>
      set((state) => {
        state.expandedFolders = []
      }),
  })),
)
