// Everyday objects a learner can scan to make a flashcard (Figure 7), in English and Filipino.
// Until the backend's picture recognition is connected, the learner taps which object the photo shows.
export type ScanObject = { eng: string; fil: string; emoji: string };

export const OBJECTS: ScanObject[] = [
  { eng: 'apple', fil: 'mansanas', emoji: '🍎' },
  { eng: 'banana', fil: 'saging', emoji: '🍌' },
  { eng: 'dog', fil: 'aso', emoji: '🐶' },
  { eng: 'cat', fil: 'pusa', emoji: '🐱' },
  { eng: 'fish', fil: 'isda', emoji: '🐟' },
  { eng: 'ball', fil: 'bola', emoji: '⚽' },
  { eng: 'book', fil: 'aklat', emoji: '📖' },
  { eng: 'pencil', fil: 'lapis', emoji: '✏️' },
  { eng: 'cup', fil: 'tasa', emoji: '☕' },
  { eng: 'spoon', fil: 'kutsara', emoji: '🥄' },
  { eng: 'chair', fil: 'upuan', emoji: '🪑' },
  { eng: 'shoe', fil: 'sapatos', emoji: '👟' },
  { eng: 'flower', fil: 'bulaklak', emoji: '🌸' },
  { eng: 'tree', fil: 'puno', emoji: '🌳' },
  { eng: 'car', fil: 'kotse', emoji: '🚗' },
  { eng: 'clock', fil: 'orasan', emoji: '🕰️' },
];

export const objectByEng = (eng: string) => OBJECTS.find((o) => o.eng === eng);
