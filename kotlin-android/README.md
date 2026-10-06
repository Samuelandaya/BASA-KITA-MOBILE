# BASA-KITA Android app (Kotlin)

The Android app for BASA-KITA, written in **Kotlin with Jetpack Compose**. It has the same screens, design and
features as the Flutter app at the top of this repo ([README](../README.md)): same colors, fonts, pictures and wording.

This is the **front end only**. Accounts, progress and settings are kept on the phone until a backend exists,
exactly like the website's demo mode. The one place to connect a server is `api/AuthApi.kt`.

## Run it

Install [Android Studio](https://developer.android.com/studio) (it brings the Android SDK and Java), then either
open this `kotlin-android` folder in Android Studio and press Run, or use the terminal:

```bash
cd kotlin-android
./gradlew installDebug        # build and install on a connected phone or emulator
./gradlew testDebugUnitTest   # unit tests for the app's logic
./gradlew assembleRelease     # app/build/outputs/apk/release/app-release.apk
```

Demo accounts (any password): `sam@basakita.ph` returning learner with history · any other email is a brand-new
learner · `admin@basakita.ph` teacher dashboard.

AR needs a real phone that supports [Google Play Services for AR](https://developers.google.com/ar/devices).
On other phones and on the emulator the 3D flashcards still work and the AR screen says AR isn't available.

## What is in it

| Area | Screens |
|---|---|
| Sign in | Welcome, Login, Sign Up, email code, Forgot password, Reset password (`basakita://reset?token=…`) |
| Main tabs | **Home** · **Books** · **Learn** · **Cards** · **Me** (bottom bar) |
| Reading | Story details, Listen (word highlight), Read and Learn (Marungko / English phonics), Read With Me, Read on My Own, Story Complete (3 questions) |
| Library | Heart to save, Saved, Reading History, progress %, Continue Reading, My Stories |
| Learn | 20 letter sounds, Marungko Practice, English Phonics Practice |
| Cards | 18 flashcards with a **3D object you can turn**, an **AR view** and **Scan a Flashcard** |
| Scan a Book | Camera or gallery, reads the page on the phone, finds the book, reads it aloud |
| Create | Write stories page by page with pictures, read them aloud, edit them |
| Me | Progress and learner level, Settings (6 voices, volume, sound effects, music, parent PIN, daily reminder), Profile, Help |
| Admin | Overview, Learners, Books, Reports (copy CSV), add learner, add storybook |

## How it works

| Feature | Built with |
|---|---|
| Screens | Jetpack Compose (Material 3) and Navigation Compose |
| Reading words from a photo | Google ML Kit text recognition, on the phone (no internet) |
| Reading aloud, word by word | Android `TextToSpeech` |
| "Say it!" and read-aloud scores | Android `SpeechRecognizer` |
| 3D flashcards and AR | SceneView (Filament) and ARCore |
| Story covers (SVG) | Coil |
| Saved progress | `SharedPreferences` with kotlinx.serialization (same shape as the website's saved state) |
| Daily 4:00 PM reminder | `AlarmManager` and a notification, restored after the phone restarts |

## Files

```
app/src/main/java/ph/basakita/app/
  MainActivity.kt, App.kt     start-up and which screens are open (signed out, learner tabs, teacher)
  data/Content.kt             GENERATED stories, lessons, vocabulary, flashcards (tools/gen-kotlin-data.mjs)
  data/Vocab.kt               word clean-up, syllables
  state/                      saved data and the AppState that screens read
  Learner.kt                  reading level, skills, recommendations, read-aloud score
  api/AuthApi.kt              sign-in calls (demo stand-ins; the one place to connect a server)
  services/                   voice, microphone, sounds, photo text reading, book matching, reminders
  ui/                         theme, shared pieces (banner, panels, buttons, story cards), reading widgets
  screens/                    one file per area: Auth, Home, Library, ReadModes, Learn, Cards, Scan, Create, Me, Settings, Admin
app/src/main/assets/          covers, pictures and the 3D models (.glb)
app/src/test/                 unit tests
```

The stories and flashcards come from the website's data so all apps stay the same. After changing
`src/data/*.js` in the website repo ([Samuelandaya/BASA-KITA](https://github.com/Samuelandaya/BASA-KITA)),
run this from the `kotlin-android` folder, giving the path to the website repo:

```bash
node tools/gen-kotlin-data.mjs ../../BASA-KITA
```

The Flutter and Kotlin apps share the 3D models: if the website's `tools/gen-glb.mjs` makes new ones,
copy `assets/models/*.glb` to `kotlin-android/app/src/main/assets/models/`.
