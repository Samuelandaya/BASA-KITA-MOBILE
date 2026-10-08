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
