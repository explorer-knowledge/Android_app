# CI_CD_SETUP.md — Build the APK via GitHub Actions (no local build needed)

This lets GitHub build your APK in the cloud every time you push code. You never need Android Studio installed to get an installable APK — just push, wait a few minutes, download from the Actions tab.

## 1. One-time GitHub setup

```bash
# from inside your project folder (after Antigravity has generated the app)
git init
git add .
git commit -m "Initial commit: BillEase Android app"
git branch -M main
git remote add origin https://github.com/explorer-knowledge/Android_app.git
git push -u origin main
```

If you don't have a repo yet, create an empty one on github.com first (no README/gitignore, so it stays truly empty), then run the above.

## 2. Add the workflow file
Place `android-build.yml` at exactly this path in your project:

```
.github/workflows/android-build.yml
```

Commit and push it:
```bash
git add .github/workflows/android-build.yml
git commit -m "Add GitHub Actions CI build"
git push
```

## 3. What happens next
- On every push to `main` (or PR into `main`), GitHub spins up a free Ubuntu runner, installs JDK 17, runs **ktlint → detekt → unit tests → `assembleDebug`**, and uploads a **debug APK** artifact. CI fails if any of the four steps fails.
- Go to your repo → **Actions** tab → click the latest run → scroll to **Artifacts** → download `app-debug-apk` → unzip → install on your phone (enable "install unknown apps" for whatever app you use to open it).
- Free GitHub accounts get generous free Actions minutes for public repos (and a monthly allowance for private repos) — a small Compose app build typically takes 2–5 minutes, so this won't be a problem for hackathon-scale usage.

## 4. Add a `.gitignore` (important — do this before your first commit)
Make sure Antigravity generates a standard Android `.gitignore` so you don't push huge build folders. It should include at minimum:
```
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
```

## 5. (Optional, later) Signed release APK
The debug APK is enough to install and test. If you eventually want a proper signed release build (e.g. to share more broadly or upload somewhere), the commented-out section in `android-build.yml` shows how — it needs 4 secrets added under **Settings → Secrets and variables → Actions** in your GitHub repo:
- `KEYSTORE_BASE64` — your `.jks`/`.keystore` file, base64-encoded (`base64 -i your.keystore | pbcopy` on Mac, or `certutil -encode` on Windows)
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

You don't need this for a hackathon — skip it unless you specifically want a distributable signed build.

## 6. Tell Antigravity about this
Add to your kickoff prompt (or just say it as a follow-up):
> "Also generate a standard Android `.gitignore` at the project root, and place the CI_CD_SETUP.md's android-build.yml file at `.github/workflows/android-build.yml` in the project."