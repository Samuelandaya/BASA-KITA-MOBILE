import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { storyById, storyQuestions } from '../../../content';
import { speak } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { Btn, Card, Page } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';

/** Story complete: finishing a story earns three stars (as in the Kotlin app), then three questions; first-try right answers are the quiz score. */
export default function Complete() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const app = useApp();
  const story = storyById(id);
  const [q, setQ] = useState(0);
  const [firstTry, setFirstTry] = useState(0);
  const [wrong, setWrong] = useState<string[]>([]);
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (story) app.finishStory(story.id, 3);
  }, [story?.id]);

  if (!story) return <Page title="Story"><Text style={T.body(16)}>This story wasn’t found.</Text></Page>;
  const questions = storyQuestions(story);
  const quiz = questions[q];

  const answer = (label: string, correct?: boolean) => {
    if (!correct) {
      setWrong((w) => [...w, label]);
      speak('Try again!', { lang: 'en-US', volume: app.settings.volume / 100 });
      return;
    }
    const right = firstTry + (wrong.length === 0 ? 1 : 0);
    setFirstTry(right);
    setWrong([]);
    if (q + 1 < questions.length) return setQ(q + 1);
    app.recordQuiz(story.id, right, questions.length);
    speak('Great job!', { lang: 'en-US', volume: app.settings.volume / 100 });
    setDone(true);
  };

  if (done) {
    return (
      <Page>
        <View style={st.center}>
          <Text style={T.fun(40)}>Great job, {app.displayName}!</Text>
          <View style={st.stars}>
            {[48, 64, 48].map((size, n) => <Ionicons key={n} name="star" size={size} color={C.yellow2} />)}
          </View>
          <Text style={T.body(16, C.muted)}>You finished “{story.title}” and got {firstTry} of {questions.length} answers right on the first try.</Text>
        </View>
        <Btn label="Back to Home" icon="home" big onPress={() => router.dismissTo('/')} />
        <Btn label="Read it again" kind="white" onPress={() => router.replace(`/story/${story.id}/read`)} />
      </Page>
    );
  }

  return (
    <Page title="Story questions">
      <Text style={T.body(13, C.muted, 'ExtraBold')}>Question {q + 1} of {questions.length}</Text>
      <Card>
        <Text style={T.head(24, C.navy, 'Bold')}>{quiz.question}</Text>
      </Card>
      {quiz.options.map((o) => {
        const isWrong = wrong.includes(o.label);
        return (
          <Pressable
            key={o.label}
            accessibilityRole="button"
            disabled={isWrong}
            onPress={() => answer(o.label, o.correct)}
            style={[st.option, { borderColor: o.color, opacity: isWrong ? 0.4 : 1 }]}
          >
            <View style={[st.dot, { backgroundColor: o.color }]} />
            <Text style={[T.body(18, C.ink, 'ExtraBold'), { flex: 1 }]}>{o.label}</Text>
            {isWrong ? <Ionicons name="close" size={22} color={C.red} /> : null}
          </Pressable>
        );
      })}
    </Page>
  );
}

const st = StyleSheet.create({
  center: { alignItems: 'center', gap: 12, paddingVertical: 24 },
  stars: { flexDirection: 'row', gap: 6 },
  option: { flexDirection: 'row', alignItems: 'center', gap: 12, backgroundColor: C.white, borderWidth: 3, borderRadius: 20, padding: 16 },
  dot: { width: 22, height: 22, borderRadius: 11 },
});
