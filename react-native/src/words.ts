import { VOCAB } from './content';

/** Splits page text into words with where each one starts, for highlighting while it is read aloud. */
export function splitWords(text: string): { word: string; start: number }[] {
  const out: { word: string; start: number }[] = [];
  const re = /\S+/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text))) out.push({ word: m[0], start: m.index });
  return out;
}

/** Which word is being read, given the character position the voice reported. */
export function wordAt(words: { start: number }[], charIndex: number): number {
  let i = -1;
  for (let k = 0; k < words.length && words[k].start <= charIndex; k++) i = k;
  return i;
}

/** Lowercase word without punctuation, the key used in the vocabulary list. */
export const cleanWord = (w: string) => w.toLowerCase().replace(/[^\p{L}\p{N}'-]/gu, '').replace(/^'+|'+$/g, '');

export const meaningOf = (w: string) => VOCAB[cleanWord(w)];

const VOWELS = 'aeiouáéíóú';
/**
 * Splits a Filipino word into syllables (pantig) for Read & Learn: a consonant that comes right
 * before a vowel starts a new syllable, and "ng" counts as one consonant. Example: paaralan -> pa-a-ra-lan.
 */
export function syllables(word: string): string[] {
  const w = cleanWord(word);
  const units: string[] = [];
  for (let i = 0; i < w.length; i++) {
    if (w[i] === 'n' && w[i + 1] === 'g') { units.push('ng'); i++; } else units.push(w[i]);
  }
  const isV = (u?: string) => !!u && VOWELS.includes(u);
  const out: string[] = [];
  let cur = '';
  units.forEach((u, i) => {
    const startsNew = cur !== '' && (isV(u) ? isV(units[i - 1]) : isV(units[i + 1]) && /[aeiouáéíóú]/.test(cur));
    if (startsNew) { out.push(cur); cur = ''; }
    cur += u;
  });
  if (cur) out.push(cur);
  return out;
}
