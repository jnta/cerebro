import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { StatusBar } from "./StatusBar.tsx";
import * as ThemeContext from "@/ThemeContext.tsx";

// Mock the ThemeContext hook
vi.mock("@/ThemeContext.tsx", () => ({
  useTheme: vi.fn(),
}));

describe("StatusBar", () => {
  const mockToggleTheme = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should render light mode option when current theme is dark", () => {
    (ThemeContext.useTheme as any).mockReturnValue({
      theme: "dark",
      toggleTheme: mockToggleTheme,
    });

    render(<StatusBar />);
    
    expect(screen.getByText("Light")).toBeInTheDocument();
    expect(screen.getByText("light_mode")).toBeInTheDocument();
    expect(screen.getByTitle("Switch to light mode")).toBeInTheDocument();
  });

  it("should render dark mode option when current theme is light", () => {
    (ThemeContext.useTheme as any).mockReturnValue({
      theme: "light",
      toggleTheme: mockToggleTheme,
    });

    render(<StatusBar />);
    
    expect(screen.getByText("Dark")).toBeInTheDocument();
    expect(screen.getByText("dark_mode")).toBeInTheDocument();
    expect(screen.getByTitle("Switch to dark mode")).toBeInTheDocument();
  });

  it("should call toggleTheme when the theme toggle button is clicked", () => {
    (ThemeContext.useTheme as any).mockReturnValue({
      theme: "dark",
      toggleTheme: mockToggleTheme,
    });

    render(<StatusBar />);
    
    const toggleButton = screen.getByTitle(/Switch to light mode/i);
    fireEvent.click(toggleButton);

    expect(mockToggleTheme).toHaveBeenCalledTimes(1);
  });

  it("should display core status indicators", () => {
    (ThemeContext.useTheme as any).mockReturnValue({
      theme: "dark",
      toggleTheme: mockToggleTheme,
    });

    render(<StatusBar />);
    
    expect(screen.getByText("Synapse Linked")).toBeInTheDocument();
    expect(screen.getByText("UTF-8")).toBeInTheDocument();
    expect(screen.getByText(/MODIFIED:/)).toBeInTheDocument();
  });
});
