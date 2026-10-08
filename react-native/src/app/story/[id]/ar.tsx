import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { cue, isPlaceholderKey, moveBook, openBook, placeBook, resetScene, showBook, showBookPage, type BookState, type FromUnity, type ToUnity } from '../../../ar/bridge';
import { unityAvailable, vuforiaLicenseKey } from '../../../ar/config';
import { cuesBetween, timeCues } from '../../../ar/cues';
import { VuforiaView } from '../../../ar/VuforiaView';
import { storyById } from '../../../content';
import { speak, stopSpeaking } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { type IconName } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';
import { splitWords, wordAt } from '../../../words';

/**
 * The story as a 3D book in the room, with no printed book needed. The reader points the phone where
 * they want the book and places it, opens it, and each page's scene rises from the open book while
 * the page is read aloud and the characters act it out.
 */
export default function StoryBookAr() {
  const { id, page: pageParam } = useLocalSearchParams<{ id: string; page: string }>();
  const story = storyById(id);
  const app = useApp();
  const [page, setPage] = useState(() => Math.max(1, Number(pageParam) || 1));
  const [book, setBook] = useState<BookState | 'loading'>('loading');
  const [error, setError] = useState<string | null>(null);
  const data = story?.pages[page - 1];
  const printed = page + 2; // the story starts on printed page 3

  const missing = !story?.arBook
    ? 'This story has no 3D book yet.'
    : !unityAvailable
      ? 'AR isn’t in this build yet. It needs the Unity + Vuforia export (see unity/README.md).'
      : isPlaceholderKey(vuforiaLicenseKey)
        ? 'AR needs a Vuforia license key. Add EXPO_PUBLIC_VUFORIA_LICENSE_KEY to .env.local and build again.'
        : null;

  // The first message Unity gets; later pages and actions are sent with `send`.
  const first = useMemo(() => (story?.arBook ? showBook(story.arBook, printed, data?.arScene) : null), []);
  const send = useRef<((m: ToUnity) => void) | null>(null);
  const isOpen = book === 'open' || book === 'scene-ready';

  const onState = (s: FromUnity) => {
    if (s.type === 'book') setBook(s.state);
    else if (s.type === 'error') setError(s.code === 'LICENSE' ? 'AR needs a Vuforia license key.' : 'AR could not start on this phone.');
  };

  // ---- reading the page aloud while the characters act it out ----
  const text = data?.text ?? '';
  const words = useMemo(() => splitWords(text), [text]);
  const timed = useMemo(() => timeCues(text, data?.arCues), [text, data?.arCues]);
  const fired = useRef(-1); // cues up to this word have been sent
  const resumeAt = useRef(0); // word to continue from after a pause
  const stopping = useRef(false);
  const [playing, setPlaying] = useState(false);
  const [current, setCurrent] = useState(-1);

  const fire = (upTo: number) => {
    for (const t of cuesBetween(timed, fired.current, upTo)) send.current?.(cue(t.cue.actor, t.cue.action, t.cue.x ?? 0, t.cue.z ?? 0));
    fired.current = Math.max(fired.current, upTo);
  };

  const play = (from: number) => {
    if (!send.current || !words.length) return;
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

  const restart = () => { stopReading(); play(0); };

  const go = (to: number) => {
    stopReading();
    resumeAt.current = 0;
    fired.current = -1;
    setCurrent(-1);
    setPage(to);
    const next = story?.pages[to - 1];
    if (isOpen) {
      setBook('open');
      send.current?.(showBookPage(to + 2, next?.arScene));
    }
  };

  useEffect(() => () => { stopping.current = true; stopSpeaking(); }, []);

  // ---- what to show ----
  const hint =
    error ?? missing ??
    (book === 'loading' ? 'Getting the camera and the book ready…'
      : book === 'placing' ? 'Point your phone where you want the book, then tap Place book.'
        : book === 'placed' || book === 'placed-surface' ? 'Here is your book! Drag to move it, pinch to make it bigger or smaller.'
          : book === 'open' ? 'Opening the page…'
            : null);

  return (
    <View style={st.screen}>
      {first && !missing ? <VuforiaView show={first} onState={onState} onSender={(f) => { send.current = f; }} /> : null}
      <SafeAreaView style={st.overlay} pointerEvents="box-none">
        <View style={st.top}>
          <Pressable accessibilityRole="button" accessibilityLabel="Back" onPress={() => router.back()} style={st.round}>
            <Ionicons name="arrow-back" size={22} color={C.ink} />
          </Pressable>
          <View style={st.title}>
            <Text style={T.body(15, C.navy, 'ExtraBold')} numberOfLines={1}>{story?.title ?? 'Story'}{isOpen ? ` · page ${printed}` : ''}</Text>
          </View>
          {book !== 'loading' && book !== 'placing' && !missing ? (
            <Pressable accessibilityRole="button" accessibilityLabel="Move the book" onPress={() => { stopReading(); send.current?.(moveBook()); }} style={st.round}>
              <Ionicons name="move" size={22} color={C.ink} />
            </Pressable>
          ) : null}
        </View>
        <View style={{ flex: 1 }} pointerEvents="none" />

        {hint ? <Text accessibilityLiveRegion="polite" style={[T.body(16, C.navy, 'ExtraBold'), st.message]}>{hint}</Text> : null}

        {book === 'placing' && !missing ? <Action icon="locate" label="Place book" onPress={() => send.current?.(placeBook())} /> : null}
        {(book === 'placed' || book === 'placed-surface') && !missing ? (
          <Action icon="book" label="Open the book" onPress={() => send.current?.(openBook(printed, data?.arScene))} />
        ) : null}

        {book === 'scene-ready' ? (
          <View style={st.reader}>
            {words.length ? (
              <Text style={st.subtitle} accessibilityLabel={text}>
                {words.map((w, i) => (
                  <Text key={i} style={[T.body(17, C.ink, 'Bold'), i === current && st.now]}>{w.word}{' '}</Text>
                ))}
              </Text>
            ) : (
              <Text style={T.body(16, C.muted, 'Bold')}>This page is a picture. Look at what Butit and the girl are doing!</Text>
            )}
            {words.length ? (
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
            ) : null}
          </View>
        ) : null}

        {story && isOpen ? (
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

function Action({ icon, label, onPress }: { icon: IconName; label: string; onPress: () => void }) {
  return (
    <Pressable accessibilityRole="button" onPress={onPress} style={st.action}>
      <Ionicons name={icon} size={22} color={C.ink} />
      <Text style={T.body(17, C.ink, 'ExtraBold')}>{label}</Text>
    </Pressable>
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
  action: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, backgroundColor: C.orange, borderRadius: 28, paddingVertical: 16 },
  reader: { backgroundColor: C.white, borderRadius: 20, padding: 14, gap: 10 },
  subtitle: { lineHeight: 26 },
  now: { backgroundColor: C.yellow, borderRadius: 6 },
  controls: { flexDirection: 'row', gap: 10, justifyContent: 'center' },
  pill: { flexDirection: 'row', alignItems: 'center', gap: 6, borderRadius: 24, paddingHorizontal: 18, paddingVertical: 10, backgroundColor: C.cream2 },
  primary: { backgroundColor: C.orange },
  nav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 20 },
  count: { minWidth: 60, textAlign: 'center' },
});
