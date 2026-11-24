import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { storyById } from '../../../content';
import { speak } from '../../../services/speech';
import { useApp } from '../../../state/AppState';
import { Btn, Page } from '../../../ui/kit';
import { C, T } from '../../../ui/theme';

/** Finished message (Figure 6), shown after the last page: Practice Words opens Read & Learn, More Stories opens Books. */
export default function Complete() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const app = useApp();
  const story = storyById(id);

  useEffect(() => {
    if (!story) return;
    app.finishStory(story.id, 3);
    speak('Great job!', { lang: 'en-US', volume: app.settings.volume / 100 });
  }, [story?.id]);

  if (!story) return <Page title="Story"><Text style={T.body(16)}>This story wasn’t found.</Text></Page>;

  return (
    <Page>
      <View style={st.center}>
        <Text style={T.fun(40)}>Great job, {app.displayName}!</Text>
        <View style={st.stars}>
          {[48, 64, 48].map((size, n) => <Ionicons key={n} name="star" size={size} color={C.yellow2} />)}
        </View>
        <Text style={[T.body(16, C.muted), { textAlign: 'center' }]}>You finished “{story.title}”.</Text>
      </View>
      <Btn label="Practice Words" icon="book" kind="blue" big onPress={() => router.replace(`/story/${story.id}/learn`)} />
      <Btn label="More Stories" icon="chevron-forward" big onPress={() => router.dismissTo('/books')} />
    </Page>
  );
}

const st = StyleSheet.create({
  center: { alignItems: 'center', gap: 12, paddingVertical: 24 },
  stars: { flexDirection: 'row', gap: 6 },
});
