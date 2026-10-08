import { Stack } from 'expo-router';
import { C } from '../../ui/theme';

export const unstable_settings = { initialRouteName: 'welcome' };

export default function AuthLayout() {
  return <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: C.cream } }} />;
}
