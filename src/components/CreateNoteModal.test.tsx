import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { CreateNoteModal } from "./CreateNoteModal";
import * as tauriService from "@/services/tauri";

// Mock the tauri service
vi.mock("@/services/tauri", () => ({
  createNote: vi.fn(),
}));

describe("CreateNoteModal", () => {
  const mockOnClose = vi.fn();
  const mockOnCreated = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should not render when open is false", () => {
    render(<CreateNoteModal open={false} onClose={mockOnClose} onCreated={mockOnCreated} />);
    expect(screen.queryByText("New Note")).not.toBeInTheDocument();
  });

  it("should render when open is true", () => {
    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    expect(screen.getByText("New Note")).toBeInTheDocument();
    expect(screen.getByText("Select type")).toBeInTheDocument();
    expect(screen.getByText("Daily Note")).toBeInTheDocument();
    expect(screen.getByText("Resource Note")).toBeInTheDocument();
    expect(screen.getByText("Project Note")).toBeInTheDocument();
  });

  it("should call onClose when close button is clicked", () => {
    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    const closeButton = screen.getByLabelText("Close");
    fireEvent.click(closeButton);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("should call onClose when Cancel button is clicked", () => {
    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    const cancelButton = screen.getByText("Cancel");
    fireEvent.click(cancelButton);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("should show date preview for daily note and create it without input", async () => {
    const mockNote = { name: "2024-03-12", folder: "daily-notes", modified: 123 };
    (tauriService.createNote as any).mockResolvedValue(mockNote);

    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    
    // Select Daily Note (it's often first but let's be explicit)
    fireEvent.click(screen.getByText("Daily Note"));
    
    expect(screen.getByText(/Will create or open/)).toBeInTheDocument();
    
    fireEvent.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(tauriService.createNote).toHaveBeenCalledWith("daily-notes", expect.any(String));
      expect(mockOnCreated).toHaveBeenCalledWith(mockNote);
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("should require name for Resource Note", async () => {
    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    
    fireEvent.click(screen.getByText("Resource Note"));
    expect(screen.getByPlaceholderText(/e.g., Atomic Habits Summary/i)).toBeInTheDocument();
    
    // Attempt create without name
    fireEvent.click(screen.getByRole("button", { name: /Create/i }));
    
    expect(screen.getByText("Please enter a name for the note.")).toBeInTheDocument();
    expect(tauriService.createNote).not.toHaveBeenCalled();
  });

  it("should create Resource Note when name is provided", async () => {
    const mockNote = { name: "My Resource", folder: "resource-notes", modified: 123 };
    (tauriService.createNote as any).mockResolvedValue(mockNote);

    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    
    fireEvent.click(screen.getByText("Resource Note"));
    const input = screen.getByPlaceholderText(/e.g., Atomic Habits Summary/i);
    fireEvent.change(input, { target: { value: "My Resource" } });
    
    fireEvent.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(tauriService.createNote).toHaveBeenCalledWith("resource-notes", "My Resource");
      expect(mockOnCreated).toHaveBeenCalledWith(mockNote);
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("should show error message when creation fails", async () => {
    (tauriService.createNote as any).mockRejectedValue("Disk full");

    render(<CreateNoteModal open={true} onClose={mockOnClose} onCreated={mockOnCreated} />);
    
    fireEvent.click(screen.getByText("Daily Note"));
    fireEvent.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(screen.getByText("Disk full")).toBeInTheDocument();
    });
    expect(mockOnCreated).not.toHaveBeenCalled();
    expect(mockOnClose).not.toHaveBeenCalled();
  });
});
