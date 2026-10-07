import type { TextStyle } from 'react-native';

/** Colors from the BASA-KITA design system (same tokens as the website and the Kotlin app). */
export const C = {
  cream: '#FFF9ED',
  cream2: '#FFF3D6',
  orange: '#FF7A22',
  orangeDark: '#D9600F',
  orangeText: '#B4520F',
  yellow: '#FFE588',
  yellow2: '#F6C85F',
  green: '#46B97B',
  greenText: '#1D6B42',
  blue: '#3E89C9',
  blueBtn: '#2F6FA8',
  blueDark: '#1F4F7A',
  navy: '#183B5B',
  ink: '#14293A',
  muted: '#566879',
  border: '#F3EEE3',
  cardBorder: '#F2E4C4',
  red: '#C7302A',
  white: '#FFFFFF',
  track: '#D9E2EC',
  paleBlue: '#EAF3FB',
  paleGreen: '#EAF8F0',
  locked: '#ECE8DF',
} as const;

/** Bundled fonts, loaded in src/app/_layout.tsx. Fredoka for headings, Nunito for text. */
export const FONTS = {
  'Fredoka-Medium': require('../../assets/fonts/fredoka_medium.ttf'),
  'Fredoka-SemiBold': require('../../assets/fonts/fredoka_semibold.ttf'),
  'Fredoka-Bold': require('../../assets/fonts/fredoka_bold.ttf'),
  'Nunito-Regular': require('../../assets/fonts/nunito_regular.ttf'),
  'Nunito-SemiBold': require('../../assets/fonts/nunito_semibold.ttf'),
  'Nunito-Bold': require('../../assets/fonts/nunito_bold.ttf'),
  'Nunito-ExtraBold': require('../../assets/fonts/nunito_extrabold.ttf'),
  Chewy: require('../../assets/fonts/chewy_regular.ttf'),
  LuckiestGuy: require('../../assets/fonts/luckiestguy_regular.ttf'),
};

type HeadWeight = 'Medium' | 'SemiBold' | 'Bold';
type BodyWeight = 'Regular' | 'SemiBold' | 'Bold' | 'ExtraBold';

/** Text styles named like the Kotlin app's T.head / T.body / T.fun_ / T.pill. */
export const T = {
  head: (size: number, color: string = C.navy, weight: HeadWeight = 'SemiBold'): TextStyle => ({
    fontFamily: `Fredoka-${weight}`, fontSize: size, color, lineHeight: Math.round(size * 1.2),
  }),
  body: (size: number, color: string = C.ink, weight: BodyWeight = 'SemiBold'): TextStyle => ({
    fontFamily: `Nunito-${weight}`, fontSize: size, color, lineHeight: Math.round(size * 1.4),
  }),
  fun: (size: number, color: string = C.orange): TextStyle => ({ fontFamily: 'Chewy', fontSize: size, color }),
  pill: (size: number, color: string = C.ink): TextStyle => ({ fontFamily: 'LuckiestGuy', fontSize: size, color, letterSpacing: 0.5 }),
};

export const levelBg = (name: string) => (name === 'Growing' ? '#DDF3E6' : name === 'Confident' ? '#E8F1FA' : C.cream2);
export const levelFg = (name: string) => (name === 'Growing' ? C.greenText : name === 'Confident' ? C.blueDark : C.navy);
