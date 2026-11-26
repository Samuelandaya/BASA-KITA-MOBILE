import Ionicons from '@expo/vector-icons/Ionicons';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { STORIES, storyById, type Story } from '../../content';
import { useApp } from '../../state/AppState';
import { historyIds } from '../../state/model';
import { filterStories, isFiltering, LANGUAGE_FILTERS, type LanguageFilter } from '../../storySearch';
import { Btn, Page, Seg, StoryCard } from '../../ui/kit';
import { C, T } from '../../ui/theme';

type Shelf = 'All' | 'Saved' | 'History';

/**
 * Storybooks: every story, the ones with a heart, and Reading History, with a title search and a language filter.
 * Paper: Chapter 2 Figure 20 (Storybooks Page), Use Case 6 (Browse Storybooks), Use Case 7 (Search and Filter
 * Stories) and Figure 6 (no matching story shows a message and returns to the book list).
 */
export default function Books() {
  const app = useApp();
  const [shelf, setShelf] = useState<Shelf>('All');
  const [language, setLanguage] = useState<LanguageFilter>('All');
  const [query, setQuery] = useState('');

  const base: Story[] =
    shelf === 'Saved' ? app.account.saved.map(storyById).filter((s): s is Story => !!s)
      : shelf === 'History' ? historyIds(app.account.progress).map(storyById).filter((s): s is Story => !!s)
        : STORIES;
  const list = filterStories(base, { query, language });
  const noMatch = list.length === 0 && base.length > 0 && isFiltering(query, language);
  const showAll = () => { setQuery(''); setLanguage('All'); };

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Explore Storybooks</Text>
      <View style={st.search}>
        <Ionicons name="search" size={20} color={C.muted} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Search stories"
          placeholderTextColor="#9AA8B5"
          accessibilityLabel="Search stories by title"
          returnKeyType="search"
          autoCorrect={false}
          autoCapitalize="none"
          style={[T.body(16), st.input]}
        />
        {query ? (
          <Pressable accessibilityRole="button" accessibilityLabel="Clear search" hitSlop={10} onPress={() => setQuery('')}>
            <Ionicons name="close-circle" size={22} color={C.muted} />
          </Pressable>
        ) : null}
      </View>
      <Seg options={LANGUAGE_FILTERS} value={language} onChange={setLanguage} />
      <Seg options={['All', 'Saved', 'History'] as Shelf[]} value={shelf} onChange={setShelf} label={(v) => (v === 'All' ? 'All books' : v)} />
      {noMatch ? (
        <View accessibilityRole="alert" style={st.empty}>
          <Text style={T.head(18, C.navy)}>No stories found</Text>
          <Text style={T.body(15, C.muted)}>
            {query.trim() ? `No ${language === 'All' ? '' : `${language} `}story is called “${query.trim()}”.` : `There are no ${language} stories here yet.`}
          </Text>
          <Btn label="Show all stories" icon="book" onPress={showAll} />
        </View>
      ) : list.length === 0 ? (
        <Text style={T.body(15, C.muted)}>
          {shelf === 'Saved' ? 'Tap the heart on a story to keep it here.' : shelf === 'History' ? 'Stories you open will show up here.' : 'No stories yet.'}
        </Text>
      ) : (
        <View style={st.grid}>
          {list.map((s) => <StoryCard key={s.id} story={s} width={158} />)}
        </View>
      )}
    </Page>
  );
}

const st = StyleSheet.create({
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, justifyContent: 'space-between' },
  search: { flexDirection: 'row', alignItems: 'center', gap: 8, backgroundColor: C.white, borderRadius: 999, borderWidth: 1.5, borderColor: C.cardBorder, paddingHorizontal: 16 },
  input: { flex: 1, paddingVertical: 12 },
  empty: { gap: 10, alignItems: 'flex-start' },
});
