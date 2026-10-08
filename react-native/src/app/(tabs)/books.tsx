import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { STORIES, storyById, type Story } from '../../content';
import { useApp } from '../../state/AppState';
import { historyIds } from '../../state/model';
import { Page, Seg, StoryCard } from '../../ui/kit';
import { C, T } from '../../ui/theme';

type Shelf = 'All' | 'Saved' | 'History';

/** Storybooks: every story, the ones with a heart, and Reading History. */
export default function Books() {
  const app = useApp();
  const [shelf, setShelf] = useState<Shelf>('All');
  const [language, setLanguage] = useState<'All' | 'Filipino' | 'English'>('All');

  const base: Story[] =
    shelf === 'Saved' ? app.account.saved.map(storyById).filter((s): s is Story => !!s)
      : shelf === 'History' ? historyIds(app.account.progress).map(storyById).filter((s): s is Story => !!s)
        : STORIES;
  const list = base.filter((s) => language === 'All' || s.language === language || s.language === 'Bilingual');

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Storybooks</Text>
      <Seg options={['All', 'Saved', 'History'] as Shelf[]} value={shelf} onChange={setShelf} />
      <Seg options={['All', 'Filipino', 'English'] as ('All' | 'Filipino' | 'English')[]} value={language} onChange={setLanguage} label={(v) => (v === 'All' ? 'Any language' : v)} />
      {list.length === 0 ? (
        <Text style={T.body(15, C.muted)}>{shelf === 'Saved' ? 'Tap the heart on a story to keep it here.' : 'Stories you open will show up here.'}</Text>
      ) : (
        <View style={st.grid}>
          {list.map((s) => <StoryCard key={s.id} story={s} width={158} />)}
        </View>
      )}
    </Page>
  );
}

const st = StyleSheet.create({ grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, justifyContent: 'space-between' } });
