import { router } from 'expo-router';
import { Image, StyleSheet, Text, View } from 'react-native';
import { Btn, Page } from '../../ui/kit';
import { PICTURES } from '../../ui/Cover';
import { C, T } from '../../ui/theme';

export default function Welcome() {
  return (
    <Page>
      <View style={st.top}>
        <Image source={PICTURES['logo.png']} style={st.logo} resizeMode="contain" accessibilityLabel="BASA-KITA" />
        <Image source={PICTURES['kid-reading.png']} style={st.art} resizeMode="contain" />
        <Text style={[T.head(30, C.navy, 'Bold'), st.center]}>Let’s read together!</Text>
        <Text style={[T.body(16, C.muted), st.center]}>
          Storybooks, 3D AR stories and flashcards in Filipino and English, made for young readers.
        </Text>
      </View>
      <Btn label="Get started" icon="sparkles" big onPress={() => router.push('/signup')} />
      <Btn label="I already have an account" kind="white" onPress={() => router.push('/login')} />
    </Page>
  );
}

const st = StyleSheet.create({
  top: { alignItems: 'center', gap: 12, paddingTop: 24, paddingBottom: 12 },
  logo: { width: 180, height: 60 },
  art: { width: 260, height: 220 },
  center: { textAlign: 'center' },
});
