# How to use Vuforia from React Native (checked October 2026)

**Short answer:** Vuforia has no React Native support of its own, so the app uses **Unity as a Library**:
a small Unity project with Vuforia draws the AR, and React Native shows it inside a screen.

| Option | What it means | Verdict |
|---|---|---|
| Official Vuforia React Native SDK | Does not exist. Vuforia ships a native SDK (Android, iOS) and a Unity package only. | Not possible |
| Old community packages (e.g. `react-native-vuforia`) | Written years ago for old Vuforia versions; unmaintained. | Don't use |
| **Unity as a Library + `@azesmway/react-native-unity`** | Build the AR scene in Unity with Vuforia, export it as an Android library / iOS framework, show it in React Native. The package is maintained (v1.1.1, Aug 2026), supports the New Architecture and has an Expo plugin. Vuforia documents Unity as a Library. | **Chosen** |
| Our own native module on the Vuforia native SDK | Write Kotlin/Swift code that runs Vuforia and draws the 3D models ourselves (OpenGL/Metal). Most control, but a lot of graphics code for two platforms. | Too much work for now |

Why Unity: Vuforia's Unity package already handles the camera, Ground Plane and Image Targets, and placing 3D models is
drag-and-drop. The cost is a bigger app (Unity adds roughly 30–60 MB) and a second tool (Unity Editor) for the team.

Sources: Vuforia "Unity as a Library" guide (developer.vuforia.com/library/vuforia-engine/unity-extension/using-vuforia-engine-unity-library-uaal),
Vuforia Engine release notes (v11.x), github.com/azesmway/react-native-unity.
