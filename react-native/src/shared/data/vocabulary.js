// COPIED from the website (src/data/vocabulary.js) by tools/sync-web.mjs. Edit it there, then run the script again.
// Kid-friendly meanings for story words, with the word in the other language.
// Keys are lowercase words without punctuation.
export const VOCAB = {
  ball: { pair: 'bola', meaning: 'A round toy you can throw, kick or bounce.' },
  red: { pair: 'pula', meaning: 'The color of an apple or a fire truck.' },
  big: { pair: 'malaki', meaning: 'Large; not small.' },
  tree: { pair: 'puno', meaning: 'A tall plant with a trunk, branches and leaves.' },
  under: { pair: 'sa ilalim', meaning: 'Below something.' },
  picked: { pair: 'pinulot', meaning: 'Took something up with your hand.' },
  bounced: { pair: 'pinatalbog', meaning: 'Made it hit the ground and jump back up.' },
  turn: { pair: 'pagkakataon', meaning: 'Your time to play or do something.' },
  sharing: { pair: 'pagbabahagi', meaning: 'Letting others use or have some of what you have.' },
  smile: { pair: 'ngiti', meaning: 'A happy face with the corners of your mouth up.' },
  pup: { pair: 'tuta', meaning: 'A baby dog.' },
  park: { pair: 'parke', meaning: 'An outdoor place with grass and trees where people play.' },
  gate: { pair: 'tarangkahan', meaning: 'A door in a fence or wall.' },
  sad: { pair: 'malungkot', meaning: 'Feeling unhappy.' },
  home: { pair: 'bahay', meaning: 'The place where you live.' },
  tag: { pair: 'tatak', meaning: 'A small label with a name on it.' },
  tail: { pair: 'buntot', meaning: 'The part at the back of an animal that can wag.' },
  kite: { pair: 'saranggola', meaning: 'A toy that flies in the wind on a long string.' },
  wind: { pair: 'hangin', meaning: 'Moving air you can feel but cannot see.' },
  town: { pair: 'bayan', meaning: 'A place with many houses and people.' },
  magic: { pair: 'mahiwaga', meaning: 'Special and amazing, like in a fairy tale.' },
  school: { pair: 'paaralan', meaning: 'A place where children go to learn.' },
  bag: { pair: 'bag', meaning: 'Something you carry your things in.' },
  scared: { pair: 'takot', meaning: 'Feeling afraid.' },
  teacher: { pair: 'guro', meaning: 'A person who helps you learn.' },
  sings: { pair: 'kumakanta', meaning: 'Makes music with the voice.' },
  friends: { pair: 'kaibigan', meaning: 'People you like and play with.' },
  fun: { pair: 'masaya', meaning: 'Enjoyable; something you like doing.' },
  cat: { pair: 'pusa', meaning: 'A small furry pet that says “meow.”' },
  dog: { pair: 'aso', meaning: 'A furry pet that says “woof.”' },
  sun: { pair: 'araw', meaning: 'The big bright light in the sky in the day.' },
  naps: { pair: 'umiidlip', meaning: 'Sleeps for a short time.' },
  nap: { pair: 'idlip', meaning: 'A short sleep.' },
  together: { pair: 'magkasama', meaning: 'With each other.' },
  puno: { pair: 'tree', meaning: 'Halamang mataas na may katawan, sanga at dahon.' },
  maliit: { pair: 'small', meaning: 'Hindi malaki.' },
  umaga: { pair: 'morning', meaning: 'Ang simula ng araw, pagsikat ng araw.' },
  araw: { pair: 'sun / day', meaning: 'Ang maliwanag na bilog sa langit.' },
  dahon: { pair: 'leaves', meaning: 'Ang luntiang bahagi ng halaman.' },
  masaya: { pair: 'happy', meaning: 'Nararamdaman kapag ikaw ay natutuwa.' },
  dinidiligan: { pair: 'watering', meaning: 'Binubuhusan ng tubig ang halaman.' },
  lumaki: { pair: 'grew', meaning: 'Naging mas malaki.' },
  saranggola: { pair: 'kite', meaning: 'Laruan na lumilipad sa hangin.' },
  hangin: { pair: 'wind', meaning: 'Gumagalaw na hangin na nararamdaman natin.' },
  malakas: { pair: 'strong', meaning: 'May lakas; hindi mahina.' },
};

const VOWELS = 'aeiouáéíóú';

// English words don't follow simple consonant-vowel rules, so their parts are listed.
const EN_PARTS = {
  teacher: ['tea', 'cher'], together: ['to', 'geth', 'er'], under: ['un', 'der'], magic: ['ma', 'gic'],
  sharing: ['shar', 'ing'], bounced: ['boun', 'ced'], picked: ['pick', 'ed'], squirrel: ['squir', 'rel'],
  puppy: ['pup', 'py'], happy: ['hap', 'py'], apple: ['ap', 'ple'], rabbit: ['rab', 'bit'], insect: ['in', 'sect'],
  octopus: ['oc', 'to', 'pus'], umbrella: ['um', 'brel', 'la'], little: ['lit', 'tle'], playtime: ['play', 'time'],
};

// Word parts for kids: Filipino uses consonant-vowel syllables (with "ng" kept together);
// English uses the list above, otherwise the whole word.
export function syllables(word, lang = 'fil-PH') {
  const raw = word.toLowerCase().replace(/[^\p{L}]/gu, '');
  if (!lang.startsWith('fil')) return EN_PARTS[raw] ?? [raw];
  const w = raw.replace(/ng/g, 'ŋ');
  const parts = [];
  let cur = '';
  for (let i = 0; i < w.length; i++) {
    cur += w[i];
    const isV = VOWELS.includes(w[i]);
    const next = w[i + 1];
    const afterNext = w[i + 2];
    if (isV && next && !VOWELS.includes(next)) {
      // keep a single consonant for the next syllable; split clusters
      if (afterNext && !VOWELS.includes(afterNext)) { cur += next; i++; }
      parts.push(cur); cur = '';
    } else if (isV && next && VOWELS.includes(next)) {
      parts.push(cur); cur = '';
    }
  }
  if (cur) {
    if (parts.length && ![...cur].some((c) => VOWELS.includes(c))) parts[parts.length - 1] += cur;
    else parts.push(cur);
  }
  return (parts.length ? parts : [w]).map((p) => p.replace(/ŋ/g, 'ng'));
}
