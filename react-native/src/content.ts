// The stories, lessons, flashcards and vocabulary, copied from the website (src/shared).
// This file only adds TypeScript types on top of the website's data.
import { LESSONS as WEB_LESSONS, STORIES as WEB_STORIES, storyQuestions as webQuestions } from './shared/data/stories.js';
import { DECKS as WEB_DECKS } from './shared/data/flashcards.js';
import { VOCAB as WEB_VOCAB } from './shared/data/vocabulary.js';

export type QuizOption = { label: string; color: string; correct?: boolean };
export type Quiz = { question: string; options: QuizOption[] };
export type StoryPage = { text: string; word: string };
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
};
export type Lesson = { id: number; letter: string; fil: string; eng: string };
export type FlashCard = { fil: string; shape: string; eng: string; syl: string[]; color: string };
export type Deck = { letter: string; cards: FlashCard[] };

export const STORIES = WEB_STORIES as Story[];
export const LESSONS = WEB_LESSONS as Lesson[];
export const DECKS = WEB_DECKS as Deck[];
export const VOCAB = WEB_VOCAB as Record<string, { pair: string; meaning: string }>;
export const ALL_CARDS: FlashCard[] = DECKS.flatMap((d) => d.cards);

export const storyById = (id: string) => STORIES.find((s) => s.id === id);
export const cardByWord = (fil: string) => ALL_CARDS.find((c) => c.fil === fil);
/** Three comprehension questions per story. */
export const storyQuestions = (s: Story) => webQuestions(s) as Quiz[];
export const isFilipino = (s: Story) => s.lang.startsWith('fil');
export const shownLevel = (s: Story) => (s.level === s.language ? 'Beginner' : s.level);

/** Easy stories for a brand-new reader in the language they picked (same rule as the Kotlin app). */
export function starterStories(language: string): Story[] {
  return STORIES.filter((s) => language === 'Bilingual' || s.language === language || s.language === 'Bilingual')
    .sort((a, b) => a.minutes - b.minutes || a.pages.length - b.pages.length);
}
