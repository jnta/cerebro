import { useState } from 'react'
import { TabBar } from '@/features/editor/components/TabBar'
import { MarkdownEditor } from '@/features/editor/components/MarkdownEditor'
import { useEditorStore } from '@/features/editor/store/useEditorStore'

const INITIAL_CONTENT = `# Welcome to Synapse

Start writing your thoughts here...

## Getting Started

- Use **bold** and *italic* for emphasis
- Create links with \`[[note title]]\`
- Add tags with \`#topic\`
`

function IconExplorer() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M3 6h18M3 12h18M3 18h18" />
    </svg>
  )
}

export function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const { tabs, activeId, contents, setContent } = useEditorStore()

  const activeContent = contents[activeId] ?? INITIAL_CONTENT

  return (
    <div className="grid h-full w-full overflow-hidden" style={{ gridTemplateColumns: 'var(--width-activitybar) auto 1fr' }}>
      <nav
        className="flex flex-col items-center py-2 gap-0.5 bg-[var(--color-bg-secondary)] border-r border-[var(--color-border)] z-10"
        aria-label="Activity bar"
      >
        <button
          className={[
            'relative flex items-center justify-center w-9 h-9 rounded transition-colors duration-[var(--duration-fast)]',
            'hover:bg-[var(--color-surface-hover)]',
            sidebarOpen
              ? 'text-[var(--color-text-primary)] before:absolute before:left-[-3px] before:top-1/2 before:-translate-y-1/2 before:w-0.5 before:h-4 before:bg-[var(--color-accent)] before:rounded-r'
              : 'text-[var(--color-text-secondary)]',
          ].join(' ')}
          onClick={() => setSidebarOpen((o) => !o)}
          aria-label="Toggle Explorer"
          title="Explorer"
        >
          <IconExplorer />
        </button>
      </nav>

      <aside
        className={[
          'flex flex-col bg-[var(--color-sidebar-bg)] border-r border-[var(--color-border)] overflow-hidden transition-all duration-[var(--duration-slow)]',
          sidebarOpen ? 'w-[var(--width-sidebar)] opacity-100' : 'w-0 opacity-0 border-r-0',
        ].join(' ')}
        aria-label="Explorer"
        aria-hidden={!sidebarOpen}
      >
        <div className="px-4 py-3 text-[11px] font-semibold tracking-widest uppercase text-[var(--color-text-secondary)] border-b border-[var(--color-border-subtle)] shrink-0 whitespace-nowrap overflow-hidden">
          Explorer
        </div>
        <div className="flex-1 overflow-auto p-2">
          <p className="text-[11px] text-[var(--color-text-muted)] px-2 whitespace-nowrap">No vault open</p>
        </div>
      </aside>

      <main className="flex flex-col overflow-hidden bg-[var(--color-editor-bg)] min-w-0">
        <TabBar tabs={tabs} activeId={activeId} />
        {tabs.length > 0 ? (
          <MarkdownEditor
            key={activeId}
            content={activeContent}
            onChange={(v) => setContent(activeId, v)}
          />
        ) : (
          <div className="flex-1 flex items-center justify-center text-[13px] text-[var(--color-text-muted)]">
            Press + to open a new tab
          </div>
        )}
      </main>
    </div>
  )
}
