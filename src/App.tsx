import { ThemeProvider } from "./ThemeContext";
import { TheTopography } from "./components/TheTopography";
import { Editor } from "./components/Editor";

function App() {
  return (
    <ThemeProvider>
      <div className="flex h-screen w-screen overflow-hidden bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 font-sans">
        <TheTopography />
        <main className="flex-1 h-full">
          <Editor />
        </main>
      </div>
    </ThemeProvider>
  );
}

export default App;
