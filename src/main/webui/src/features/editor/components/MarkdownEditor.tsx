import { useRef } from 'react'
import { useCodeMirror } from '@/features/editor/hooks/useCodeMirror'

interface Props {
  content: string
  onChange?: (value: string) => void
}

export function MarkdownEditor({ content, onChange }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  useCodeMirror(containerRef, content, onChange)

  return (
    <div
      ref={containerRef}
      className="flex-1 overflow-hidden flex flex-col min-h-0"
    />
  )
}
