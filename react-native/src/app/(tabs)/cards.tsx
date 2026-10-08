import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { DECKS } from '../../content';
import { speak } from '../../services/speech';
import { useApp } from '../../state/AppState';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/** Flashcards: one deck per Marungko letter. Each card can be heard, and opened in AR. */
export default function Cards() {
  const app = useApp();
  const [deck, setDeck] = useState(0);
  const [index, setIndex] = useState(0);
  const cards = DECKS[deck].cards;
  const card = cards[index];
  const say = (t: string) => speak(t, { lang: 'fil-PH', rate: 0.85, volume: app.settings.volume / 100 });

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>Flashcards</Text>
      <View style={st.decks}>
        {DECKS.map((d, i) => (
          <Pressable key={d.letter} accessibilityRole="button" accessibilityState={{ selected: i === deck }} onPress={() => { setDeck(i); setIndex(0); }} style={[st.deck, i === deck && { backgroundColor: C.orange }]}>
            <Text style={T.head(20, C.ink, 'Bold')}>{d.letter}</Text>
          </Pressable>
        ))}
      </View>
      <Card style={st.card}>
        <View style={[st.swatch, { backgroundColor: card.color }]}>
          <Ionicons name="cube" size={72} color="rgba(20,41,58,0.55)" />
        </View>
        <Text style={T.head(40, C.navy, 'Bold')}>{card.fil}</Text>
        <Text style={T.body(16, C.muted)}>{card.eng}</Text>
        <View style={st.syl}>
          {card.syl.map((x, i) => (
            <Pressable key={i} accessibilityRole="button" onPress={() => say(x)} style={st.sylBtn}>
              <Text style={T.head(22, C.blueDark, 'Bold')}>{x}</Text>
            </Pressable>
          ))}
        </View>
        <Btn label="Say it" icon="volume-high" kind="blue" onPress={() => say(card.fil)} />
        <Btn label="See it in AR" icon="cube-outline" big onPress={() => router.push(`/ar/${card.fil}`)} />
      </Card>
      <View style={st.nav}>
        <Btn label="Back" icon="chevron-back" kind="white" disabled={index === 0} onPress={() => setIndex(index - 1)} style={{ flex: 1 }} />
        <Btn label="Next" icon="chevron-forward" kind="white" disabled={index === cards.length - 1} onPress={() => setIndex(index + 1)} style={{ flex: 1 }} />
      </View>
      <Text style={T.body(13, C.muted)}>The turnable 3D model and Scan a Flashcard come in the next stage of the port.</Text>
    </Page>
  );
}

const st = StyleSheet.create({
  decks: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  deck: { width: 54, height: 54, borderRadius: 18, backgroundColor: C.cream2, alignItems: 'center', justifyContent: 'center' },
  card: { alignItems: 'stretch' },
  swatch: { height: 160, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  syl: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  sylBtn: { backgroundColor: C.paleBlue, borderRadius: 14, paddingHorizontal: 16, paddingVertical: 8 },
  nav: { flexDirection: 'row', gap: 12 },
});
