import { existsSync } from 'node:fs';
import { join } from 'node:path';
import type { ConfigContext, ExpoConfig } from 'expo/config';

// The AR scenes are a Unity project with Vuforia, exported as an Android library into
// unity/builds/android (see unity/README.md). Until that export exists, the app builds
// without Unity and the AR screen explains what is missing.
const unityAvailable = existsSync(join(__dirname, 'unity/builds/android/unityLibrary'));

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: 'BASA-KITA',
  slug: 'basa-kita',
  // Own link name so it doesn't clash with the Kotlin and Flutter apps (they use basakita://).
  scheme: 'basakita-rn',
  version: '0.1.0',
  orientation: 'portrait',
  icon: './assets/icon.png',
  userInterfaceStyle: 'light',
  ios: { supportsTablet: true, bundleIdentifier: 'ph.basakita.rn' },
  android: {
    package: 'ph.basakita.rn',
    adaptiveIcon: { backgroundColor: '#FFF9ED', foregroundImage: './assets/android-icon-foreground.png' },
    permissions: ['android.permission.CAMERA'],
    predictiveBackGestureEnabled: false,
  },
  plugins: [
    'expo-router',
    'expo-font',
    'expo-image',
    ['expo-splash-screen', { image: './assets/images/logo.png', imageWidth: 200, resizeMode: 'contain', backgroundColor: '#FFF9ED' }],
    // The Unity + Vuforia library needs Android 10 (API 29) or newer: Vuforia Engine 11.4 won't build below it.
    ['expo-build-properties', { android: { minSdkVersion: 29 } }],
    ...(unityAvailable ? ['./plugins/withUnityGradleProperties', '@azesmway/react-native-unity'] : []),
  ],
  extra: {
    unityAvailable,
    // Vuforia license key from the Vuforia developer portal. Put it in react-native/.env.local as
    // EXPO_PUBLIC_VUFORIA_LICENSE_KEY=... (that file is not committed). The app sends it to Unity at start.
    vuforiaLicenseKey: process.env.EXPO_PUBLIC_VUFORIA_LICENSE_KEY ?? '',
  },
});
