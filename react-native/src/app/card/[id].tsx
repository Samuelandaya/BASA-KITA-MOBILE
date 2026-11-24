import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { objectByEng } from '../../data/objects';
import { speak } from '../../services/speech';
import { useApp } from '../../state/AppState';
import { Btn, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';
import { syllables } from '../../words';

/**
 * A flashcard (Figure 7): Listen plays the word, Flip shows the back with the other language,
 * Back returns, and See All Flashcards shows every card the learner made.
 */
export default function FlashcardView() {
  const { id, fresh } = useLocalSearchParams<{ id: string; fresh?: string }>();
  const app = useApp();
  const cards = app.account.cards ?? [];
  const card = id === 'latest' ? cards[0] : cards.find((c) => c.id === id);
  const [flipped, setFlipped] = useState(false);

  if (!card) return <Page title="Flashcard"><Text style={T.body(16)}>This flashcard wasn’t found.</Text></Page>;
  const english = card.lang === 'English';
  const front = english ? card.eng : card.fil;
  const back = english ? card.fil : card.eng;
  const shown = flipped ? back : front;
  const shownEnglish = flipped ? !english : english;
  const listen = () => speak(shown, { lang: shownEnglish ? 'en-US' : 'fil-PH', rate: 0.75, volume: app.settings.volume / 100 });

  return (
    <Page title={fresh ? 'Your new flashcard' : 'Flashcard'}>
      <Pressable accessibilityRole="button" accessibilityLabel={`${shown}. Tap to flip the card`} onPress={() => setFlipped(!flipped)} style={[st.card, flipped && st.back]}>
        {flipped ? (
          <>
            <Text style={T.head(46, C.white, 'Bold')}>{back}</Text>
            <Text style={T.body(16, C.white, 'Bold')}>{front} in {english ? 'Filipino' : 'English'}</Text>
          </>
        ) : (
          <>
            {card.photo ? <Image source={{ uri: card.photo }} style={st.photo} /> : <Text style={st.emoji}>{objectByEng(card.eng)?.emoji ?? '⭐'}</Text>}
            <Text style={T.head(40, C.navy, 'Bold')}>{front}</Text>
          </>
        )}
        <Text style={T.body(13, flipped ? C.white : C.muted)}>Tap the card to flip it</Text>
      </Pressable>
      <View style={st.syl}>
        {(shownEnglish ? [shown] : syllables(shown)).map((p, k) => <Text key={k} style={[T.head(20, C.blueDark, 'Bold'), st.pill]}>{p}</Text>)}
      </View>
      <View style={st.row}>
        <Btn label="Listen" icon="volume-high" kind="blue" onPress={listen} style={{ flex: 1 }} />
        <Btn label="Flip card" icon="swap-horizontal" onPress={() => setFlipped(!flipped)} style={{ flex: 1 }} />
      </View>
      <View style={st.row}>
        <Btn label="Back" icon="chevron-back" kind="white" onPress={() => router.back()} style={{ flex: 1 }} />
        <Btn label="See all flashcards" kind="white" onPress={() => router.dismissTo('/flashcard')} style={{ flex: 1 }} />
      </View>
    </Page>
  );
}

const st = StyleSheet.create({
  card: { backgroundColor: C.white, borderRadius: 28, borderWidth: 3, borderColor: C.yellow2, padding: 20, alignItems: 'center', gap: 10, minHeight: 360, justifyContent: 'center' },
  back: { backgroundColor: C.blue, borderColor: C.blueBtn },
  photo: { width: 220, height: 220, borderRadius: 20 },
  emoji: { fontSize: 120, lineHeight: 150 },
  syl: { flexDirection: 'row', gap: 8, justifyContent: 'center' },
  pill: { backgroundColor: C.paleBlue, borderRadius: 14, paddingHorizontal: 14, paddingVertical: 4, overflow: 'hidden' },
  row: { flexDirection: 'row', gap: 12 },
});
