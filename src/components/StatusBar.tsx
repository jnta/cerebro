import { useTheme } from "@/ThemeContext.tsx";

export function StatusBar() {
  const { theme, toggleTheme } = useTheme();

  return (
    <footer className="h-8 border-t border-synapse-border bg-background-light dark:bg-background-dark flex items-center px-4 justify-between shrink-0">
      <div className="flex gap-4 items-center">
        <button
          id="theme-toggle"
          onClick={toggleTheme}
          title={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
          className="flex items-center gap-1 text-slate-500 hover:text-slate-300 dark:hover:text-slate-200 transition-colors duration-150"
        >
          <span className="material-symbols-outlined text-[14px] leading-none">
            {theme === "dark" ? "light_mode" : "dark_mode"}
          </span>
          <span className="text-[10px] font-bold tracking-widest uppercase">
            {theme === "dark" ? "Light" : "Dark"}
          </span>
        </button>

        <div className="h-4 w-px bg-synapse-border" />

        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse shadow-[0_0_5px_rgba(236,91,19,0.6)]" />
          <span className="text-[10px] text-slate-500 font-bold tracking-widest uppercase">Synapse Linked</span>
        </div>

        <div className="h-4 w-px bg-synapse-border" />

        <div className="flex items-center gap-1.5 text-slate-600">
          <span className="text-[10px] uppercase font-bold tracking-widest">UTF-8</span>
        </div>
      </div>

      <div className="text-[10px] text-slate-600 font-mono flex gap-4">
        <span>MODIFIED: OCT 24, 2023</span>
        <span>COORD: 45.32 -122.34</span>
      </div>
    </footer>
  );
}
