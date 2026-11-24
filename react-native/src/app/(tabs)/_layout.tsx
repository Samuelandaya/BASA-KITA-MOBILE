import Ionicons from '@expo/vector-icons/Ionicons';
import { Tabs } from 'expo-router/tabs';
import type { IconName } from '../../ui/kit';
import { C } from '../../ui/theme';

const tab = (title: string, icon: IconName, active: IconName) => ({
  title,
  tabBarIcon: ({ focused, color }: { focused: boolean; color: unknown }) => <Ionicons name={focused ? active : icon} size={24} color={color as string} />,
});

/** The five learner sections from the dashboard flowchart (Figure 5): Home, Books, AR, Flashcard, Me. */
export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: C.orangeDark,
        tabBarInactiveTintColor: C.muted,
        tabBarLabelStyle: { fontFamily: 'Nunito-ExtraBold', fontSize: 12 },
        tabBarStyle: { backgroundColor: C.white },
        sceneStyle: { backgroundColor: C.cream },
      }}
    >
      <Tabs.Screen name="index" options={tab('Home', 'home-outline', 'home')} />
      <Tabs.Screen name="books" options={tab('Books', 'book-outline', 'book')} />
      <Tabs.Screen name="ar" options={tab('AR', 'cube-outline', 'cube')} />
      <Tabs.Screen name="flashcard" options={tab('Flashcard', 'albums-outline', 'albums')} />
      <Tabs.Screen name="me" options={tab('Me', 'person-outline', 'person')} />
    </Tabs>
  );
}
