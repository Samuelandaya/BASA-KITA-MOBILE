import { useFonts } from 'expo-font';
import { SplashScreen, Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { AppStateProvider, useApp } from '../state/AppState';
import { C, FONTS } from '../ui/theme';

SplashScreen.preventAutoHideAsync().catch(() => {});

export default function RootLayout() {
  return (
    <AppStateProvider>
      <StatusBar style="dark" />
      <Routes />
    </AppStateProvider>
  );
}

/**
 * Which screens are open depends on who is signed in: the sign-in screens when no one is,
 * the email code screen for a new account, the teacher view for an admin, and the learner
 * tabs for everyone else. The role comes from the backend (demo for now).
 */
function Routes() {
  const app = useApp();
  const [fontsLoaded, fontError] = useFonts(FONTS);
  const ready = app.ready && (fontsLoaded || fontError != null);

  useEffect(() => {
    if (ready) SplashScreen.hideAsync().catch(() => {});
  }, [ready]);
  if (!ready) return null;

  const role = app.user?.role;
  return (
    <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: C.cream } }}>
      <Stack.Protected guard={!app.user}>
        <Stack.Screen name="(auth)" />
      </Stack.Protected>
      <Stack.Protected guard={role === 'pending'}>
        <Stack.Screen name="verify" />
      </Stack.Protected>
      <Stack.Protected guard={role === 'admin'}>
        <Stack.Screen name="admin" />
      </Stack.Protected>
      <Stack.Protected guard={role === 'learner'}>
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="story/[id]/index" />
        <Stack.Screen name="story/[id]/read" />
        <Stack.Screen name="story/[id]/complete" />
        <Stack.Screen name="ar/[word]" options={{ animation: 'fade' }} />
      </Stack.Protected>
    </Stack>
  );
}
