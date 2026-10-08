import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { FlatList, Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { STORIES, starterStories, storyById } from '../../content';
import { useApp } from '../../state/AppState';
import { continueId, isNewReader, percentRead, type Settings } from '../../state/model';
import { PICTURES } from '../../ui/Cover';
import { Bar, Btn, Card, Page, SectionTitle, Seg, StoryCard, type IconName } from '../../ui/kit';
import { C, T } from '../../ui/theme';

const LANGUAGES: Settings['language'][] = ['Filipino', 'English', 'Bilingual'];
const greeting = (h = new Date().getHours()) => (h < 12 ? 'Good morning' : h < 18 ? 'Good afternoon' : 'Good evening');
const dateLabel = () => new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });

export default function Home() {
  const app = useApp();
  const isNew = isNewReader(app.account.progress);
  return (
    <Page>
      <View style={st.row}>
        <View style={{ flex: 1, gap: 4 }}>
          <Text style={T.head(26, C.navy, 'Bold')}>{greeting()}, {app.displayName}!</Text>
          <Text style={T.body(14, C.muted)}>{isNew ? 'Your reading adventure starts today!' : 'Ready for another fun reading adventure?'}</Text>
          <Text style={[T.body(12, C.orangeText, 'ExtraBold'), st.date]}>{dateLabel()}</Text>
        </View>
        <Image source={PICTURES['teacher-1.png']} style={{ width: 84, height: 84 }} />
      </View>
      <View style={st.row}>
        <Quick icon="cube-outline" label="AR flashcards" color={C.orange} fg={C.ink} onPress={() => router.navigate('/cards')} />
        <Quick icon="school-outline" label="Letter sounds" color={C.blueBtn} fg={C.white} onPress={() => router.navigate('/learn')} />
      </View>
      {isNew ? <NewReaderHome /> : <ReturningHome />}
    </Page>
  );
}

function Quick({ icon, label, color, fg, onPress }: { icon: IconName; label: string; color: string; fg: string; onPress: () => void }) {
  return (
    <Pressable accessibilityRole="button" onPress={onPress} style={[st.quick, { backgroundColor: color }]}>
      <Ionicons name={icon} size={26} color={fg} />
      <Text style={T.body(15, fg, 'ExtraBold')}>{label}</Text>
    </Pressable>
  );
}

function NewReaderHome() {
  const app = useApp();
  const list = starterStories(app.settings.language);
  const first = list[0] ?? STORIES[0];
  const steps = [
    { done: Object.keys(app.account.progress).length > 0, icon: 'headset' as const, title: 'Listen to a story', go: () => router.push(`/story/${first.id}/read`) },
    { done: app.account.lessonsDone > 0, icon: 'school' as const, title: 'Learn your first sound: Mm', go: () => router.navigate('/learn') },
    { done: false, icon: 'star' as const, title: 'Earn your first star', go: () => router.push(`/story/${first.id}`) },
  ];
  return (
    <>
      <Card>
        <Text style={T.body(14, C.orangeText, 'ExtraBold')}>Welcome to BASA-KITA!</Text>
        <Text style={T.head(22, C.navy, 'Bold')}>Let’s read your first story together, {app.displayName}!</Text>
        <Text style={T.body(14, C.ink, 'ExtraBold')}>I want to read in:</Text>
        <Seg
          options={LANGUAGES}
          value={app.settings.language}
          label={(v) => (v === 'Bilingual' ? 'Both' : v)}
          onChange={(language) => app.updateSettings({ language, languageChosen: true })}
        />
        <Btn label="Start my first story" icon="play" big onPress={() => router.push(`/story/${first.id}/read`)} />
        <Text style={T.body(13, C.muted, 'ExtraBold')}>“{first.title}” · {first.minutes} min</Text>
      </Card>
      <SectionTitle title="Your first steps" action={`${steps.filter((x) => x.done).length} of ${steps.length} done`} />
      {steps.map((x) => (
        <Card key={x.title} onPress={x.go} style={st.stepCard}>
          <Ionicons name={x.icon} size={26} color={C.orangeDark} />
          <Text style={[T.body(16, C.navy, 'ExtraBold'), { flex: 1 }]}>{x.title}</Text>
          <Ionicons name={x.done ? 'checkmark-circle' : 'ellipse-outline'} size={28} color={x.done ? C.green : '#B7C4D1'} />
        </Card>
      ))}
      <SectionTitle title="Your storybook" />
      <FlatList horizontal data={list} keyExtractor={(x) => x.id} contentContainerStyle={{ gap: 12 }} showsHorizontalScrollIndicator={false} renderItem={({ item }) => <StoryCard story={item} />} />
    </>
  );
}

function ReturningHome() {
  const app = useApp();
  const currentId = continueId(app.account.progress);
  const current = currentId ? storyById(currentId) : undefined;
  return (
    <>
      {current ? (
        <Card color={C.cream2}>
          <Text style={T.body(14, C.orangeText, 'ExtraBold')}>Continue Reading</Text>
          <Text style={T.head(24, C.navy, 'Bold')}>{current.title}</Text>
          <Text style={T.body(13, C.muted)}>You’re {percentRead(current.pages.length, app.account.progress[current.id])}% through this story.</Text>
          <Bar pct={percentRead(current.pages.length, app.account.progress[current.id])} />
          <Btn label="Continue" icon="play" onPress={() => router.push(`/story/${current.id}/read`)} />
        </Card>
      ) : (
        <Card>
          <Text style={T.head(20)}>Read Butit Kulit again!</Text>
          <Btn label="Open the storybook" onPress={() => router.navigate('/books')} />
        </Card>
      )}
      <SectionTitle title="Your storybook" />
      <FlatList horizontal data={STORIES} keyExtractor={(x) => x.id} contentContainerStyle={{ gap: 12 }} showsHorizontalScrollIndicator={false} renderItem={({ item }) => <StoryCard story={item} />} />
    </>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  date: { alignSelf: 'flex-start', backgroundColor: C.white, borderWidth: 1.5, borderColor: C.orange, borderRadius: 20, paddingHorizontal: 14, paddingVertical: 4, marginTop: 6, overflow: 'hidden' },
  quick: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 10, borderRadius: 22, padding: 14 },
  stepCard: { flexDirection: 'row', alignItems: 'center', gap: 12 },
});
