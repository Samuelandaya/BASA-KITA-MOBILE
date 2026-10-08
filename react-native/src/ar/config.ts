import Constants from 'expo-constants';

type Extra = { unityAvailable?: boolean; vuforiaLicenseKey?: string };
const extra = (Constants.expoConfig?.extra ?? {}) as Extra;

/** True when the app was built with the Unity + Vuforia export in unity/builds/android. */
export const unityAvailable = Boolean(extra.unityAvailable);

/** From EXPO_PUBLIC_VUFORIA_LICENSE_KEY in react-native/.env.local (not committed). */
export const vuforiaLicenseKey = extra.vuforiaLicenseKey ?? '';
