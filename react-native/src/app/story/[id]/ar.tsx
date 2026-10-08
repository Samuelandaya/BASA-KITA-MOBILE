import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { isPlaceholderKey, pageArMessage, showPage, type FromUnity } from '../../../ar/bridge';
import { unityAvailable, vuforiaLicenseKey } from '../../../ar/config';
import { VuforiaView } from '../../../ar/VuforiaView';
import { storyById } from '../../../content';
import { C, T } from '../../../ui/theme';

/**
 * AR for one story page: point the camera at that page of the printed book (or the PDF on another
 * screen) and the page's characters stand on it in 3D. Vuforia recognises the whole printed page,
 * so this works on phones without ARCore.
 */
export default function StoryPageAr() {
  const { id, page: pageParam } = useLocalSearchParams<{ id: string; page: string }>();
  const story = storyById(id);
  const [page, setPage] = useState(() => Math.max(1, Number(pageParam) || 1));
  const [state, setState] = useState<FromUnity | { type: 'starting' }>({ type: 'starting' });
  const data = story?.pages[page - 1];
  // The page number printed in the book: the story text starts on printed page 3.
  const printed = page + 2;

  const missing = !unityAvailable
    ? 'Wala pa ang AR sa build na ito. Kailangan ang Unity + Vuforia export (tingnan ang unity/README.md).'
    : isPlaceholderKey(vuforiaLicenseKey)
      ? 'Kailangan ng Vuforia license key. Ilagay ang EXPO_PUBLIC_VUFORIA_LICENSE_KEY sa .env.local at i-build muli.'
      : null;
  const message = !data?.arTarget ? 'Walang AR ang pahinang ito.' : missing ?? pageArMessage(state, printed);
  const go = (to: number) => { setState({ type: 'starting' }); setPage(to); };

  return (
    <View style={st.screen}>
      {data?.arTarget && !missing ? (
        <VuforiaView show={showPage(data.arTarget, data.arModels ?? [])} onState={setState} />
      ) : null}
      <SafeAreaView style={st.overlay} pointerEvents="box-none">
        <View style={st.top}>
          <Pressable accessibilityRole="button" accessibilityLabel="Bumalik" onPress={() => router.back()} style={st.round}>
            <Ionicons name="arrow-back" size={22} color={C.ink} />
          </Pressable>
          <View style={st.title}>
            <Text style={T.body(15, C.navy, 'ExtraBold')} numberOfLines={1}>{story?.title ?? 'Kuwento'} · pahina {printed}</Text>
          </View>
        </View>
        <View style={{ flex: 1 }} />
        <Text accessibilityLiveRegion="polite" style={[T.body(16, C.navy, 'ExtraBold'), st.message]}>{message}</Text>
        {story ? (
          <View style={st.nav}>
            <Pressable accessibilityRole="button" accessibilityLabel="Nakaraang pahina" disabled={page === 1} onPress={() => go(page - 1)} style={[st.round, page === 1 && st.off]}>
              <Ionicons name="chevron-back" size={24} color={C.ink} />
            </Pressable>
            <Text style={[T.body(14, C.white, 'ExtraBold'), st.count]}>{page} / {story.pages.length}</Text>
            <Pressable accessibilityRole="button" accessibilityLabel="Susunod na pahina" disabled={page === story.pages.length} onPress={() => go(page + 1)} style={[st.round, page === story.pages.length && st.off]}>
              <Ionicons name="chevron-forward" size={24} color={C.ink} />
            </Pressable>
          </View>
        ) : null}
      </SafeAreaView>
    </View>
  );
}

const st = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#000' },
  overlay: { ...StyleSheet.absoluteFill, padding: 16, gap: 12 },
  top: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  round: { width: 48, height: 48, borderRadius: 24, backgroundColor: C.white, alignItems: 'center', justifyContent: 'center' },
  off: { opacity: 0.4 },
  title: { flex: 1, backgroundColor: C.white, borderRadius: 24, paddingHorizontal: 16, paddingVertical: 12 },
  message: { backgroundColor: C.white, borderRadius: 20, padding: 16, textAlign: 'center', overflow: 'hidden' },
  nav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 20 },
  count: { minWidth: 60, textAlign: 'center' },
});
