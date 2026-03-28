import { useEffect, useState } from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { Markdown } from '@tiptap/markdown'
import Placeholder from '@tiptap/extension-placeholder'

interface Props {
  content: string
  onChange?: (value: string) => void
}

export function MarkdownEditor({ content, onChange }: Props) {
  const [isReady, setIsReady] = useState(false)
  const editor = useEditor({
    extensions: [
      StarterKit.configure(),
      Markdown.configure(),
      Placeholder.configure({
        placeholder: 'Synaptic spark goes here...',
      }),
    ],
    content: content || '',
    // @ts-ignore
    contentType: 'markdown',
    onUpdate: ({ editor }) => {
      if (onChange) {
        const markdown = (editor.storage as any)?.markdown?.getMarkdown?.()
        if (markdown !== undefined) {
          onChange(markdown)
        }
      }
    },
    onCreate: () => {
      setIsReady(true)
    },
    editorProps: {
      attributes: {
        class: 'tiptap focus:outline-none min-h-full',
      },
    },
  })

  useEffect(() => {
    if (editor && !editor.isFocused && isReady) {
      const currentMarkdown = (editor.storage as any)?.markdown?.getMarkdown?.()
      if (content !== currentMarkdown) {
        // @ts-ignore
        editor.commands.setContent(content || '', { contentType: 'markdown' })
      }
    }
  }, [content, editor, isReady])

  if (!editor || !isReady) {
    return (
      <div className="flex-1 bg-[var(--color-editor-bg)] flex items-center justify-center text-[var(--color-text-muted)] text-[12px]">
        Igniting synapses...
      </div>
    )
  }

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-[var(--color-editor-bg)] overflow-hidden">
      <EditorContent 
        editor={editor} 
        className="flex-1 overflow-y-auto w-full h-full" 
      />
    </div>
  )
}
