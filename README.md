# Equilibrio

This repository contains **all parts of the project**:
- `backend/` → server-side code (API, database, etc.)
- `frontend/` → web app
- `mobile/` → mobile app
- `docs/` → documentation

---

## 🚀 Contribution Rules

### General
- **Always run `git pull` before starting work** to make sure your repo is up-to-date.
- **Never run `git add .` in the root folder.**
  This will accidentally add changes from other teams’ folders.
- Each team member should **only commit inside their folder + docs**.
- Try to always first push to a branch, then open a PR.

### Simple as:
```bash
# Stage only backend + docs
git add backend/ docs/ README.md
git commit -m "Backend: describe your change here"
git push

# Stage only frontend + docs
git add frontend/ docs/ README.md
git commit -m "Frontend: describe your change here"
git push

# Stage only mobile + docs
git add mobile/ docs/ README.md
git commit -m "Mobile: describe your change here"
git push