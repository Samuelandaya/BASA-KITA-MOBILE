// Copies the website's shared code and pictures into the React Native app, so the website
// and the app use the very same stories, flashcards, vocabulary and learner-level rules.
// Run from this react-native folder, with the website repo (Samuelandaya/BASA-KITA) checked out:
//   node tools/sync-web.mjs ../../BASA-KITA
import { copyFileSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { basename, join, resolve } from 'node:path';

const web = resolve(process.argv[2] ?? '../../BASA-KITA');
const out = 'src/shared';
const banner = (from) => `// COPIED from the website (${from}) by tools/sync-web.mjs. Edit it there, then run the script again.\n`;

mkdirSync(join(out, 'data'), { recursive: true });
mkdirSync(join(out, 'api'), { recursive: true });
mkdirSync('assets/images', { recursive: true });

// Plain JavaScript files the app uses unchanged. (api/auth.js talks to the backend through
// src/shared/api/client.js, which is the app's own version of the website's client.js.)
for (const f of ['data/vocabulary.js', 'data/flashcards.js', 'learner.js', 'lock.js', 'api/auth.js']) {
  writeFileSync(join(out, f), banner(`src/${f}`) + readFileSync(join(web, 'src', f), 'utf8'));
}

// stories.js imports picture files, which the bundler on the phone can't read the same way.
// Swap each import for a name the app's <Cover> looks up (src/ui/covers.ts).
const stories = readFileSync(join(web, 'src/data/stories.js'), 'utf8').replace(
  /^import (\w+) from '\.\.\/assets\/(?:figma|covers)\/([^']+)';$/gm,
  (_, name, file) => `const ${name} = '${file}';`,
);
if (/^import /m.test(stories)) throw new Error('stories.js has an import this script does not know how to copy');
writeFileSync(join(out, 'data/stories.js'), banner('src/data/stories.js') + stories);

// Story covers drawn as SVG become strings for react-native-svg.
const svgDir = join(web, 'src/assets/covers');
const svgs = readdirSync(svgDir).filter((f) => f.endsWith('.svg')).sort();
writeFileSync(
  join(out, 'svgCovers.ts'),
  banner('src/assets/covers') + 'export const SVG_COVERS: Record<string, string> = {\n' +
    svgs.map((f) => `  ${JSON.stringify(f)}: ${JSON.stringify(readFileSync(join(svgDir, f), 'utf8'))},`).join('\n') + '\n};\n',
);

// PNG pictures go into assets/images; src/ui/covers.ts lists the ones the app shows.
const pngDir = join(web, 'src/assets/figma');
for (const f of readdirSync(pngDir).filter((f) => f.endsWith('.png'))) copyFileSync(join(pngDir, f), join('assets/images', basename(f)));

console.log(`Copied shared code, ${svgs.length} SVG covers and the PNG pictures from ${web}`);
