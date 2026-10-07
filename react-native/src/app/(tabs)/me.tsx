import { StyleSheet, Switch, Text, View } from 'react-native';
import { LESSONS } from '../../content';
import { LEVELS, learnerLevel } from '../../shared/learner.js';
import { useApp } from '../../state/AppState';
import type { Settings } from '../../state/model';
import { Bar, Btn, Card, Page, Seg, Tag } from '../../ui/kit';
import { C, T, levelBg, levelFg } from '../../ui/theme';

const pctText = (n: number | null) => (n == null ? 'Not yet' : `${n}%`);

/** Me: learner level and skills, a few reading settings, and log out. */
export default function Me() {
  const app = useApp();
  const level = learnerLevel(app.account);
  const sk = level.skills;
  const set = (patch: Partial<Settings>) => app.updateSettings(patch);

  return (
    <Page>
      <Text style={T.head(28, C.navy, 'Bold')}>{app.user?.firstName} {app.user?.lastName}</Text>
      <Card>
        <View style={st.row}>
          <Text style={[T.head(20), { flex: 1 }]}>My reading level</Text>
          <Tag text={level.name} bg={levelBg(level.name)} fg={levelFg(level.name)} />
        </View>
        <Text style={T.body(14, C.muted)}>{LEVELS[level.index].goal}</Text>
        <Bar pct={level.toNext} color={C.orange} />
        <Text style={T.body(12, C.muted)}>{level.next ? `${level.toNext}% of the way to ${level.next}` : 'Top level reached!'}</Text>
      </Card>
      <Card>
        <Text style={T.head(20)}>My skills</Text>
        <Skill label="Stories finished" value={String(sk.finished)} />
        <Skill label="Letter sounds" value={`${app.account.lessonsDone} of ${LESSONS.length}`} />
        <Skill label="Reading aloud" value={pctText(sk.reading)} />
        <Skill label="Understanding" value={pctText(sk.understanding)} />
      </Card>
      <Card>
        <Text style={T.head(20)}>Reading settings</Text>
        <Text style={T.body(14, C.ink, 'ExtraBold')}>Language</Text>
        <Seg options={['Filipino', 'English', 'Bilingual'] as Settings['language'][]} value={app.settings.language} label={(v) => (v === 'Bilingual' ? 'Both' : v)} onChange={(language) => set({ language, languageChosen: true })} />
        <Text style={T.body(14, C.ink, 'ExtraBold')}>Text size</Text>
        <Seg options={['Normal', 'Big', 'Bigger'] as Settings['textSize'][]} value={app.settings.textSize} onChange={(textSize) => set({ textSize })} />
        <Text style={T.body(14, C.ink, 'ExtraBold')}>Reading speed</Text>
        <Seg options={['0.75', '1', '1.25']} value={String(app.settings.speed)} label={(v) => (v === '1' ? 'Normal' : v === '0.75' ? 'Slow' : 'Fast')} onChange={(v) => set({ speed: Number(v) })} />
        <Toggle label="Read each page out loud" value={app.settings.narration} onChange={(narration) => set({ narration })} />
        <Toggle label="Highlight words while reading" value={app.settings.highlight} onChange={(highlight) => set({ highlight })} />
      </Card>
      <Btn label="Log out" icon="log-out-outline" kind="white" onPress={app.logout} />
    </Page>
  );
}

function Skill({ label, value }: { label: string; value: string }) {
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

const st = StyleSheet.create({ row: { flexDirection: 'row', alignItems: 'center', gap: 10 } });
