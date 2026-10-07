// COPIED from the website (src/data/stories.js) by tools/sync-web.mjs. Edit it there, then run the script again.
const squirrelCover = 'story-squirrel-large.png';
const treeCover = 'story-tree.png';
const miaCover = 'mia-red-ball.svg';
const puppyCover = 'lost-puppy.svg';
const kiteCover = 'magic-kite.svg';
const catDogCover = 'cat-and-dog.svg';

// Sample storybooks. `lang` is the BCP-47 tag the browser voice uses for read-aloud.
export const STORIES = [
  {
    id: 'mia-and-the-red-ball',
    title: 'Mia and the Red Ball',
    language: 'English',
    lang: 'en-US',
    level: 'Beginner',
    minutes: 8,
    ages: '5–7 years',
    theme: 'Sharing',
    color: '#3E89C9',
    cover: miaCover,
    summary: 'A beginner-friendly English story about sharing and friendship.',
    blurb: 'Mia finds a red ball under a big tree and discovers that sharing makes playtime even more fun.',
    focus: { letter: 'Bb', sound: '/b/', example: 'ball', exampleFil: 'bola', syllables: ['ba', 'be', 'bi', 'bo', 'bu'], parts: ['b', 'a', 'll'], partsFil: ['bo', 'la'] },
    pages: [
      { text: 'Mia saw a red ball under a big tree.', word: 'ball' },
      { text: 'She picked up the ball and bounced it.', word: 'bounced' },
      { text: 'Ben came by and looked at the ball.', word: 'Ben' },
      { text: 'Mia gave Ben a turn with the ball.', word: 'turn' },
      { text: 'Sharing the ball made them both smile.', word: 'smile' },
    ],
    quiz: {
      question: 'What did Mia find?',
      options: [
        { label: 'A red ball', color: '#E74C3C', correct: true },
        { label: 'A kite', color: '#3E89C9' },
        { label: 'A puppy', color: '#C49A6C' },
      ],
    },
  },
  {
    id: 'the-lost-puppy',
    title: 'The Lost Puppy',
    language: 'English',
    lang: 'en-US',
    level: 'Beginner',
    minutes: 8,
    ages: '5–7 years',
    theme: 'Kindness',
    color: '#2F6FA8',
    cover: puppyCover,
    summary: 'A gentle story about helping someone who is lost.',
    blurb: 'A little puppy gets lost in the park, and two friends help it find its way home.',
    focus: { letter: 'Pp', sound: '/p/', example: 'pup', exampleFil: 'pato', syllables: ['pa', 'pe', 'pi', 'po', 'pu'], parts: ['p', 'u', 'p'], partsFil: ['pa', 'to'] },
    pages: [
      { text: 'A small pup sat by the park gate.', word: 'pup' },
      { text: 'The pup was sad and could not find home.', word: 'home' },
      { text: 'Lia and Paolo saw the pup.', word: 'saw' },
      { text: 'They read the name tag on its neck.', word: 'tag' },
      { text: 'They walked the pup home, and it wagged its tail.', word: 'tail' },
    ],
    quiz: {
      question: 'Where was the puppy?',
      options: [
        { label: 'At the park', color: '#46B97B', correct: true },
        { label: 'At school', color: '#FF7A22' },
        { label: 'At the beach', color: '#3E89C9' },
      ],
    },
  },
  {
    id: 'ang-munting-puno',
    title: 'Ang Munting Puno',
    language: 'Filipino',
    lang: 'fil-PH',
    level: 'Filipino',
    minutes: 8,
    ages: '5–7 years',
    theme: 'Pag-aalaga',
    color: '#46B97B',
    cover: treeCover,
    summary: 'Isang kuwento tungkol sa pag-aalaga ng halaman.',
    blurb: 'Nagtanim si Ana ng maliit na puno at inalagaan niya ito araw-araw.',
    focus: { letter: 'Mm', sound: '/m/', example: 'mama', exampleFil: 'mama', syllables: ['ma', 'me', 'mi', 'mo', 'mu'], parts: ['ma', 'ma'], partsFil: ['ma', 'ma'] },
    pages: [
      { text: 'May maliit na puno si Ana.', word: 'puno' },
      { text: 'Dinidiligan niya ito tuwing umaga.', word: 'umaga' },
      { text: 'Lumaki ang puno sa tulong ng araw.', word: 'araw' },
      { text: 'Nagkaroon ito ng maraming dahon.', word: 'dahon' },
      { text: 'Masaya si Ana sa kanyang puno.', word: 'Masaya' },
    ],
    quiz: {
      question: 'Ano ang inalagaan ni Ana?',
      options: [
        { label: 'Puno', color: '#46B97B', correct: true },
        { label: 'Pusa', color: '#F6C85F' },
        { label: 'Bola', color: '#E74C3C' },
      ],
    },
  },
  {
    id: 'the-magic-kite',
    title: 'The Magic Kite',
    language: 'Bilingual',
    lang: 'en-US',
    level: 'Bilingual',
    minutes: 8,
    ages: '5–7 years',
    theme: 'Imagination',
    color: '#FF7A22',
    cover: kiteCover,
    summary: 'A bilingual adventure with a kite that flies very high.',
    blurb: 'Sam’s kite flies up, up, up — saranggola — and shows Sam the whole town.',
    focus: { letter: 'Kk', sound: '/k/', example: 'kite', exampleFil: 'kahon', syllables: ['ka', 'ke', 'ki', 'ko', 'ku'], parts: ['k', 'i', 'te'], partsFil: ['ka', 'hon'] },
    pages: [
      { text: 'Sam has a kite. Saranggola!', word: 'kite' },
      { text: 'The wind is strong. Malakas ang hangin.', word: 'wind' },
      { text: 'The kite flies up, up, up!', word: 'up' },
      { text: 'Sam can see the whole town.', word: 'town' },
      { text: 'What a magic kite!', word: 'magic' },
    ],
    quiz: {
      question: 'What did Sam fly?',
      options: [
        { label: 'A kite', color: '#FF7A22', correct: true },
        { label: 'A plane', color: '#3E89C9' },
        { label: 'A bird', color: '#46B97B' },
      ],
    },
  },
  {
    id: 'my-first-school-day',
    title: 'My First School Day',
    language: 'English',
    lang: 'en-US',
    level: 'Beginner',
    minutes: 8,
    ages: '5–7 years',
    theme: 'Responsibility',
    color: '#F6B6C8',
    cover: squirrelCover,
    summary: 'A story about being brave on the first day of school.',
    blurb: 'Ms. Squirrel begins her first school day and discovers that learning and making friends can be lots of fun.',
    focus: { letter: 'Ss', sound: '/s/', example: 'school', exampleFil: 'saba', syllables: ['sa', 'se', 'si', 'so', 'su'], parts: ['s', 'chool'], partsFil: ['sa', 'ba'] },
    pages: [
      { text: 'Ms. Squirrel packs her bag for school.', word: 'school' },
      { text: 'She feels a little scared.', word: 'scared' },
      { text: 'Her teacher gives her a big smile.', word: 'smile' },
      { text: 'She sings a song with new friends.', word: 'sings' },
      { text: 'School is fun, says Ms. Squirrel.', word: 'fun' },
    ],
    quiz: {
      question: 'How did Ms. Squirrel feel at first?',
      options: [
        { label: 'A little scared', color: '#F6B6C8', correct: true },
        { label: 'Very sleepy', color: '#3E89C9' },
        { label: 'Hungry', color: '#F6C85F' },
      ],
    },
  },
  {
    id: 'cat-and-the-dog',
    title: 'Cat and the Dog',
    language: 'English',
    lang: 'en-US',
    level: 'Beginner',
    minutes: 6,
    ages: '5–7 years',
    theme: 'Friendship',
    color: '#F6C85F',
    cover: catDogCover,
    summary: 'Two very different pets learn to be friends.',
    blurb: 'A cat and a dog live in the same house. Can they learn to share the sunny spot?',
    focus: { letter: 'Dd', sound: '/d/', example: 'dog', exampleFil: 'daga', syllables: ['da', 'de', 'di', 'do', 'du'], parts: ['d', 'o', 'g'], partsFil: ['da', 'ga'] },
    pages: [
      { text: 'The cat naps in the sun.', word: 'cat' },
      { text: 'The dog wants the sun too.', word: 'dog' },
      { text: 'The cat moves a little.', word: 'moves' },
      { text: 'Now the cat and the dog nap together.', word: 'together' },
    ],
    quiz: {
      question: 'Where did the cat nap?',
      options: [
        { label: 'In the sun', color: '#F6C85F', correct: true },
        { label: 'In a box', color: '#C49A6C' },
        { label: 'On a tree', color: '#46B97B' },
      ],
    },
  },
];

