#!/usr/bin/env python3
"""
Renames package from com.muhofy.korex → com.korexx across the entire repo.
Run from the root of the Korex repository:
    python3 rename-package.py
"""

import os
import re
import shutil
from pathlib import Path

OLD_PKG       = "com.korexx"
NEW_PKG       = "com.termux"
OLD_PKG_PATH  = "com/korexx"
NEW_PKG_PATH  = "com/termux"

# File extensions to do text replacement in
TEXT_EXTENSIONS = {
    ".kt", ".java", ".xml", ".gradle", ".kts",
    ".c", ".h", ".cpp", ".mk", ".pro", ".yml",
    ".yaml", ".json", ".md", ".txt", ".S"
}

# Directories to skip entirely
SKIP_DIRS = {".git", ".gradle", "build", ".idea", "__pycache__"}


def should_process(path: Path) -> bool:
    for part in path.parts:
        if part in SKIP_DIRS:
            return False
    return path.suffix in TEXT_EXTENSIONS


def replace_in_file(path: Path) -> bool:
    try:
        content = path.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        print(f"  SKIP (read error): {path} — {e}")
        return False

    # Replace both dot-separated and slash-separated forms
    new_content = content.replace(OLD_PKG, NEW_PKG)
    new_content = new_content.replace(OLD_PKG_PATH, NEW_PKG_PATH)
    # Also handle underscore form used in JNI: com_muhofy_korex → com_korexx
    new_content = new_content.replace(
        OLD_PKG.replace(".", "_"),
        NEW_PKG.replace(".", "_")
    )

    if new_content != content:
        path.write_text(new_content, encoding="utf-8")
        return True
    return False


def rename_directories(root: Path):
    """
    Rename source directories from com/muhofy/korex → com/korexx.
    Must be done after text replacement, bottom-up to avoid path conflicts.
    """
    old_dirs = sorted(root.rglob(OLD_PKG_PATH), reverse=True)
    for old_dir in old_dirs:
        if not old_dir.is_dir():
            continue
        # Build new path
        new_dir = Path(str(old_dir).replace(OLD_PKG_PATH, NEW_PKG_PATH))
        if new_dir.exists():
            print(f"  SKIP (already exists): {new_dir}")
            continue
        new_dir.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(old_dir), str(new_dir))
        print(f"  MOVE dir: {old_dir} → {new_dir}")

    # Clean up empty leftover directories
    for d in sorted(root.rglob("muhofy"), reverse=True):
        if d.is_dir() and not any(d.iterdir()):
            d.rmdir()
            print(f"  REMOVE empty dir: {d}")


def main():
    root = Path(".").resolve()
    print(f"Root: {root}")
    print(f"Replacing: {OLD_PKG} → {NEW_PKG}\n")

    changed_files = []

    for path in sorted(root.rglob("*")):
        if not path.is_file():
            continue
        if not should_process(path):
            continue
        if replace_in_file(path):
            changed_files.append(path)
            print(f"  UPDATED: {path.relative_to(root)}")

    print(f"\nText replacement done. {len(changed_files)} files updated.")
    print("\nRenaming source directories...")
    rename_directories(root)
    print("\nDone. Review changes with: git diff")
    print("Then commit with: git add -A && git commit -m 'refactor(app): rename package to com.korexx'")


if __name__ == "__main__":
    main()