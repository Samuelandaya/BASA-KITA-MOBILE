import type { ArCue } from '../content';
import { splitWords } from '../words';

export type TimedCue = { index: number; cue: ArCue };

/**
 * Pins each cue to a word of the page text: `at` is the word's position, `word` the first word
 * that matches exactly (punctuation included, so "Butit." is the end of a sentence).
 * Cues whose word isn't in the text are dropped. Sorted by word.
 */
export function timeCues(text: string, cues: ArCue[] = []): TimedCue[] {
  const words = splitWords(text).map((w) => w.word);
  const out: TimedCue[] = [];
  for (const cue of cues) {
    const index = cue.at ?? (cue.word ? words.indexOf(cue.word) : -1);
    if (index >= 0 && index < Math.max(words.length, 1)) out.push({ index, cue });
  }
  return out.sort((a, b) => a.index - b.index);
}

/** The cues to fire when the voice moves on from word `after` to word `upTo` (after < index <= upTo). */
export const cuesBetween = (timed: TimedCue[], after: number, upTo: number) => timed.filter((t) => t.index > after && t.index <= upTo);
