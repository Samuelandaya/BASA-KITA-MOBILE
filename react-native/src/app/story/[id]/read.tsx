import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { storyById } from '../../../content';
import { speak, stopSpeaking } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { Cover } from '../../../ui/Cover';
import { Bar, Btn, Card, Page } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';
import { cleanWord, meaningOf, splitWords, wordAt } from '../../../words';

const SIZES = { Normal: 22, Big: 26, Bigger: 32 } as const;

/** Listen: the story is read aloud page by page, with the word being read highlighted. Tap a word to hear it and see what it means. */
export default function Read() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const app = useApp();
  const story = storyById(id);
  const saved = story ? app.account.progress[story.id] : undefined;
  const [page, setPage] = useState(() => (saved && !saved.finished ? Math.max(1, saved.page) : 1));
  const [reading, setReading] = useState(false);
  const [current, setCurrent] = useState(-1);
  const [picked, setPicked] = useState<string | null>(null);

  const pageData = story?.pages[page - 1];
  const text = pageData?.text ?? '';
  const words = useMemo(() => splitWords(text), [text]);
  const { speed, volume, highlight, narration, textSize } = app.settings;

  useEffect(() => {
    if (story) app.openStory(story.id);
    return () => { stopSpeaking(); };
  }, [story?.id]);

  const readPage = () => {
    if (!story) return;
    setReading(true);
    speak(text, {
      lang: story.lang,
      rate: speed,
      volume: volume / 100,
      onWord: highlight ? (i) => setCurrent(wordAt(words, i)) : undefined,
      onDone: () => { setReading(false); setCurrent(-1); },
    });
  };

  // Narration reads each new page on its own when it is turned on in Settings.
  useEffect(() => {
    setCurrent(-1);
    setPicked(null);
    if (story) app.savePage(story.id, page);
    if (narration) readPage();
    else stopSpeaking();
  }, [page]);

  if (!story) return <Page title="Story"><Text style={T.body(16)}>This story wasn’t found.</Text></Page>;
  const last = page === story.pages.length;
  const vocab = picked ? meaningOf(picked) : undefined;

  return (
    <Page title={story.title}>
      {pageData?.image ? (
        <View style={st.drawing}>
          <Cover name={pageData.image} width={340} height={240} fit="contain" />
        </View>
      ) : (
        <View style={[st.cover, { backgroundColor: story.color }]}>
          <Cover name={story.cover} width={340} height={180} />
        </View>
      )}
      <Text style={T.body(13, C.muted, 'ExtraBold')}>Page {page} of {story.pages.length}</Text>
      <Bar pct={(page / story.pages.length) * 100} color={C.orange} />
      <Card>
        <Text style={st.words} accessibilityLabel={text}>
          {words.map((w, i) => (
            <Text
              key={i}
              onPress={() => { setPicked(w.word); speak(cleanWord(w.word), { lang: story.lang, rate: speed, volume: volume / 100 }); }}
              style={[T.body(SIZES[textSize] ?? 26, C.ink, 'Bold'), i === current && st.now, cleanWord(w.word) === cleanWord(pageData?.word ?? '') && st.focus]}
            >
              {w.word}{' '}
            </Text>
          ))}
        </Text>
      </Card>
      {picked ? (
        <Card color={C.paleGreen}>
          <Text style={T.head(22, C.greenText, 'Bold')}>{cleanWord(picked)}</Text>
          <Text style={T.body(15)}>{vocab ? `${vocab.meaning} (${vocab.pair})` : 'Tap the speaker to hear it again.'}</Text>
        </Card>
      ) : null}
      {pageData?.arTarget ? (
        <Btn
          label="See this page in AR"
          icon="cube"
          kind="white"
          onPress={() => { stopSpeaking(); router.push({ pathname: '/story/[id]/ar', params: { id: story.id, page: String(page) } }); }}
        />
      ) : null}
      <Btn label={reading ? 'Stop' : 'Read this page to me'} icon={reading ? 'stop' : 'volume-high'} kind="blue" onPress={() => (reading ? stopSpeaking() : readPage())} />
      <View style={st.nav}>
        <Btn label="Back" icon="chevron-back" kind="white" disabled={page === 1} onPress={() => setPage((p) => p - 1)} style={{ flex: 1 }} />
        <Btn
          label={last ? 'Finish' : 'Next'}
          icon={last ? 'star' : 'chevron-forward'}
          onPress={() => (last ? router.replace(`/story/${story.id}/complete`) : setPage((p) => p + 1))}
          style={{ flex: 1 }}
        />
      </View>
    </Page>
  );
}

const st = StyleSheet.create({
  cover: { borderRadius: 20, overflow: 'hidden', height: 180, alignItems: 'center', justifyContent: 'center' },
  drawing: { borderRadius: 20, overflow: 'hidden', height: 240, alignItems: 'center', justifyContent: 'center', backgroundColor: '#FFFFFF' },
  words: { lineHeight: 42 },
  now: { backgroundColor: C.yellow, borderRadius: 6 },
  focus: { color: C.orangeDark, textDecorationLine: 'underline' },
  nav: { flexDirection: 'row', gap: 12 },
});
