import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { LESSONS, STORIES } from '../../content';
import { speak } from '../../services/speech';
import { useApp } from '../../state/AppState';
import { Bar, Btn, Card, Page, Seg } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/** Letter sounds, one at a time (Marungko order), as in the Kotlin app's Learn tab. */
export default function Learn() {
  const app = useApp();
  const [lang, setLang] = useState<'Filipino' | 'English'>('Filipino');
  const done = app.account.lessonsDone;
  const allDone = done >= LESSONS.length;
  const next = LESSONS[allDone ? LESSONS.length - 1 : done];
  const practice = STORIES.find((s) => s.focus.letter === next.letter) ?? STORIES[0];
  const say = (text: string, l = 'fil-PH') => speak(text, { lang: l, rate: 0.8, volume: app.settings.volume / 100 });

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Let’s Learn Our Sounds!</Text>
      <Text style={T.body(14, C.muted)}>Marungko Method · one letter sound at a time, then practice!</Text>
      <Card>
        <View style={st.row}>
          <Pressable accessibilityRole="button" accessibilityLabel={`Hear ${next.letter}`} onPress={() => say(next.letter.slice(-1))} style={st.bigLetter}>
            <Text style={T.head(48, C.ink, 'Bold')}>{next.letter}</Text>
          </Pressable>
          <View style={{ flex: 1, gap: 2 }}>
            <Text style={T.body(13, C.orangeText, 'ExtraBold')}>Up next · Lesson {next.id}</Text>
            <Text style={T.head(22, C.navy, 'Bold')}>The {next.letter} sound</Text>
            <Text style={T.body(13, C.muted)}>Tap the letter to hear it, then say it: {next.fil} · {next.eng}</Text>
          </View>
        </View>
        <Bar pct={(done * 100) / LESSONS.length} />
        <Text style={T.body(12, C.muted)}>{done} of {LESSONS.length} letter sounds learned</Text>
        <Btn label={allDone ? 'All sounds learned!' : 'I can say it!'} icon="checkmark" disabled={allDone} onPress={app.completeLesson} />
        <Btn label="Practice in a story" kind="white" onPress={() => router.push(`/story/${practice.id}/read`)} />
      </Card>
      <View style={st.row}>
        <Text style={[T.head(20), { flex: 1 }]}>All lessons</Text>
        <View style={{ width: 200 }}><Seg options={['Filipino', 'English'] as ('Filipino' | 'English')[]} value={lang} onChange={setLang} /></View>
      </View>
      <View style={st.grid}>
        {LESSONS.map((l, i) => {
          const locked = i > done;
          const word = lang === 'Filipino' ? l.fil : l.eng;
          return (
            <Pressable
              key={l.id}
              accessibilityRole="button"
              accessibilityLabel={locked ? `${l.letter}, locked` : `${l.letter}, ${word}`}
              disabled={locked}
              onPress={() => say(`${l.letter.slice(-1)}. ${word}`, lang === 'Filipino' ? 'fil-PH' : 'en-US')}
              style={[st.lesson, { backgroundColor: locked ? C.locked : i < done ? C.paleGreen : C.cream2 }]}
            >
              <Text style={T.head(26, locked ? C.muted : C.navy, 'Bold')}>{l.letter}</Text>
              {locked ? <Ionicons name="lock-closed" size={16} color={C.muted} /> : <Text style={T.body(13, C.muted, 'Bold')}>{word}</Text>}
            </Pressable>
          );
        })}
      </View>
    </Page>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  bigLetter: { width: 96, height: 96, borderRadius: 26, backgroundColor: C.orange, alignItems: 'center', justifyContent: 'center' },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  lesson: { width: '31%', aspectRatio: 1, borderRadius: 20, alignItems: 'center', justifyContent: 'center', gap: 4 },
});
