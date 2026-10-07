// Shared building blocks, matching the Kotlin app's ui/Common.kt.
import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import type { ComponentProps, ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View, type StyleProp, type ViewStyle } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { shownLevel, type Story } from '../content';
import { useApp } from '../state/AppState';
import { percentRead } from '../state/model';
import { Cover } from './Cover';
import { C, T } from './theme';

export type IconName = ComponentProps<typeof Ionicons>['name'];

/** A scrolling page on the cream background. `title` adds a back button header. */
export function Page({ children, title, scroll = true }: { children: ReactNode; title?: string; scroll?: boolean }) {
  const body = scroll ? (
    <ScrollView contentContainerStyle={s.page} keyboardShouldPersistTaps="handled">{children}</ScrollView>
  ) : (
    <View style={[s.page, { flex: 1 }]}>{children}</View>
  );
  return (
    <SafeAreaView style={s.screen} edges={title ? ['top', 'left', 'right'] : ['top', 'left', 'right']}>
      {title ? <Header title={title} /> : null}
      {body}
    </SafeAreaView>
  );
}

export function Header({ title }: { title: string }) {
  return (
    <View style={s.header}>
      <Pressable accessibilityRole="button" accessibilityLabel="Back" onPress={() => router.back()} style={s.round} hitSlop={8}>
        <Ionicons name="arrow-back" size={22} color={C.ink} />
      </Pressable>
      <Text style={[T.head(20), { flex: 1 }]} numberOfLines={1}>{title}</Text>
    </View>
  );
}

type BtnKind = 'orange' | 'blue' | 'white';
export function Btn({ label, onPress, icon, kind = 'orange', big, disabled, style }: {
  label: string; onPress: () => void; icon?: IconName; kind?: BtnKind; big?: boolean; disabled?: boolean; style?: StyleProp<ViewStyle>;
}) {
  const bg = kind === 'orange' ? C.orange : kind === 'blue' ? C.blueBtn : C.white;
  const fg = kind === 'blue' ? C.white : C.ink;
  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [s.btn, { backgroundColor: bg, opacity: disabled ? 0.5 : pressed ? 0.85 : 1, paddingVertical: big ? 16 : 12 }, kind === 'white' && s.btnWhite, style]}
    >
      {icon ? <Ionicons name={icon} size={big ? 22 : 18} color={fg} /> : null}
      <Text style={T.body(big ? 18 : 16, fg, 'ExtraBold')}>{label}</Text>
    </Pressable>
  );
}

export function Card({ children, color = C.white, style, onPress }: { children: ReactNode; color?: string; style?: StyleProp<ViewStyle>; onPress?: () => void }) {
  const box = [s.card, { backgroundColor: color }, style];
  return onPress ? <Pressable onPress={onPress} style={({ pressed }) => [box, pressed && { opacity: 0.9 }]}>{children}</Pressable> : <View style={box}>{children}</View>;
}

/** Progress bar, 0-100. */
export function Bar({ pct, color = C.green }: { pct: number; color?: string }) {
  return (
    <View style={s.track} accessibilityRole="progressbar" accessibilityValue={{ min: 0, max: 100, now: pct }}>
      <View style={[s.fill, { width: `${Math.max(0, Math.min(pct, 100))}%`, backgroundColor: color }]} />
    </View>
  );
}

export function Tag({ text, bg = C.cream2, fg = C.navy }: { text: string; bg?: string; fg?: string }) {
  return <Text style={[T.body(12, fg, 'ExtraBold'), s.tag, { backgroundColor: bg }]}>{text}</Text>;
}

export function SectionTitle({ title, action, onAction }: { title: string; action?: string; onAction?: () => void }) {
  return (
    <View style={s.section}>
      <Text style={[T.head(20), { flex: 1 }]}>{title}</Text>
      {action ? <Text onPress={onAction} style={T.body(14, C.blueBtn, 'ExtraBold')}>{action}</Text> : null}
    </View>
  );
}

/** Segmented choice, e.g. Filipino / English / Both. */
export function Seg<V extends string>({ options, value, onChange, label = (v) => v }: { options: V[]; value: V; onChange: (v: V) => void; label?: (v: V) => string }) {
  return (
    <View style={s.seg}>
      {options.map((o) => (
        <Pressable key={o} accessibilityRole="button" accessibilityState={{ selected: o === value }} onPress={() => onChange(o)} style={[s.segItem, o === value && { backgroundColor: C.orange }]}>
          <Text style={T.body(14, C.ink, 'ExtraBold')}>{label(o)}</Text>
        </Pressable>
      ))}
    </View>
  );
}

/** Story cover card used in rows and grids. */
export function StoryCard({ story, note, width = 160 }: { story: Story; note?: string; width?: number }) {
  const app = useApp();
  const pct = percentRead(story.pages.length, app.account.progress[story.id]);
  return (
    <Pressable accessibilityRole="button" accessibilityLabel={story.title} onPress={() => router.push(`/story/${story.id}`)} style={[s.storyCard, { width }]}>
      <View style={[s.cover, { backgroundColor: story.color }]}>
        <Cover name={story.cover} width={width} height={width * 0.75} />
      </View>
      <View style={{ padding: 10, gap: 4 }}>
        <Text style={T.head(16)} numberOfLines={2}>{story.title}</Text>
        <Text style={T.body(12, C.muted)}>{story.language} · {shownLevel(story)} · {story.minutes} min</Text>
        {note ? <Text style={T.body(12, C.orangeText, 'ExtraBold')} numberOfLines={1}>{note}</Text> : null}
        {pct > 0 ? <Bar pct={pct} /> : null}
      </View>
    </Pressable>
  );
}

export const s = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.cream },
  page: { padding: 18, paddingBottom: 40, gap: 14 },
  header: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingHorizontal: 16, paddingTop: 8 },
  round: { width: 42, height: 42, borderRadius: 21, backgroundColor: C.white, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: C.border },
  btn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, borderRadius: 999, paddingHorizontal: 20 },
  btnWhite: { borderWidth: 2, borderColor: C.cardBorder },
  card: { borderRadius: 24, padding: 16, borderWidth: 1.5, borderColor: C.cardBorder, gap: 8 },
  track: { height: 10, borderRadius: 5, backgroundColor: C.track, overflow: 'hidden' },
  fill: { height: 10, borderRadius: 5 },
  tag: { alignSelf: 'flex-start', paddingHorizontal: 12, paddingVertical: 4, borderRadius: 999, overflow: 'hidden' },
  section: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  seg: { flexDirection: 'row', backgroundColor: C.cream2, borderRadius: 999, padding: 4, gap: 4 },
  segItem: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: 999 },
  storyCard: { backgroundColor: C.white, borderRadius: 20, overflow: 'hidden', borderWidth: 1.5, borderColor: C.cardBorder },
  cover: { overflow: 'hidden' },
});
