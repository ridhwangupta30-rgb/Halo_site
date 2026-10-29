# Halo Focus (Android)

Halo, opened inside a small Android app, plus a blocker for the apps you choose.
While your Halo focus timer is running (on this phone **or on any other device you are signed in on**),
the chosen apps are sent back to the home screen with a "Focus first" screen.

Each device keeps its own list of apps (app names differ between a phone and a tablet).

## Get the APK

### Easiest: let GitHub build it (no software to install)
1. Make a free account at github.com and create a new repository (Private is fine).
2. Click "uploading an existing file" and drag in everything inside this folder **except** `build-workflow.txt`
   (the hidden `.github` folder may not show up when you drag; it is fine if it is missing). Commit.
3. Still in the repository, choose **Add file, Create new file**. In the name box type exactly
   `.github/workflows/build.yml` (typing the slashes makes the folders). Open `build-workflow.txt`, copy
   everything in it, paste it into the big box, and commit.
4. Open the **Actions** tab, choose **Build APK**, and wait about 4 minutes for the green tick.
5. Open that run, download **halo-focus-apk** (a zip), unzip it to get `app-debug.apk`.
6. Send the APK to your phone (Drive, WhatsApp to yourself, USB) and open it. Android will ask you to allow
   installs from that app once ("Install unknown apps").

### Or: Android Studio on a computer
Open this folder in Android Studio, wait for Gradle to sync, then Build, Build APK(s).

## First run (on each phone or tablet)
1. Open **Halo Focus**. Halo loads inside it; sign in to your cloud account as usual.
2. Tap **App blocking**.
3. Step 1: turn on the Halo Focus permission (Accessibility). Android warns about this permission; the app
   only looks at which app is in front and never reads what is inside any app.
4. Step 2: tick the apps to block.
5. In Halo, Settings, **Focus guard**, switch on "Guard my focus sessions".

Needs the Halo site version that has Focus guard (the app loads your live site).

## Limits to know about
* The permission is the way Android lets an app close other apps. Some phone makers (Xiaomi, Oppo, Vivo, Samsung
  "battery optimisation") can switch it off in the background; set Halo Focus to "No restrictions" in battery
  settings and, if needed, allow auto-start.
* The other-device signal needs a valid Halo sign-in. The app uses the sign-in Halo last passed to it, which lasts
  about an hour after you last had Halo open on this device. For a long session started on another device,
  open Halo in this app now and then.
* This version blocks apps only. Websites inside Chrome are not blocked (block the Chrome app, or use the desktop extension).
* "Pause blocking for 15 minutes" is in the App blocking screen, in case you ever need it.
* Written without being able to compile or run it on a phone, so the first build may need a small fix.
  If the build fails, send the error text.
