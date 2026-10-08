// COPIED from the website (src/data/flashcards.js) by tools/sync-web.mjs. Edit it there, then run the script again.
// Flashcard decks, one per Marungko letter. Each card shows a rotating 3D object
// with its syllables and sound.
// `shape` picks a simple 3D stand-in model; `color` tints it.
export const DECKS = [
  { letter: 'Mm', cards: [
    { fil: 'mama', shape: 'person', eng: 'mother', syl: ['ma', 'ma'], color: '#F6B6C8' },
    { fil: 'mesa', shape: 'table', eng: 'table', syl: ['me', 'sa'], color: '#C49A6C' },
    { fil: 'mata', shape: 'eye', eng: 'eye', syl: ['ma', 'ta'], color: '#7FB3E0' },
  ] },
  { letter: 'Aa', cards: [
    { fil: 'aso', shape: 'animal', eng: 'dog', syl: ['a', 'so'], color: '#D9A066' },
    { fil: 'araw', shape: 'sun', eng: 'sun', syl: ['a', 'raw'], color: '#F6C85F' },
    { fil: 'ama', shape: 'person', eng: 'father', syl: ['a', 'ma'], color: '#8FD6AE' },
  ] },
  { letter: 'Ss', cards: [
    { fil: 'saba', shape: 'fruit', eng: 'banana', syl: ['sa', 'ba'], color: '#FFE27A' },
    { fil: 'susi', shape: 'key', eng: 'key', syl: ['su', 'si'], color: '#F6C85F' },
    { fil: 'sapatos', shape: 'shoe', eng: 'shoes', syl: ['sa', 'pa', 'tos'], color: '#E76F51' },
  ] },
  { letter: 'Ii', cards: [
    { fil: 'isda', shape: 'fish', eng: 'fish', syl: ['is', 'da'], color: '#7FC4F2' },
    { fil: 'ibon', shape: 'animal', eng: 'bird', syl: ['i', 'bon'], color: '#46B97B' },
    { fil: 'ilaw', shape: 'bulb', eng: 'light', syl: ['i', 'law'], color: '#FFE588' },
  ] },
  { letter: 'Oo', cards: [
    { fil: 'oso', shape: 'animal', eng: 'bear', syl: ['o', 'so'], color: '#9B6A43' },
    { fil: 'orasan', shape: 'clock', eng: 'clock', syl: ['o', 'ra', 'san'], color: '#9B8AFB' },
    { fil: 'okra', shape: 'fruit', eng: 'okra', syl: ['ok', 'ra'], color: '#5DB65A' },
  ] },
  { letter: 'Bb', cards: [
    { fil: 'bola', shape: 'ball', eng: 'ball', syl: ['bo', 'la'], color: '#E74C3C' },
    { fil: 'baka', shape: 'animal', eng: 'cow', syl: ['ba', 'ka'], color: '#ECE8DF' },
    { fil: 'bata', shape: 'person', eng: 'child', syl: ['ba', 'ta'], color: '#FFB27A' },
  ] },
];
