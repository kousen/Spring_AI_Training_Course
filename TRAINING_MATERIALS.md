# Training Materials

This repository includes comprehensive Spring AI training materials:

## Core Materials
- **`slides.md`** - Interactive Slidev presentation (29KB)
- **`labs.md`** - 15 progressive lab exercises
- **`README.md`** - Complete setup and usage guide
- **`CLAUDE.md`** - Developer guidance and course structure

## Exported Presentations
The slides PDF is built automatically by GitHub Actions on every push to `main` that touches `slides.md`, and published to the rolling [`slides-latest` release](https://github.com/kousen/Spring_AI_Training_Course/releases/latest). Exports are not committed to the repository.

## Assets
- **`public/images/`** - Presentation images for Slidev
- **`package.json`** - Slidev dependencies and export configuration

## Usage

### Interactive Presentation
```bash
npm install
npm run dev
```

### Local PDF Export
```bash
npm run export   # writes spring-ai-slides.pdf (git-ignored)
```

All training materials are production-ready for enterprise Spring AI workshops.
