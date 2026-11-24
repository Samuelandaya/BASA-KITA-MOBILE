import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { STORIES } from '../../content';
import { Cover } from '../../ui/Cover';
import { Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/**
 * AR (Figure 5): the stories that have a 3D version. Selecting one opens the 3D storybook, where the
 * learner places the book, plays the story, and moves with Previous and Next.
 */
export default function ArStories() {
  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>AR Storybooks</Text>
      <Text style={T.body(14, C.muted)}>Pick a story and watch it come alive in 3D!</Text>
      {STORIES.map((s) => {
        const has3d = !!s.arBook;
        return (
          <Card
            key={s.id}
            onPress={has3d ? () => router.push({ pathname: '/story/[id]/ar', params: { id: s.id, page: '1' } }) : undefined}
            style={[st.row, !has3d && { opacity: 0.6 }]}
          >
            <View style={[st.thumb, { backgroundColor: s.color }]}>
              <Cover name={s.cover} width={84} height={64} />
            </View>
            <View style={{ flex: 1, gap: 2 }}>
              <Text style={T.head(18, C.navy, 'Bold')}>{s.title}</Text>
              <Text style={T.body(13, C.muted)}>{has3d ? `3D storybook · ${s.pages.length} pages` : 'No 3D book yet'}</Text>
            </View>
            {has3d ? <Ionicons name="cube" size={28} color={C.green} /> : null}
          </Card>
        );
      })}
      <Card color={C.paleBlue} style={st.row}>
        <Ionicons name="information-circle" size={24} color={C.blueDark} />
        <Text style={[T.body(13, C.blueDark), { flex: 1 }]}>You need a table or the floor to put the 3D book on.</Text>
      </Card>
    </Page>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  thumb: { width: 84, height: 64, borderRadius: 14, overflow: 'hidden' },
});
