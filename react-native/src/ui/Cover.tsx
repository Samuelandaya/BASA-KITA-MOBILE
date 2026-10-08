import { Image, type ImageStyle, type StyleProp } from 'react-native';
import { SvgXml } from 'react-native-svg';
import { SVG_COVERS } from '../shared/svgCovers';

// PNG pictures copied from the website by tools/sync-web.mjs. Bundlers need each require() written out.
export const PICTURES: Record<string, number> = {
  'story-squirrel-large.png': require('../../assets/images/story-squirrel-large.png'),
  'story-tree.png': require('../../assets/images/story-tree.png'),
  'teacher-1.png': require('../../assets/images/teacher-1.png'),
  'teacher-2.png': require('../../assets/images/teacher-2.png'),
  'kid-reading.png': require('../../assets/images/kid-reading.png'),
  'login-illustration.png': require('../../assets/images/login-illustration.png'),
  'logo.png': require('../../assets/images/logo.png'),
  'wordmark.png': require('../../assets/images/wordmark.png'),
  'crayons.png': require('../../assets/images/crayons.png'),
  // Butit Kulit (src/data/butitKulit.ts), drawings by Joan D. Mayo
  'butit/cover.jpg': require('../../assets/images/butit/cover.jpg'),
  'butit/page-3.jpg': require('../../assets/images/butit/page-3.jpg'),
  'butit/page-4.jpg': require('../../assets/images/butit/page-4.jpg'),
  'butit/page-5.jpg': require('../../assets/images/butit/page-5.jpg'),
  'butit/page-6.jpg': require('../../assets/images/butit/page-6.jpg'),
  'butit/page-7.jpg': require('../../assets/images/butit/page-7.jpg'),
  'butit/page-8.jpg': require('../../assets/images/butit/page-8.jpg'),
  'butit/page-9.jpg': require('../../assets/images/butit/page-9.jpg'),
  'butit/page-10.jpg': require('../../assets/images/butit/page-10.jpg'),
};

/** A story cover or picture by its file name: SVG covers are drawn, PNGs are shown as images. */
export function Cover({ name, width, height, style, fit = 'cover' }: { name: string; width: number; height: number; style?: StyleProp<ImageStyle>; fit?: 'cover' | 'contain' }) {
  const svg = SVG_COVERS[name];
  if (svg) return <SvgXml xml={svg} width={width} height={height} preserveAspectRatio="xMidYMid slice" />;
  const png = PICTURES[name];
  if (png == null) return null;
  return <Image source={png} style={[{ width, height }, style]} resizeMode={fit} accessibilityIgnoresInvertColors />;
}
