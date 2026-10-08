import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { cue, isPlaceholderKey, pageArMessage, resetScene, showPage, type FromUnity, type ToUnity } from '../../../ar/bridge';
import { unityAvailable, vuforiaLicenseKey } from '../../../ar/config';
import { cuesBetween, timeCues } from '../../../ar/cues';
import { VuforiaView } from '../../../ar/VuforiaView';
import { storyById } from '../../../content';
import { speak, stopSpeaking } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { C, T } from '../../../ui/theme';
import { splitWords, wordAt } from '../../../words';

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
    ? 'AR isn’t in this build yet. It needs the Unity + Vuforia export (see unity/README.md).'
    : isPlaceholderKey(vuforiaLicenseKey)
      ? 'AR needs a Vuforia license key. Add EXPO_PUBLIC_VUFORIA_LICENSE_KEY to .env.local and build again.'
      : null;
  const message = !data?.arTarget ? 'This page has no AR.' : missing ?? pageArMessage(state, printed);
  const go = (to: number) => { stopReading(); setState({ type: 'starting' }); setPage(to); };

  // ---- reading the page aloud while the characters act it out ----
  const app = useApp();
  const text = data?.text ?? '';
  const words = useMemo(() => splitWords(text), [text]);
  const timed = useMemo(() => timeCues(text, data?.arCues), [text, data?.arCues]);
  const send = useRef<((m: ToUnity) => void) | null>(null);
  const fired = useRef(-1); // cues up to this word have been sent
  const resumeAt = useRef(0); // word to continue from after a pause
  const stopping = useRef(false);
  const pausedByLoss = useRef(false);
  const [playing, setPlaying] = useState(false);
  const [current, setCurrent] = useState(-1);
  const [seen, setSeen] = useState(false);
  const canPlay = timed.length > 0 && words.length > 0;

  const fire = (upTo: number) => {
    for (const t of cuesBetween(timed, fired.current, upTo)) send.current?.(cue(t.cue.actor, t.cue.action, t.cue.x ?? 0, t.cue.z ?? 0));
    fired.current = Math.max(fired.current, upTo);
  };

  const play = (from: number) => {
    if (!send.current) return;
    if (from === 0) {
      send.current(resetScene());
      fired.current = -1;
    }
    fire(from);
    const offset = words[from]?.start ?? 0;
    stopping.current = false;
    setPlaying(true);
    setCurrent(from);
    speak(text.slice(offset), {
      lang: story?.lang,
      rate: app.settings.speed,
      volume: app.settings.volume / 100,
      onWord: (ci) => {
        const i = wordAt(words, offset + ci);
        if (i < 0) return;
        setCurrent(i);
        resumeAt.current = i;
        fire(i);
      },
      onDone: () => {
        setPlaying(false);
        if (stopping.current) return; // paused: keep the place
        fire(words.length - 1);
        resumeAt.current = 0;
        setCurrent(-1);
      },
    });
  };

  function stopReading() {
    stopping.current = true;
    stopSpeaking();
    setPlaying(false);
  }

  const restart = () => { stopReading(); resumeAt.current = 0; play(0); };

  // A new page starts from the top; leaving the screen stops the voice.
  useEffect(() => {
    resumeAt.current = 0; fired.current = -1; pausedByLoss.current = false;
    setCurrent(-1); setSeen(false);
  }, [page]);
  useEffect(() => () => { stopping.current = true; stopSpeaking(); }, []);

  // When the camera loses the page, pause; carry on when it finds it again.
  useEffect(() => {
    if (state.type === 'tracking') {
      setSeen(true);
      if (pausedByLoss.current) { pausedByLoss.current = false; play(resumeAt.current); }
    } else if (state.type === 'lost' && playing) {
      pausedByLoss.current = true;
      stopReading();
    }
  }, [state.type]);

  return (
    <View style={st.screen}>
      {data?.arTarget && !missing ? (
        <VuforiaView show={showPage(data.arTarget, data.arModels ?? [], 0.21, data.arScene)} onState={setState} onSender={(f) => { send.current = f; }} />
      ) : null}
      <SafeAreaView style={st.overlay} pointerEvents="box-none">
        <View style={st.top}>
          <Pressable accessibilityRole="button" accessibilityLabel="Back" onPress={() => router.back()} style={st.round}>
            <Ionicons name="arrow-back" size={22} color={C.ink} />
          </Pressable>
          <View style={st.title}>
            <Text style={T.body(15, C.navy, 'ExtraBold')} numberOfLines={1}>{story?.title ?? 'Story'} · page {printed}</Text>
          </View>
        </View>
        <View style={{ flex: 1 }} />
        {canPlay && seen && !missing ? (
          <View style={st.reader}>
            <Text style={st.subtitle} accessibilityLabel={text}>
              {words.map((w, i) => (
                <Text key={i} style={[T.body(17, C.ink, 'Bold'), i === current && st.now]}>{w.word}{' '}</Text>
              ))}
            </Text>
            {state.type === 'lost' ? <Text style={T.body(14, C.orangeDark, 'ExtraBold')}>{message}</Text> : null}
            <View style={st.controls}>
              <Pressable accessibilityRole="button" accessibilityLabel={playing ? 'Pause' : 'Play'} onPress={() => (playing ? stopReading() : play(resumeAt.current))} style={[st.pill, st.primary]}>
                <Ionicons name={playing ? 'pause' : 'play'} size={20} color={C.ink} />
                <Text style={T.body(15, C.ink, 'ExtraBold')}>{playing ? 'Pause' : resumeAt.current > 0 ? 'Continue' : 'Play'}</Text>
              </Pressable>
              <Pressable accessibilityRole="button" accessibilityLabel="Read again from the start" onPress={restart} style={st.pill}>
                <Ionicons name="refresh" size={20} color={C.ink} />
                <Text style={T.body(15, C.ink, 'ExtraBold')}>Replay</Text>
              </Pressable>
            </View>
          </View>
        ) : (
          <Text accessibilityLiveRegion="polite" style={[T.body(16, C.navy, 'ExtraBold'), st.message]}>{message}</Text>
        )}
        {story ? (
          <View style={st.nav}>
            <Pressable accessibilityRole="button" accessibilityLabel="Previous page" disabled={page === 1} onPress={() => go(page - 1)} style={[st.round, page === 1 && st.off]}>
              <Ionicons name="chevron-back" size={24} color={C.ink} />
            </Pressable>
            <Text style={[T.body(14, C.white, 'ExtraBold'), st.count]}>{page} / {story.pages.length}</Text>
            <Pressable accessibilityRole="button" accessibilityLabel="Next page" disabled={page === story.pages.length} onPress={() => go(page + 1)} style={[st.round, page === story.pages.length && st.off]}>
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
  reader: { backgroundColor: C.white, borderRadius: 20, padding: 14, gap: 10 },
  subtitle: { lineHeight: 26 },
  now: { backgroundColor: C.yellow, borderRadius: 6 },
  controls: { flexDirection: 'row', gap: 10, justifyContent: 'center' },
  pill: { flexDirection: 'row', alignItems: 'center', gap: 6, borderRadius: 24, paddingHorizontal: 18, paddingVertical: 10, backgroundColor: C.cream2 },
  primary: { backgroundColor: C.orange },
});
