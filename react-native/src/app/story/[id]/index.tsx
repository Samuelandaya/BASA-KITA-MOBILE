import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { shownLevel, storyById } from '../../../content';
import { useApp } from '../../../state/AppState';
import { percentRead } from '../../../state/model';
import { Cover } from '../../../ui/Cover';
import { Bar, Card, Page, Tag } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';

/** Story details (Figure 6): cover, what it is about, and the two reading modes. */
export default function StoryDetails() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const app = useApp();
  const story = storyById(id);
  if (!story) return <Page title="Story"><Text style={T.body(16)}>This story wasn’t found.</Text></Page>;
  const saved = app.account.saved.includes(story.id);
  const pct = percentRead(story.pages.length, app.account.progress[story.id]);

  return (
    <Page title={story.title}>
      <View style={[st.cover, { backgroundColor: story.color }]}>
        <Cover name={story.cover} width={340} height={240} />
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={saved ? 'Remove from saved' : 'Save this story'}
          onPress={() => app.toggleSaved(story.id)}
          style={st.heart}
        >
          <Ionicons name={saved ? 'heart' : 'heart-outline'} size={26} color={C.red} />
        </Pressable>
      </View>
      <View style={st.tags}>
        <Tag text={story.language} />
        <Tag text={shownLevel(story)} />
        <Tag text={`${story.minutes} min`} />
        <Tag text={story.ages} />
      </View>
      <Text style={T.body(16)}>{story.blurb}</Text>
      {pct > 0 ? <Bar pct={pct} /> : null}
      {story.credit ? <Text style={T.body(13, C.muted, 'Bold')}>{story.credit}</Text> : null}
      <Text style={T.head(22, C.navy, 'Bold')}>Choose your Reading Mode</Text>
      <Card onPress={() => router.push(`/story/${story.id}/read`)} style={st.mode}>
        <View style={[st.modeIcon, { backgroundColor: C.orange }]}><Ionicons name="headset" size={28} color={C.ink} /></View>
        <View style={{ flex: 1 }}>
          <Text style={T.head(19, C.navy, 'Bold')}>{pct > 0 && pct < 100 ? 'Continue: ' : ''}Listen & Read Aloud</Text>
          <Text style={T.body(13, C.muted)}>Hear each page while the words light up, then read it yourself.</Text>
        </View>
        <Ionicons name="chevron-forward" size={22} color={C.muted} />
      </Card>
      <Card onPress={() => router.push(`/story/${story.id}/learn`)} style={st.mode}>
        <View style={[st.modeIcon, { backgroundColor: C.blueBtn }]}><Ionicons name="school" size={28} color={C.white} /></View>
        <View style={{ flex: 1 }}>
          <Text style={T.head(19, C.navy, 'Bold')}>Read & Learn</Text>
          <Text style={T.body(13, C.muted)}>Learn the story’s words one at a time and sound them out.</Text>
        </View>
        <Ionicons name="chevron-forward" size={22} color={C.muted} />
      </Card>
    </Page>
  );
}

const st = StyleSheet.create({
  cover: { borderRadius: 24, overflow: 'hidden', height: 240, alignItems: 'center', justifyContent: 'center' },
  heart: { position: 'absolute', top: 12, right: 12, width: 46, height: 46, borderRadius: 23, backgroundColor: C.white, alignItems: 'center', justifyContent: 'center' },
  tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  mode: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  modeIcon: { width: 52, height: 52, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
});
