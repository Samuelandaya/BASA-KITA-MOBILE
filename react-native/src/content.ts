// The lessons, flashcards and vocabulary, copied from the website (src/shared), with TypeScript types on top.
// The mobile app has one story, Butit Kulit (src/data/butitKulit.ts), instead of the website's sample stories.
import { BUTIT_KULIT } from './data/butitKulit';
import { LESSONS as WEB_LESSONS, storyQuestions as webQuestions } from './shared/data/stories.js';
import { DECKS as WEB_DECKS } from './shared/data/flashcards.js';
import { VOCAB as WEB_VOCAB } from './shared/data/vocabulary.js';

export type QuizOption = { label: string; color: string; correct?: boolean };
export type Quiz = { question: string; options: QuizOption[] };
export type StoryPage = {
  text: string;
  word: string;
  image?: string; // the page's drawing, see src/ui/Cover.tsx
  arTarget?: string; // Vuforia Image Target made from the printed page (unity StreamingAssets/pages/<arTarget>.jpg)
  arModels?: string[]; // 3D models (unity StreamingAssets/models/<name>.glb) shown on that page in AR
};
export type Story = {
  id: string;
  title: string;
  language: 'Filipino' | 'English' | 'Bilingual' | string;
  lang: string; // speech locale, e.g. en-US or fil-PH
  level: string;
  minutes: number;
  ages: string;
  theme: string;
  color: string;
  cover: string; // picture name, see src/ui/Cover.tsx
  summary: string;
  blurb: string;
  focus: { letter: string; sound: string; example: string; exampleFil: string; syllables: string[]; parts: string[]; partsFil: string[] };
  pages: StoryPage[];
  quiz: Quiz;
  credit?: string; // author and owner, shown with the story
  questions?: Quiz[]; // more comprehension questions after `quiz`
};
export type Lesson = { id: number; letter: string; fil: string; eng: string };
export type FlashCard = { fil: string; shape: string; eng: string; syl: string[]; color: string };
export type Deck = { letter: string; cards: FlashCard[] };

export const STORIES: Story[] = [BUTIT_KULIT];
export const LESSONS = WEB_LESSONS as Lesson[];
export const DECKS = WEB_DECKS as Deck[];
export const VOCAB = WEB_VOCAB as Record<string, { pair: string; meaning: string }>;
export const ALL_CARDS: FlashCard[] = DECKS.flatMap((d) => d.cards);

export const storyById = (id: string) => STORIES.find((s) => s.id === id);
export const cardByWord = (fil: string) => ALL_CARDS.find((c) => c.fil === fil);
/** Three comprehension questions per story. */
export const storyQuestions = (s: Story): Quiz[] => (s.questions ? [s.quiz, ...s.questions] : (webQuestions(s) as Quiz[]));
export const isFilipino = (s: Story) => s.lang.startsWith('fil');
export const shownLevel = (s: Story) => (s.level === s.language ? 'Beginner' : s.level);

/** The stories a brand-new reader starts with: just Butit Kulit for now. */
export const starterStories = (_language: string): Story[] => STORIES;
