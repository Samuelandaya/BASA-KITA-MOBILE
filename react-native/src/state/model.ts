// Saved data, with the same shape and storage key as the website (src/AppState.jsx),
// so a backend can later return one format for the website and both apps.
// These are plain functions so they can be unit tested without a phone.

export type Progress = { page: number; finished?: boolean; stars?: number; lastRead?: number };
export type ScoreEntry = { score: number; english: boolean };
export type QuizResult = { correct: number; total: number };
export type Draft = { id: string; title: string; language: string; character: string; place: string; pages: number; texts: string[] };
export type Account = {
  progress: Record<string, Progress>;
  lessonsDone: number;
  drafts: Draft[];
  saved: string[];
  scores: Record<string, ScoreEntry>; // "storyId:page"
  quizzes: Record<string, QuizResult>;
  sample: boolean;
};
export type User = {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  role: 'learner' | 'admin' | 'pending';
  returning?: boolean;
};
export type Settings = {
  language: 'Filipino' | 'English' | 'Bilingual';
  languageChosen: boolean;
  speed: number;
  textSize: 'Normal' | 'Big' | 'Bigger';
  highlight: boolean;
  narration: boolean;
  effects: boolean;
  music: boolean;
  volume: number; // 0-100
  parentLock: boolean;
  pin: string;
  reminder: boolean;
  voice: string;
};
export type SavedState = { user: User | null; avatarColor: string; settings: Settings; accounts: Record<string, Account> };

// v3: the app now has only Butit Kulit, so saved progress on the old sample stories is dropped.
export const STORAGE_KEY = 'basa-kita:state:v3';

export const EMPTY_ACCOUNT: Account = { progress: {}, lessonsDone: 0, drafts: [], saved: [], scores: {}, quizzes: {}, sample: false };

const DAY = 24 * 60 * 60 * 1000;

/** Demo "returning student" (sam@basakita.ph) so the regular home page can be shown. */
export function sampleAccount(now = Date.now()): Account {
  const daysAgo = (n: number) => now - n * DAY;
  return {
    progress: {
      'butit-kulit': { page: 8, finished: true, stars: 3, lastRead: daysAgo(0.1) },
    },
    saved: ['butit-kulit'],
    scores: {
      'butit-kulit:1': { score: 80, english: false }, 'butit-kulit:2': { score: 74, english: false },
    },
    quizzes: { 'butit-kulit': { correct: 3, total: 3 } },
    lessonsDone: 5,
    drafts: [{ id: 'draft-1', title: 'Ang Pusa ni Sam', language: 'Filipino', character: 'Cat', place: 'Home', pages: 3, texts: ['Si Sam ay may pusa.', 'Ang pusa ay puti at malambot.', 'Mahal ni Sam ang kanyang pusa.'] }],
    sample: true,
  };
}

export const DEFAULTS: SavedState = {
  user: null,
  avatarColor: '#FFB27A',
  settings: {
    language: 'Bilingual', languageChosen: false, speed: 1, textSize: 'Big', highlight: true, narration: true,
    effects: true, music: false, volume: 70, parentLock: false, pin: '', reminder: false, voice: 'ana',
  },
  accounts: {},
};

/** Reads a saved state; anything missing or broken falls back to the defaults. */
export function parseSaved(raw: string | null): SavedState {
  if (!raw) return DEFAULTS;
  try {
    const saved = JSON.parse(raw);
    return { ...DEFAULTS, ...saved, settings: { ...DEFAULTS.settings, ...saved.settings } };
  } catch {
    return DEFAULTS;
  }
}

export const accountKey = (user: User | null) => (user?.email || user?.username || 'guest').toLowerCase();

export const accountOf = (s: SavedState): Account => ({ ...EMPTY_ACCOUNT, ...s.accounts[accountKey(s.user)] });

export function login(s: SavedState, user: User, now = Date.now()): SavedState {
  const k = accountKey(user);
  const accounts = s.accounts[k] ? s.accounts : { ...s.accounts, [k]: user.returning ? sampleAccount(now) : EMPTY_ACCOUNT };
  return { ...s, user, accounts };
}

export function updateAccount(s: SavedState, fn: (a: Account) => Account): SavedState {
  const k = accountKey(s.user);
  return { ...s, accounts: { ...s.accounts, [k]: fn(s.accounts[k] ?? EMPTY_ACCOUNT) } };
}

// Changes to the signed-in learner's account. Each returns a new account.
const entry = (a: Account, id: string): Progress => a.progress[id] ?? { page: 1 };
export const openStory = (a: Account, id: string, now = Date.now()): Account =>
  ({ ...a, progress: { ...a.progress, [id]: { ...entry(a, id), lastRead: now } } });
export const savePage = (a: Account, id: string, page: number, now = Date.now()): Account =>
  ({ ...a, progress: { ...a.progress, [id]: { ...entry(a, id), page, lastRead: now } } });
export const finishStory = (a: Account, id: string, stars: number, now = Date.now()): Account =>
  ({ ...a, progress: { ...a.progress, [id]: { ...entry(a, id), finished: true, stars, lastRead: now } } });
export const toggleSaved = (a: Account, id: string): Account =>
  ({ ...a, saved: a.saved.includes(id) ? a.saved.filter((x) => x !== id) : [id, ...a.saved] });
export const recordQuiz = (a: Account, id: string, correct: number, total: number): Account =>
  ({ ...a, quizzes: { ...a.quizzes, [id]: { correct, total } } });
export const completeLesson = (a: Account): Account => ({ ...a, lessonsDone: a.lessonsDone + 1 });

// Helpers shared by screens.
export const isNewReader = (progress: Record<string, Progress>) => !Object.values(progress).some((p) => p.finished);

/** How much of a story has been read, 0-100. */
export function percentRead(pages: number, entry?: Progress): number {
  if (!entry) return 0;
  if (entry.finished) return 100;
  return Math.round(((Math.min(entry.page ?? 1, pages) - 1) / pages) * 100);
}

/** Story ids, most recently read first. */
export const historyIds = (progress: Record<string, Progress>) =>
  Object.entries(progress).filter(([, p]) => p.lastRead).sort((a, b) => b[1].lastRead! - a[1].lastRead!).map(([id]) => id);

/** The story to pick up again: the most recently read one that isn't finished. */
export const continueId = (progress: Record<string, Progress>) => historyIds(progress).find((id) => !progress[id].finished);
