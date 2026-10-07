import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { arMessage, isPlaceholderKey, type ArMode, type FromUnity } from '../../ar/bridge';
import { unityAvailable, vuforiaLicenseKey } from '../../ar/config';
import { VuforiaView } from '../../ar/VuforiaView';
import { cardByWord } from '../../content';
import { Seg } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/**
 * AR for one flashcard, drawn by Unity with Vuforia (see unity/README.md).
 * "On a table" uses Vuforia Ground Plane, so no printed card is needed.
 * "On the card" uses an Image Target for the printed flashcard.
 */
export default function ArScreen() {
  const { word } = useLocalSearchParams<{ word: string }>();
  const card = cardByWord(word);
  const [mode, setMode] = useState<ArMode>('ground');
  const [state, setState] = useState<FromUnity | { type: 'starting' }>({ type: 'starting' });

  const missing = !unityAvailable
    ? 'AR isn’t in this build yet. It needs the Unity + Vuforia export (see unity/README.md).'
    : isPlaceholderKey(vuforiaLicenseKey)
      ? 'AR needs a Vuforia license key. Add EXPO_PUBLIC_VUFORIA_LICENSE_KEY to .env.local and build again.'
      : null;
  const message = !card ? 'This flashcard wasn’t found.' : missing ?? arMessage(state, card.eng, card.fil, mode);

  return (
    <View style={st.screen}>
      {card && !missing ? <VuforiaView word={card.fil} mode={mode} onState={setState} /> : null}
      <SafeAreaView style={st.overlay} pointerEvents="box-none">
        <View style={st.top}>
          <Pressable accessibilityRole="button" accessibilityLabel="Back" onPress={() => router.back()} style={st.back}>
            <Ionicons name="arrow-back" size={22} color={C.ink} />
          </Pressable>
          {card && !missing ? (
            <View style={{ flex: 1 }}>
              <Seg options={['ground', 'card'] as ArMode[]} value={mode} onChange={setMode} label={(m) => (m === 'ground' ? 'On a table' : 'On the card')} />
            </View>
          ) : null}
        </View>
        <View style={{ flex: 1 }} />
        <Text accessibilityLiveRegion="polite" style={[T.body(16, C.navy, 'ExtraBold'), st.message]}>{message}</Text>
      </SafeAreaView>
    </View>
  );
}

const st = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#000' },
  overlay: { ...StyleSheet.absoluteFill, padding: 16 },
  top: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  back: { width: 44, height: 44, borderRadius: 22, backgroundColor: C.white, alignItems: 'center', justifyContent: 'center' },
  message: { backgroundColor: C.white, borderRadius: 20, padding: 16, textAlign: 'center', overflow: 'hidden' },
});
