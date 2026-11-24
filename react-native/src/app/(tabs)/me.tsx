import { router } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Switch, Text, View } from 'react-native';
import { STORIES } from '../../content';
import { useApp } from '../../state/AppState';
import type { Settings } from '../../state/model';
import { Bar, Btn, Card, Page, Seg } from '../../ui/kit';
import { C, T } from '../../ui/theme';

/** Me (Figure 5): the learner's profile and progress, Edit Profile, reading settings, and Log Out. */
export default function Me() {
  const app = useApp();
  const [askOut, setAskOut] = useState(false);
  const set = (patch: Partial<Settings>) => app.updateSettings(patch);
  const finished = STORIES.filter((s) => app.account.progress[s.id]?.finished).length;
  const name = `${app.user?.firstName ?? ''} ${app.user?.lastName ?? ''}`.trim();

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>My Profile</Text>
      <Card style={st.row}>
        <View style={st.avatar}><Text style={T.head(28, C.white, 'Bold')}>{(name || 'R').charAt(0).toUpperCase()}</Text></View>
        <View style={{ flex: 1 }}>
          <Text style={T.head(20, C.navy, 'Bold')}>{name || 'Reader'}</Text>
          <Text style={T.body(13, C.muted)}>{app.user?.email}</Text>
        </View>
        <Btn label="Edit profile" kind="blue" onPress={() => router.push('/edit-profile')} />
      </Card>
      <Card>
        <Text style={T.head(20)}>My progress</Text>
        <Text style={T.body(14, C.muted)}>You’re doing great! Keep exploring and learning.</Text>
        <Stat label="Storybooks completed" value={`${finished} of ${STORIES.length}`} />
        <Bar pct={(finished * 100) / Math.max(1, STORIES.length)} />
        <Stat label="Flashcards made" value={String(app.account.cards?.length ?? 0)} />
        <Stat label="Words practiced" value={String(app.account.words?.length ?? 0)} />
      </Card>
      <Card>
        <Text style={T.head(20)}>Reading settings</Text>
        <Text style={T.body(14, C.ink, 'ExtraBold')}>Text size</Text>
        <Seg options={['Normal', 'Big', 'Bigger'] as Settings['textSize'][]} value={app.settings.textSize} onChange={(textSize) => set({ textSize })} />
        <Text style={T.body(14, C.ink, 'ExtraBold')}>Reading speed</Text>
        <Seg options={['0.75', '1', '1.25']} value={String(app.settings.speed)} label={(v) => (v === '1' ? 'Normal' : v === '0.75' ? 'Slow' : 'Fast')} onChange={(v) => set({ speed: Number(v) })} />
        <Toggle label="Read each page out loud" value={app.settings.narration} onChange={(narration) => set({ narration })} />
        <Toggle label="Highlight words while reading" value={app.settings.highlight} onChange={(highlight) => set({ highlight })} />
      </Card>
      {askOut ? (
        <Card color={C.cream2}>
          <Text style={T.head(20, C.navy, 'Bold')}>Log out of BASA-KITA?</Text>
          <View style={st.row}>
            <Btn label="Stay" kind="white" onPress={() => setAskOut(false)} style={{ flex: 1 }} />
            <Btn label="Log out" onPress={app.logout} style={{ flex: 1 }} />
          </View>
        </Card>
      ) : (
        <Btn label="Log out" icon="log-out-outline" kind="white" onPress={() => setAskOut(true)} />
      )}
    </Page>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <View style={st.row}>
      <Text style={[T.body(15), { flex: 1 }]}>{label}</Text>
      <Text style={T.body(15, C.navy, 'ExtraBold')}>{value}</Text>
    </View>
  );
}

function Toggle({ label, value, onChange }: { label: string; value: boolean; onChange: (v: boolean) => void }) {
  return (
    <View style={st.row}>
      <Text style={[T.body(15), { flex: 1 }]}>{label}</Text>
      <Switch accessibilityLabel={label} value={value} onValueChange={onChange} trackColor={{ true: C.orange, false: C.track }} thumbColor={C.white} />
    </View>
  );
}

const st = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  avatar: { width: 56, height: 56, borderRadius: 28, backgroundColor: C.orange, alignItems: 'center', justifyContent: 'center' },
});
