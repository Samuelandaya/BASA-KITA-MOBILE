import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { storyById } from '../../../content';
import { speak, stopSpeaking } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { Cover } from '../../../ui/Cover';
import { Bar, Btn, Card, Page } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';
import { cleanWord, meaningOf, syllables } from '../../../words';

/**
 * Read & Learn (Figure 6): the story's vocabulary words one at a time (Marungko: sound, syllable, word).
 * Listen plays the word, Next shows the next word, and Back returns to the story details.
 */
export default function ReadAndLearn() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const app = useApp();
  const story = storyById(id);
  const words = useMemo(() => {
    const seen = new Set<string>();
    return (story?.pages ?? []).flatMap((p) => {
      const w = cleanWord(p.word);
      if (!w || seen.has(w)) return [];
      seen.add(w);
      const sentence = p.text.split(/(?<=[.!?”"])\s+/).find((x) => cleanWord(x).length && x.toLowerCase().includes(w)) ?? '';
      return [{ word: w, image: p.image, sentence }];
    });
  }, [story?.id]);
  const [i, setI] = useState(0);
  useEffect(() => () => { stopSpeaking(); }, []);

  if (!story || words.length === 0) return <Page title="Read & Learn"><Text style={T.body(16)}>This story has no words to learn yet.</Text></Page>;
  const item = words[i];
  const parts = syllables(item.word);
  const meaning = meaningOf(item.word);
  const say = (text: string, rate = 0.8) => speak(text, { lang: story.lang, rate, volume: app.settings.volume / 100 });
  const listen = () => {
    app.practiceWord(item.word);
    say(`${parts.join(' ')}. ${item.word}`, 0.7);
  };
  const last = i === words.length - 1;

  return (
    <Page title={`${story.title} · Read & Learn`}>
      <Text style={T.body(13, C.muted, 'ExtraBold')}>Word {i + 1} of {words.length}</Text>
      <Bar pct={((i + 1) / words.length) * 100} color={C.blueBtn} />
      <Card style={{ alignItems: 'center' }}>
        {item.image ? <View style={st.pic}><Cover name={item.image} width={300} height={190} fit="contain" /></View> : null}
        <Text style={T.head(52, C.navy, 'Bold')}>{item.word}</Text>
        <View style={st.syl}>
          {parts.map((p, k) => (
            <Pressable key={k} accessibilityRole="button" accessibilityLabel={`Hear ${p}`} onPress={() => say(p, 0.7)} style={st.sylBtn}>
              <Text style={T.head(24, C.blueDark, 'Bold')}>{p}</Text>
            </Pressable>
          ))}
        </View>
        {meaning ? <Text style={T.body(15, C.muted)}>{meaning.meaning} · {meaning.pair}</Text> : null}
        {item.sentence ? <Text style={[T.body(15, C.ink), { textAlign: 'center' }]}>“{item.sentence}”</Text> : null}
      </Card>
      <Btn label="Listen" icon="volume-high" kind="blue" big onPress={listen} />
      <View style={st.nav}>
        <Btn label="Back" icon="chevron-back" kind="white" onPress={() => { stopSpeaking(); router.back(); }} style={{ flex: 1 }} />
        <Btn
          label={last ? 'Done' : 'Next'}
          icon={last ? 'checkmark' : 'chevron-forward'}
          onPress={() => { app.practiceWord(item.word); stopSpeaking(); if (last) router.back(); else setI(i + 1); }}
          style={{ flex: 1 }}
        />
      </View>
    </Page>
  );
}

const st = StyleSheet.create({
  pic: { borderRadius: 18, overflow: 'hidden', backgroundColor: C.white, height: 190, alignItems: 'center', justifyContent: 'center' },
  syl: { flexDirection: 'row', gap: 10, flexWrap: 'wrap', justifyContent: 'center' },
  sylBtn: { backgroundColor: C.paleBlue, borderRadius: 16, paddingHorizontal: 18, paddingVertical: 8 },
  nav: { flexDirection: 'row', gap: 12 },
});
