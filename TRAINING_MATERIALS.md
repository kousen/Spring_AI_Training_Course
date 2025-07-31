# Training Materials

This repository includes comprehensive Spring AI training materials:

## Core Materials
- **`slides.md`** - Interactive Slidev presentation (29KB)
- **`labs.md`** - 15 progressive lab exercises
- **`README.md`** - Complete setup and usage guide
- **`CLAUDE.md`** - Developer guidance and course structure

## Exported Presentations
- **`slides-export.pdf`** - PDF version (499KB) - *GitHub compatible*
- **`slides-export.pptx`** - PowerPoint version (32MB) - *Large file, may not display in GitHub web interface*

## Assets
- **`public/images/`** - Presentation images for Slidev
- **`package.json`** - Slidev dependencies and export configuration

## Usage

### Interactive Presentation
```bash
npm install -g @slidev/cli
slidev slides.md
```

### Export Options
```bash
# PDF export
slidev export slides.md --format pdf

# PowerPoint export  
slidev export slides.md --format pptx
```

## Note on Large Files
The PPTX export (~32MB) may not be visible in GitHub's web interface due to size, but is included in the repository. If you need to reduce file size, consider:
- Using PDF format for distribution (499KB)
- Re-exporting PPTX with lower image quality
- Using Git LFS for large presentation files

All training materials are production-ready for enterprise Spring AI workshops.