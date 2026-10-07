import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { shownLevel, storyById } from '../../../content';
import { useApp } from '../../../state/AppState';
import { percentRead } from '../../../state/model';
import { Cover } from '../../../ui/Cover';
import { Bar, Btn, Card, Page, Tag } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';

/** Story details: cover, what it is about, the letter sound it practices, and how to read it. */
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
      <Card color={C.paleBlue}>
        <Text style={T.body(14, C.blueDark, 'ExtraBold')}>Sound of the story</Text>
        <Text style={T.head(24, C.navy, 'Bold')}>{story.focus.letter} {story.focus.sound}</Text>
        <Text style={T.body(14, C.muted)}>like “{story.focus.example}” and “{story.focus.exampleFil}”</Text>
      </Card>
      <Btn label={pct > 0 && pct < 100 ? 'Continue reading' : 'Listen to the story'} icon="headset" big onPress={() => router.push(`/story/${story.id}/read`)} />
      <Text style={T.body(13, C.muted)}>Read and Learn, Read With Me and Read on My Own come in the next stage of the port.</Text>
    </Page>
  );
}

const st = StyleSheet.create({
  cover: { borderRadius: 24, overflow: 'hidden', height: 240, alignItems: 'center', justifyContent: 'center' },
  heart: { position: 'absolute', top: 12, right: 12, width: 46, height: 46, borderRadius: 23, backgroundColor: C.white, alignItems: 'center', justifyContent: 'center' },
  tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
});
