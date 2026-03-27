import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Tab } from '@/core/types/Tab'

const WELCOME_ID = crypto.randomUUID()

interface EditorState {
  tabs: Tab[]
  activeId: string
  contents: Record<string, string>
  openNew: () => void
  closeTab: (id: string) => void
  setActiveId: (id: string) => void
  setContent: (id: string, content: string) => void
}

export const useEditorStore = create<EditorState>()(
  immer((set) => ({
    tabs: [{ id: WELCOME_ID, name: 'Welcome.md' }],
    activeId: WELCOME_ID,
    contents: {},

    openNew: () =>
      set((state) => {
        const id = crypto.randomUUID()
        state.tabs.push({ id, name: 'Untitled.md' })
        state.activeId = id
      }),

    closeTab: (id) =>
      set((state) => {
        const idx = state.tabs.findIndex((t) => t.id === id)
        state.tabs.splice(idx, 1)
        if (state.activeId === id && state.tabs.length > 0) {
          state.activeId = state.tabs[Math.max(0, idx - 1)].id
        }
        delete state.contents[id]
      }),

    setActiveId: (id) =>
      set((state) => {
        state.activeId = id
      }),

    setContent: (id, content) =>
      set((state) => {
        state.contents[id] = content
      }),
  })),
)
