# AR with Unity and Vuforia

The React Native app shows AR through a small Unity project that uses **Vuforia Engine**.
Unity is exported as an Android library ("Unity as a Library") and shown inside the app by
[`@azesmway/react-native-unity`](https://github.com/azesmway/react-native-unity).

```
React Native screen (src/app/ar/[word].tsx)
   │  Configure {licenseKey} · ShowCard {word, mode} · Stop        ← src/ar/bridge.ts
   ▼
Unity scene with Vuforia  (unity/BasaKitaAR)
   │  BasaKitaBridge.cs: starts Vuforia, loads the word's 3D model, places it
   ▼
ready · searching · tracking · lost · error  → back to the app
```

Two ways to place the 3D picture:

| Mode in the app | Vuforia feature | Needs |
|---|---|---|
| **On a table** | Ground Plane | Nothing printed. Phone must support ARCore (Android) or ARKit (iPhone). |
| **On the card** | Image Target | The printed flashcards, uploaded to a Vuforia target database. |

The 3D models are the same `.glb` files as the Kotlin app (`Assets/StreamingAssets/models`).

## What you need

- **Unity 6** (or 2022.3 LTS) from Unity Hub, with **Android Build Support** (and iOS Build Support on a Mac for iPhone).
- A free **Vuforia developer account**: https://developer.vuforia.com
- A real phone. The emulator can't run Vuforia's camera tracking.

## One-time setup

1. **License key.** In the Vuforia developer portal: *Plan & Licenses → Generate Basic License*. Copy the key.
   Put it in `react-native/.env.local` (copy `.env.example`):
   `EXPO_PUBLIC_VUFORIA_LICENSE_KEY=...`. The app sends it to Unity when AR starts, so the key is not saved in the Unity project.
2. **Open the Unity project.** In Unity Hub: *Add → Add project from disk →* `react-native/unity/BasaKitaAR`.
   (`ProjectSettings/ProjectVersion.txt` is what tells Hub it is a Unity project and which editor to use: 6000.3.25f1.)
   Unity fills in the other project files and installs glTFast (for the .glb models) from `Packages/manifest.json`.
3. **Add Vuforia.** `Packages/manifest.json` already asks for Vuforia Engine 11.4.4 as
   `file:com.ptc.vuforia.engine-11.4.4.tgz`, but that file (about 138 MB) is too big for GitHub, so it is not committed.
   Download *Download for Unity* (`add-vuforia-package-11-4-4.unitypackage`) from developer.vuforia.com → Downloads → SDK,
   then either double-click it with the project open (Unity copies the .tgz into `Packages/`), or take
   `com.ptc.vuforia.engine-11.4.4.tgz` out of it and put it in `BasaKitaAR/Packages/` yourself.
4. **Vuforia settings.** *Window → Vuforia Configuration*: tick **Delayed Initialization** (the app starts Vuforia with the key).
5. **Make the scene** (*File → New Scene*, save it as `Assets/Scenes/AR.unity`):
   - *GameObject → Vuforia Engine → AR Camera* (delete the normal Main Camera).
   - *GameObject → Vuforia Engine → Ground Plane → Plane Finder* and *→ Ground Plane Stage*.
     On the Plane Finder, set **Anchor Stage** to the Ground Plane Stage and untick **Duplicate Stage**.
   - *GameObject → Create Empty*, name it **exactly** `BasaKitaBridge`, add the `BasaKitaBridge` script,
     and drag the Plane Finder and Ground Plane Stage into its fields.
   - (Later, for "On the card") *GameObject → Create Empty* named `CardTargets`; under it add one
     *Vuforia Engine → Image Target* per flashcard, each **named after its word** (`bola`, `aso`, …), using your
     target database. Drag `CardTargets` into the bridge's *Card Targets* field.
6. **Try it in Unity** (optional): paste the key into the bridge's *Editor License Key* and press Play. A webcam works for Image Targets;
   Ground Plane needs a phone.

## Export for the app (Android)

1. *File → Build Profiles* (or *Build Settings*) → **Android** → *Switch Platform*. Add `Assets/Scenes/AR.unity` to the scene list.
2. *Player Settings*: Scripting Backend **IL2CPP**, Target Architectures **ARM64** only, Minimum API level as Vuforia asks (it shows a warning if too low).
3. Tick **Export Project** and export into `react-native/unity/builds/android`.
4. In `react-native/unity/builds/android/unityLibrary/src/main/AndroidManifest.xml`, delete the `<intent-filter>…</intent-filter>` block.
5. Rebuild the app: `cd react-native && npx expo prebuild --clean && npx expo run:android --device` (with the phone plugged in).

`app.config.ts` and `react-native.config.js` notice the export folder and switch the Unity view on. Without it, the app
builds as normal and the AR screen says AR isn't in this build yet. The export folder is large and is not committed (`.gitignore`).

## Export for iPhone

Build the Unity project for iOS, build `UnityFramework.framework` in Xcode (set the `Data` folder and
`NativeCallProxy.h` as described in the react-native-unity README), and copy it to `react-native/unity/builds/ios`.
Then `npx expo prebuild --clean && npx expo run:ios --device`. Not tried yet.

## Known limits

- Vuforia's free Basic license shows a Vuforia watermark and is for development. A paid plan is needed to publish.
- Not tested on a phone yet: written without a Unity editor, Vuforia account or device. Expect small fixes on first build.