export const getStory = (id) => STORIES.find((s) => s.id === id);

// Marungko Method letter order.
export const LESSONS = [
  ['Mm', 'mama', 'moon'], ['Aa', 'aso', 'apple'], ['Ss', 'saba', 'sun'], ['Ii', 'isda', 'insect'],
  ['Oo', 'oso', 'octopus'], ['Bb', 'bola', 'ball'], ['Ee', 'elepante', 'egg'], ['Uu', 'ubas', 'umbrella'],
  ['Tt', 'tasa', 'tree'], ['Kk', 'kahon', 'kite'], ['Ll', 'lapis', 'lion'], ['Yy', 'yoyo', 'yo-yo'],
  ['Nn', 'nanay', 'nest'], ['Gg', 'gatas', 'goat'], ['NGng', 'ngipin', 'ring'], ['Pp', 'pato', 'pig'],
  ['Rr', 'relo', 'rabbit'], ['Dd', 'daga', 'dog'], ['Hh', 'hipon', 'hat'], ['Ww', 'walis', 'web'],
].map(([letter, fil, eng], i) => ({ id: i + 1, letter, fil, eng }));

// Two more comprehension questions per story, asked after the story's main `quiz` question.
const MORE_QUESTIONS = {
  'mia-and-the-red-ball': [
    { question: 'Who came by to play?', options: [{ label: 'Ben', color: '#3E89C9', correct: true }, { label: 'Ana', color: '#46B97B' }, { label: 'Sam', color: '#FF7A22' }] },
    { question: 'How did Mia and Ben feel?', options: [{ label: 'Happy', color: '#F6C85F', correct: true }, { label: 'Sad', color: '#3E89C9' }, { label: 'Angry', color: '#E74C3C' }] },
  ],
  'the-lost-puppy': [
    { question: 'What helped them find the puppy’s home?', options: [{ label: 'A name tag', color: '#F6C85F', correct: true }, { label: 'A map', color: '#46B97B' }, { label: 'A ball', color: '#E74C3C' }] },
    { question: 'What did the puppy do at home?', options: [{ label: 'Wagged its tail', color: '#C49A6C', correct: true }, { label: 'Ran away', color: '#3E89C9' }, { label: 'Went to sleep', color: '#9B7FD1' }] },
  ],
  'ang-munting-puno': [
    { question: 'Kailan dinidiligan ni Ana ang puno?', options: [{ label: 'Tuwing umaga', color: '#F6C85F', correct: true }, { label: 'Tuwing gabi', color: '#183B5B' }, { label: 'Hindi kailanman', color: '#E74C3C' }] },
    { question: 'Ano ang naramdaman ni Ana?', options: [{ label: 'Masaya', color: '#46B97B', correct: true }, { label: 'Malungkot', color: '#3E89C9' }, { label: 'Galit', color: '#E74C3C' }] },
  ],
  'the-magic-kite': [
    { question: 'What is “kite” in Filipino?', options: [{ label: 'Saranggola', color: '#FF7A22', correct: true }, { label: 'Bola', color: '#E74C3C' }, { label: 'Puno', color: '#46B97B' }] },
    { question: 'What did Sam see from up high?', options: [{ label: 'The whole town', color: '#3E89C9', correct: true }, { label: 'The sea', color: '#46B97B' }, { label: 'A zoo', color: '#F6C85F' }] },
  ],
  'my-first-school-day': [
    { question: 'Who gave Ms. Squirrel a big smile?', options: [{ label: 'Her teacher', color: '#46B97B', correct: true }, { label: 'Her mom', color: '#F6B6C8' }, { label: 'A bird', color: '#3E89C9' }] },
    { question: 'What did she do with new friends?', options: [{ label: 'Sang a song', color: '#FF7A22', correct: true }, { label: 'Took a nap', color: '#183B5B' }, { label: 'Went home', color: '#C49A6C' }] },
  ],
  'cat-and-the-dog': [
    { question: 'What did the dog want?', options: [{ label: 'The sun', color: '#F6C85F', correct: true }, { label: 'A bone', color: '#C49A6C' }, { label: 'A ball', color: '#E74C3C' }] },
    { question: 'How did the story end?', options: [{ label: 'They napped together', color: '#46B97B', correct: true }, { label: 'They had a fight', color: '#E74C3C' }, { label: 'The cat ran away', color: '#3E89C9' }] },
  ],
};

// Comprehension questions for the Story Complete screen.
export const storyQuestions = (story) => [story.quiz, ...(MORE_QUESTIONS[story.id] ?? [])];
