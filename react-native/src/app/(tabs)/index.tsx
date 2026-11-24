import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { FlatList, Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { STORIES, storyById } from '../../content';
import { useApp } from '../../state/AppState';
import { continueId, percentRead, scansLeft } from '../../state/model';
import { PICTURES } from '../../ui/Cover';
import { Bar, Btn, Card, Page, SectionTitle, StoryCard, type IconName } from '../../ui/kit';
import { C, T } from '../../ui/theme';

const greeting = (h = new Date().getHours()) => (h < 12 ? 'Good morning' : h < 18 ? 'Good afternoon' : 'Good evening');
const dateLabel = () => new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

/**
 * Home (Figure 5): the learner dashboard with recommended books. Selecting a story opens its details;
 * the other sections are in the bottom bar (Books, AR, Flashcard, Me).
 */
export default function Home() {
  const app = useApp();
  const currentId = continueId(app.account.progress);
  const current = storyById(currentId ?? '') ?? STORIES[0];
  const pct = percentRead(current.pages.length, app.account.progress[current.id]);
  const started = pct > 0 && pct < 100;
  // Recommended: stories in the learner's language that aren't finished yet come first.
  const lang = app.settings.language;
  const recommended = [...STORIES].sort((a, b) => score(b) - score(a));
  function score(s: (typeof STORIES)[number]) {
    return (lang === 'Bilingual' || s.language === lang ? 2 : 0) + (app.account.progress[s.id]?.finished ? 0 : 1);
  }

  return (
    <Page>
      <View style={st.row}>
        <View style={{ flex: 1, gap: 4 }}>
          <Text style={T.head(26, C.navy, 'Bold')}>{greeting()}, {app.displayName}!</Text>
          <Text style={[T.body(12, C.orangeText, 'ExtraBold'), st.date]}>{dateLabel()}</Text>
        </View>
        <Image source={PICTURES['teacher-1.png']} style={{ width: 84, height: 84 }} />
      </View>
      <Card color={C.cream2}>
        <Text style={T.body(14, C.orangeText, 'ExtraBold')}>{started ? 'Continue reading' : 'Start reading'}</Text>
        <Text style={T.head(24, C.navy, 'Bold')}>{current.title}</Text>
        <Text style={T.body(13, C.muted)}>You’re {pct}% through this story.</Text>
        <Bar pct={pct} />
        <Btn label={started ? 'Continue' : 'Start'} icon="play" kind="blue" onPress={() => router.push(`/story/${current.id}`)} />
      </Card>
      <View style={st.row}>
        <Quick icon="albums" label="AR Flashcards" note={`${scansLeft(app.account)} scans left today`} color={C.orange} fg={C.ink} onPress={() => router.navigate('/flashcard')} />
        <Quick icon="cube" label="AR Storybooks" note="See a story in 3D" color={C.blueBtn} fg={C.white} onPress={() => router.navigate('/ar')} />
      </View>
      <SectionTitle title="Recommended for you" />
      <FlatList
        horizontal
        data={recommended}
        keyExtractor={(x) => x.id}
        contentContainerStyle={{ gap: 12 }}
        showsHorizontalScrollIndicator={false}
        renderItem={({ item }) => <StoryCard story={item} />}
      />
    </Page>
  );
}

function Quick({ icon, label, note, color, fg, onPress }: { icon: IconName; label: string; note: string; color: string; fg: string; onPress: () => void }) {
  return (
    <Pressable accessibilityRole="button" accessibilityLabel={label} onPress={onPress} style={[st.quick, { backgroundColor: color }]}>
      <Ionicons name={icon} size={28} color={fg} />
      <Text style={T.body(16, fg, 'ExtraBold')}>{label}</Text>
      <Text style={T.body(12, fg)}>{note}</Text>
    </Pressable>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  date: { alignSelf: 'flex-start', backgroundColor: C.white, borderWidth: 1.5, borderColor: C.orange, borderRadius: 20, paddingHorizontal: 14, paddingVertical: 4, marginTop: 6, overflow: 'hidden' },
  quick: { flex: 1, gap: 4, borderRadius: 22, padding: 14 },
});
