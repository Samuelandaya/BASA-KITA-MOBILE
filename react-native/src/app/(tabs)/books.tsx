import Ionicons from '@expo/vector-icons/Ionicons';
import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';
import { STORIES } from '../../content';
import { Card, Page, Seg, StoryCard } from '../../ui/kit';
import { C, T } from '../../ui/theme';

type Lang = 'All' | 'Filipino' | 'English';

/** Books (Figure 6): every storybook, with a search box and language filters. */
export default function Books() {
  const [query, setQuery] = useState('');
  const [language, setLanguage] = useState<Lang>('All');
  const q = query.trim().toLowerCase();
  const list = STORIES.filter(
    (s) =>
      (language === 'All' || s.language === language || s.language === 'Bilingual') &&
      (!q || s.title.toLowerCase().includes(q) || s.summary.toLowerCase().includes(q)),
  );

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Explore Storybooks</Text>
      <Text style={T.body(14, C.muted)}>Your next reading adventure starts here!</Text>
      <View style={st.search}>
        <Ionicons name="search" size={20} color={C.muted} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Search stories…"
          placeholderTextColor="#9AA8B5"
          accessibilityLabel="Search stories"
          returnKeyType="search"
          style={[T.body(16), { flex: 1, paddingVertical: 10 }]}
        />
        {query ? <Ionicons name="close-circle" size={20} color={C.muted} onPress={() => setQuery('')} accessibilityLabel="Clear search" /> : null}
      </View>
      <Seg options={['All', 'Filipino', 'English'] as Lang[]} value={language} onChange={setLanguage} />
      {list.length === 0 ? (
        <Card style={{ alignItems: 'center' }}>
          <Ionicons name="book-outline" size={40} color={C.muted} />
          <Text style={T.head(20)}>No stories found</Text>
          <Text style={[T.body(14, C.muted), { textAlign: 'center' }]}>Try another word or choose a different language.</Text>
        </Card>
      ) : (
        <View style={st.grid}>
          {list.map((s) => <StoryCard key={s.id} story={s} width={158} />)}
        </View>
      )}
    </Page>
  );
}

const st = StyleSheet.create({
  search: { flexDirection: 'row', alignItems: 'center', gap: 8, backgroundColor: C.white, borderRadius: 999, borderWidth: 1.5, borderColor: C.cardBorder, paddingHorizontal: 16 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, justifyContent: 'space-between' },
});
