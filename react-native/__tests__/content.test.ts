import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';
import { ALL_CARDS, DECKS, LESSONS, STORIES, starterStories, storyQuestions } from '../src/content';
import { learnerLevel } from '../src/shared/learner.js';
import { EMPTY_ACCOUNT, sampleAccount } from '../src/state/model';
import { cleanWord, splitWords, wordAt } from '../src/words';
import { SVG_COVERS } from '../src/shared/svgCovers';

// PNG names listed in src/ui/Cover.tsx, read as text so the test doesn't load images.
const PNG_NAMES = [...readFileSync(join(__dirname, '../src/ui/Cover.tsx'), 'utf8').matchAll(/'([\w/-]+\.(?:png|jpg))':/g)].map((m) => m[1]);

describe('content', () => {
  it('has Butit Kulit and the website’s lessons and flashcards', () => {
    expect(STORIES.map((s) => s.id)).toEqual(['butit-kulit']);
    expect(LESSONS).toHaveLength(20);
    expect(DECKS).toHaveLength(6);
    expect(ALL_CARDS).toHaveLength(18);
    for (const s of STORIES) expect(storyQuestions(s)).toHaveLength(3);
  });

  it('has a picture for every story cover and page', () => {
    for (const s of STORIES) {
      expect(Boolean(SVG_COVERS[s.cover]) || PNG_NAMES.includes(s.cover)).toBe(true);
      for (const p of s.pages) if (p.image) expect(PNG_NAMES).toContain(p.image);
    }
  });

  it('has an AR page picture and 3D models in the Unity project for every story page', () => {
    const assets = join(__dirname, '../unity/BasaKitaAR/Assets/StreamingAssets');
    const pages = readdirSync(join(assets, 'pages'));
    const models = readdirSync(join(assets, 'models'));
    for (const p of STORIES.flatMap((s) => s.pages)) {
      expect(pages).toContain(`${p.arTarget}.jpg`);
      for (const m of p.arModels ?? []) expect(models).toContain(`${m}.glb`);
    }
  });

  it('has a 3D model in the Unity project for every flashcard', () => {
    const models = readdirSync(join(__dirname, '../unity/BasaKitaAR/Assets/StreamingAssets/models'));
    for (const c of ALL_CARDS) expect(models).toContain(`${c.fil}.glb`);
  });

  it('starts every new reader on Butit Kulit', () => {
    for (const lang of ['Filipino', 'English', 'Bilingual']) expect(starterStories(lang).map((s) => s.id)).toEqual(['butit-kulit']);
  });

  it('uses the website’s learner levels', () => {
    expect(learnerLevel(EMPTY_ACCOUNT).name).toBe('Beginner');
    // The levels count the website's stories, which the app no longer has, so Sam stays a Beginner.
    expect(learnerLevel(sampleAccount()).name).toBe('Beginner');
  });
});

describe('word highlighting', () => {
  it('finds the word being read', () => {
    const words = splitWords('Mia saw a red ball.');
    expect(words.map((w) => w.start)).toEqual([0, 4, 8, 10, 14]);
    expect(wordAt(words, 0)).toBe(0);
    expect(wordAt(words, 15)).toBe(4);
    expect(cleanWord('Ball!')).toBe('ball');
    expect(cleanWord('“Hello,')).toBe('hello');
  });
});
