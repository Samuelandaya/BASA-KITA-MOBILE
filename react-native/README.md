# BASA-KITA React Native app

The BASA-KITA mobile app in **React Native** (Expo), with **Vuforia** AR through Unity.
It is the team's chosen mobile stack; the Flutter app (repo root) and the Kotlin app (`../kotlin-android`) stay as references.

This is the **front end only**, like the other apps: accounts, progress and settings are kept on the phone in demo mode.
Set `EXPO_PUBLIC_API_URL` in `.env.local` to connect a backend later (same endpoints as the website, see the website's `docs/BACKEND.md`).

## Run it

Needs Node 20+, Android Studio (for the Android SDK and an emulator) and **Java 17**. Android Studio's own Java 25 is too new
for the native build step (`configureCMakeDebug … A restricted method in java.lang.System has been called`), so point
`JAVA_HOME` at a Java 17 first, for example the one Gradle downloaded:
`export JAVA_HOME="$(/usr/libexec/java_home -v 17)"` on a Mac with Java 17 installed. Expo Go is **not** enough because the app has native code;
`expo run:android` builds a development app instead.

```bash
cd react-native
npm install
cp .env.example .env.local     # then fill in the Vuforia key when you have one
npx expo run:android           # builds and opens the app on the emulator or a plugged-in phone
npm run typecheck              # TypeScript check
npm test                       # unit tests
```

Demo accounts (any password): `sam@basakita.ph` returning learner · any other email is a new learner · `admin@basakita.ph` teacher.

## What is in it (stage 1)

| Area | Screens |
|---|---|
| Sign in | Welcome, Log in, Create account, email code |
| Tabs | **Home** · **Books** · **Learn** · **Cards** · **Me** |
| Reading | Story details (save with a heart), Listen (read aloud page by page, word highlight, tap a word for its meaning), three story questions |
| Learn | 20 letter sounds in Marungko order |
| Cards | 18 flashcards in 6 decks, syllables read aloud, **See it in AR** |
| AR | Vuforia through Unity: on a table (Ground Plane) or on the printed card (Image Target). See [unity/README.md](unity/README.md). |

Still to move over from the Kotlin app: Read and Learn, Read With Me, Read on My Own, Scan a Book, Scan a Flashcard,
the turnable 3D flashcard, Settings (voices, PIN, reminder), Profile, Help, and the teacher dashboard.

## Shared with the website

The stories, flashcards, vocabulary, learner-level rules and sign-in code are **copied from the website** so all apps match.
Don't edit `src/shared/` by hand: change the website (Samuelandaya/BASA-KITA) and run

```bash
npm run sync-web     # same as: node tools/sync-web.mjs ../../BASA-KITA
```

The only hand-written file there is `src/shared/api/client.js` (the phone's version of the website's `client.js`).

## Files

```
src/app/            screens (Expo Router: every file is a screen)
src/shared/         COPIED from the website (see above)
src/content.ts      types for the shared data, story and card lookups
src/state/          saved data (same shape and key as the website) and the AppState provider
src/ar/             the AR bridge: messages to and from Unity, the Vuforia view, config
src/ui/             colors, fonts, shared pieces, story covers
unity/BasaKitaAR/   the Unity + Vuforia project (scripts, 3D models)
tools/sync-web.mjs  copies the website's shared code
```
