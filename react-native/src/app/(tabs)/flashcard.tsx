import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useState } from 'react';
import { Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { objectByEng } from '../../data/objects';
import { useApp } from '../../state/AppState';
import { DAILY_SCANS, scansLeft } from '../../state/model';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/**
 * Flashcard (Figure 7): checks the daily card limit, then Scan An Object starts a new flashcard.
 * Below are all the flashcards the learner has made.
 */
export default function Flashcards() {
  const app = useApp();
  const left = scansLeft(app.account);
  const cards = app.account.cards ?? [];
  const [limitShown, setLimitShown] = useState(false);

  const scan = () => {
    if (left <= 0) return setLimitShown(true);
    router.push('/card/scan');
  };

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Flashcards</Text>
      <Card style={st.row}>
        <Text style={[T.body(15, C.navy, 'ExtraBold'), { flex: 1 }]}>{left} of {DAILY_SCANS} scans left today</Text>
        {Array.from({ length: DAILY_SCANS }, (_, i) => <View key={i} style={[st.dot, { backgroundColor: i < left ? C.orange : C.track }]} />)}
      </Card>
      {limitShown && left <= 0 ? (
        <Card color={C.cream2} style={st.row}>
          <Ionicons name="hourglass" size={28} color={C.orangeDark} />
          <View style={{ flex: 1 }}>
            <Text style={T.head(18, C.navy, 'Bold')}>That’s all for today!</Text>
            <Text style={T.body(14, C.muted)}>You used all {DAILY_SCANS} scans. Come back tomorrow to make more flashcards.</Text>
          </View>
        </Card>
      ) : null}
      <Btn label="Scan an object" icon="scan" big onPress={scan} />
      <Text style={T.head(20)}>Your flashcards</Text>
      {cards.length === 0 ? (
        <Card style={{ alignItems: 'center' }}>
          <Ionicons name="albums-outline" size={44} color={C.muted} />
          <Text style={[T.body(15, C.muted), { textAlign: 'center' }]}>No cards yet. Scan something around you to make your first card!</Text>
        </Card>
      ) : (
        <View style={st.grid}>
          {cards.map((c) => (
            <Pressable key={c.id} accessibilityRole="button" accessibilityLabel={c.lang === 'English' ? c.eng : c.fil} onPress={() => router.push({ pathname: '/card/[id]', params: { id: c.id } })} style={st.card}>
              {c.photo ? <Image source={{ uri: c.photo }} style={st.photo} /> : <Text style={st.emoji}>{objectByEng(c.eng)?.emoji ?? '⭐'}</Text>}
              <Text style={T.head(17, C.navy, 'Bold')}>{c.lang === 'English' ? c.eng : c.fil}</Text>
              <Text style={T.body(12, C.muted)}>{c.lang}</Text>
            </Pressable>
          ))}
        </View>
      )}
    </Page>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  dot: { width: 14, height: 14, borderRadius: 7 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  card: { width: '30%', backgroundColor: C.white, borderRadius: 18, borderWidth: 1.5, borderColor: C.cardBorder, padding: 8, alignItems: 'center', gap: 2 },
  photo: { width: '100%', aspectRatio: 1, borderRadius: 12 },
  emoji: { fontSize: 44, lineHeight: 64 },
});
