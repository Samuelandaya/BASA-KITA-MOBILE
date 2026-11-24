import Ionicons from '@expo/vector-icons/Ionicons';
import * as ImagePicker from 'expo-image-picker';
import { router } from 'expo-router';
import { useState } from 'react';
import { Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { OBJECTS, type ScanObject } from '../../data/objects';
import { useApp } from '../../state/AppState';
import { scansLeft } from '../../state/model';
import { ErrorText } from '../../ui/Field';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

type Lang = 'English' | 'Filipino';

/**
 * Scan An Object (Figure 7): choose English or Filipino, take a picture of one object, and if the
 * picture is good a flashcard is made. If not, scan again.
 * The backend's picture recognition will name the object; until it is connected the learner taps it.
 */
export default function Scan() {
  const app = useApp();
  const [lang, setLang] = useState<Lang | null>(null);
  const [photo, setPhoto] = useState<string | null>(null);
  const [error, setError] = useState('');
  const left = scansLeft(app.account);

  const takePicture = async () => {
    setError('');
    const perm = await ImagePicker.requestCameraPermissionsAsync();
    if (!perm.granted) return setError('BASA-KITA needs the camera to scan. Please allow the camera and try again.');
    try {
      const shot = await ImagePicker.launchCameraAsync({ mediaTypes: ['images'], quality: 0.5, allowsEditing: true, aspect: [1, 1] });
      if (shot.canceled || !shot.assets?.[0]?.uri) return setError('No picture was taken. Point the camera at one thing and try again.');
      setPhoto(shot.assets[0].uri);
    } catch {
      setError('The camera could not open. Please try again.');
    }
  };

  const make = (o: ScanObject) => {
    if (!lang || !photo) return;
    app.addCard({ eng: o.eng, fil: o.fil, lang, photo });
    // The new card is the first in the list ("latest").
    router.replace({ pathname: '/card/[id]', params: { id: 'latest', fresh: '1' } });
  };

  if (left <= 0) {
    return (
      <Page title="Scan an object">
        <Card color={C.cream2}>
          <Text style={T.head(20, C.navy, 'Bold')}>That’s all for today!</Text>
          <Text style={T.body(15, C.muted)}>You used all your scans. Come back tomorrow to make more flashcards.</Text>
        </Card>
      </Page>
    );
  }

  if (!lang) {
    return (
      <Page title="Choose a language">
        <Text style={T.head(22, C.navy, 'Bold')}>Which language do you want to learn?</Text>
        <View style={st.langs}>
          {(['English', 'Filipino'] as Lang[]).map((l) => (
            <Pressable key={l} accessibilityRole="button" onPress={() => setLang(l)} style={st.lang}>
              <Text style={st.flag}>{l === 'English' ? '🔤' : '🇵🇭'}</Text>
              <Text style={T.head(22, C.navy, 'Bold')}>{l}</Text>
            </Pressable>
          ))}
        </View>
        <Text style={T.body(14, C.muted)}>{left} scans left today</Text>
      </Page>
    );
  }

  if (!photo) {
    return (
      <Page title={`Scan flashcard · ${lang}`}>
        <Card style={{ alignItems: 'center' }}>
          <Ionicons name="camera" size={64} color={C.orange} />
          <Text style={[T.head(20, C.navy, 'Bold'), { textAlign: 'center' }]}>Point the camera at one thing, like a cup or a ball.</Text>
          <Text style={[T.body(14, C.muted), { textAlign: 'center' }]}>Hold the phone still so the picture is clear.</Text>
        </Card>
        <ErrorText text={error} />
        <Btn label="Scan flashcard" icon="scan" big onPress={takePicture} />
        <Btn label="Change language" kind="white" onPress={() => setLang(null)} />
      </Page>
    );
  }

  return (
    <Page title="Is this your picture?">
      <Image source={{ uri: photo }} style={st.photo} accessibilityLabel="The picture you took" />
      <Text style={T.head(20, C.navy, 'Bold')}>What is in the picture?</Text>
      <View style={st.grid}>
        {OBJECTS.map((o) => (
          <Pressable key={o.eng} accessibilityRole="button" accessibilityLabel={lang === 'English' ? o.eng : o.fil} onPress={() => make(o)} style={st.obj}>
            <Text style={st.emoji}>{o.emoji}</Text>
            <Text style={T.body(13, C.navy, 'ExtraBold')}>{lang === 'English' ? o.eng : o.fil}</Text>
          </Pressable>
        ))}
      </View>
      <Btn label="Picture not clear? Scan again" icon="refresh" kind="white" onPress={() => { setPhoto(null); takePicture(); }} />
    </Page>
  );
}

const st = StyleSheet.create({
  langs: { flexDirection: 'row', gap: 12 },
  lang: { flex: 1, alignItems: 'center', gap: 8, backgroundColor: C.white, borderRadius: 24, borderWidth: 2, borderColor: C.cardBorder, paddingVertical: 28 },
  flag: { fontSize: 56, lineHeight: 72 },
  photo: { width: '100%', aspectRatio: 1, borderRadius: 24, backgroundColor: C.white },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  obj: { width: '22%', alignItems: 'center', backgroundColor: C.white, borderRadius: 16, borderWidth: 1.5, borderColor: C.cardBorder, paddingVertical: 8 },
  emoji: { fontSize: 30, lineHeight: 40 },
});
