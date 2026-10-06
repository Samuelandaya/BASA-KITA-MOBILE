# BASA-KITA mobile app

The Android and iPhone app for BASA-KITA, written in **Dart with Flutter**. It uses the same design as the
website (same colors, fonts, pictures, wording) and has every website feature plus the ones that need a phone:
**AR flashcards, camera scanning of books and flashcards, offline-ready content and a real daily reminder.**

This is the **front end only**. Accounts, progress and settings are kept on the phone until a backend exists,
exactly like the website's demo mode.

## Run it

Install [Flutter](https://docs.flutter.dev/get-started/install) (3.47 or newer), then:

```bash
cd mobile
flutter pub get
flutter run                 # a connected phone or an emulator
flutter test                # unit tests for the app's logic
flutter build apk           # Android
flutter build ios           # iPhone (needs a Mac with Xcode)
flutter build web           # a browser preview of the design (camera, AR and speech need a real phone)
```

Demo accounts (any password): `sam@basakita.ph` returning learner with history · any other email is a brand-new
learner · `admin@basakita.ph` teacher dashboard.

## What is in it

| Area | Screens |
|---|---|
| Sign in | Welcome, Login, Sign Up, email code, Forgot password, Reset password (`basakita://reset?token=…`) |
| Main tabs | **Home** · **Books** · **Learn** · **Cards** · **Me** (bottom bar) |
| Reading | Story details, Listen (word highlight), Read and Learn (Marungko / English phonics), Read With Me, Read on My Own, Story Complete (3 questions) |
| Library | Heart to save, Saved, Reading History, progress %, Continue Reading, My Stories |
| Learn | 20 letter sounds, Marungko Practice, English Phonics Practice |
| Cards | 18 flashcards with a **3D object you can turn** and an **AR button** |
| Scan | Camera or gallery, reads the page on the phone, finds the book, reads it aloud. Also finds a flashcard from a photo |
| Create | Write stories page by page with pictures, read them aloud, edit them |
| Me | Progress and learner level, Settings (6 voices, volume, sound effects, music, parent PIN, daily reminder), Profile, Help |
| Admin | Overview, Learners, Books, Reports (copy CSV), add learner, add storybook |

## How it is built

```
lib/
  main.dart, router.dart     app start, routes, bottom tab bar
  theme.dart                 design tokens (colors) and bundled fonts
  data/                      stories, lessons, vocabulary, flashcards (content.dart is generated)
  state/                     learner data saved on the phone (same shape as the website)
  learner.dart               learner level, skills, recommendations
  api/auth.dart              sign-in calls (demo stand-ins; the one place to connect a server)
  services/                  speech (voices), microphone, OCR, book matching, sounds, reminders
  widgets/                   shared design pieces (waves, banner, buttons, cards, read-aloud text)
  screens/                   every screen
assets/                      pictures, fonts (Fredoka, Nunito, Chewy, Luckiest Guy), 18 .glb 3D models
```

The story, vocabulary and flashcard data and the 3D models are generated from the website's files, so the two
apps stay in step:

```bash
node tools/gen-dart-data.mjs   # from the project root: rewrites mobile/lib/data/content.dart
node tools/gen-glb.mjs         # from the project root: rewrites mobile/assets/models/*.glb
```

## Phone features and what they use

| Feature | Package | Needs |
|---|---|---|
| Read aloud with word highlight, 6 voices | `flutter_tts` | The phone's text-to-speech voices |
| Read With Me, pronunciation score | `speech_to_text` | Microphone and speech recognition |
| Scan books and flashcards | `image_picker`, `google_mlkit_text_recognition` | Camera; reads text on the phone, no internet |
| 3D flashcards and AR | `model_viewer_plus` | Internet for the viewer script; ARCore (Android) or ARKit (iPhone) for AR |
| Daily reminder at 4:00 PM | `flutter_local_notifications` | Notification permission; works when the app is closed |
| Sound effects and music | `audioplayers` | Made in code, no audio files |

Permissions are already set in `android/app/src/main/AndroidManifest.xml` and `ios/Runner/Info.plist`.

## Connecting the backend later

Only `lib/api/auth.dart` (sign in, sign up, verify, reset) calls a server today. The rest of a learner's data lives in
`lib/state/app_state.dart`. The endpoints and data shapes are the same as the website's, listed in
[`../docs/BACKEND.md`](../docs/BACKEND.md).

## Checked so far, and what is not

Checked here: `flutter analyze` (no errors), 14 unit tests (content, syllables, book recognition, scores,
learner level, saved data), and a web build that was opened in a phone-sized browser to check every screen's design.

**Not checked:** running on a real phone or emulator, `flutter build apk` / `ios` (this build machine has no Android
SDK or Xcode), and the camera, microphone, speech voices, AR and notifications, which need real hardware.
Test those first on a device. The app icon is made from the website's small logo (121 px), so replace
`assets/images/logo.png` with a larger one and run `dart run flutter_launcher_icons`.

Not part of this front end: a backend, real push notifications from a server, and an offline copy of the AR viewer
(it loads from the internet).
