// COPIED from the website (src/learner.js) by tools/sync-web.mjs. Edit it there, then run the script again.
import { LESSONS, STORIES } from './data/stories.js';

const avg = (xs) => (xs.length ? Math.round(xs.reduce((n, x) => n + x, 0) / xs.length) : null);

// Skill percentages from what the learner has actually done.
export function learnerSkills({ progress, lessonsDone, scores = {}, quizzes = {} }) {
  const all = Object.values(scores);
  const quiz = Object.values(quizzes);
  const finished = STORIES.filter((s) => progress[s.id]?.finished).length;
  return {
    letterSounds: Math.round((lessonsDone / LESSONS.length) * 100),
    english: avg(all.filter((x) => x.english).map((x) => x.score)), // English pronunciation score
    filipino: avg(all.filter((x) => !x.english).map((x) => x.score)), // Filipino word reading accuracy
    reading: avg(all.map((x) => x.score)),
    understanding: quiz.length ? Math.round((quiz.reduce((n, q) => n + q.correct, 0) / quiz.reduce((n, q) => n + q.total, 0)) * 100) : null,
    finished,
  };
}

export const LEVELS = [
  { name: 'Beginner', color: '#FFF3D6', text: '#183B5B', goal: 'Finish 2 stories and learn 4 letter sounds' },
  { name: 'Growing', color: '#DDF3E6', text: '#1D6B42', goal: 'Finish 5 stories, learn 10 sounds and read aloud at 80% or more' },
  { name: 'Confident', color: '#E8F1FA', text: '#1F4F7A', goal: 'Keep reading new stories!' },
];

// Learner level from finished stories, letter sounds learned and read-aloud scores.
export function learnerLevel(data) {
  const sk = learnerSkills(data);
  const goodReading = sk.reading == null || sk.reading >= 80;
  let i = 0;
  if (sk.finished >= 2 && data.lessonsDone >= 4) i = 1;
  if (sk.finished >= 5 && data.lessonsDone >= 10 && goodReading) i = 2;
  // Progress toward the next level, 0–100.
  const toNext = i === 2 ? 100 : i === 0
    ? Math.round(((Math.min(sk.finished, 2) / 2) + (Math.min(data.lessonsDone, 4) / 4)) * 50)
    : Math.round(((Math.min(sk.finished, 5) / 5) + (Math.min(data.lessonsDone, 10) / 10) + (goodReading ? 1 : 0)) / 3 * 100);
  return { ...LEVELS[i], index: i, toNext, next: LEVELS[i + 1]?.name, skills: sk };
}

// Stories to suggest: not finished yet, in the chosen language, practicing sounds the
// learner already knows first, and easier stories for beginners.
export function recommendStories({ progress, lessonsDone, language = 'Bilingual' }, level, count = 4) {
  const known = new Set(LESSONS.slice(0, Math.max(lessonsDone, 1)).map((l) => l.letter));
  const fits = (s) => language === 'Bilingual' || s.language === language || s.language === 'Bilingual';
  const score = (s) =>
    (progress[s.id] ? 0 : 2) + (fits(s) ? 3 : 0) + (known.has(s.focus.letter) ? 2 : 0) +
    (level.index === 0 ? (s.level === 'Beginner' || s.level === s.language ? 1 : 0) : 0);
  const reason = (s) =>
    known.has(s.focus.letter) ? `Practice the ${s.focus.letter} sound` : !progress[s.id] ? 'New for you' : 'Keep going';
  return STORIES.filter((s) => !progress[s.id]?.finished)
    .sort((a, b) => score(b) - score(a) || a.minutes - b.minutes)
    .slice(0, count)
    .map((s) => ({ story: s, reason: reason(s) }));
}
