// Paper: Chapter 2 Figure 20 (search bar with All, Filipino and English filters) and Use Case 7 (Search and Filter Stories).
import { STORIES } from '../src/content';
import { filterStories, isFiltering, matchesTitle, normalize } from '../src/storySearch';

const BOOKS = [
  { id: 'butit-kulit', title: 'Butit Kulit', language: 'Filipino' },
  { id: 'mag-kinder', title: 'Mag-Kinder si Ate', language: 'Filipino' },
  { id: 'red-ball', title: 'The Red Ball', language: 'English' },
  { id: 'bola', title: 'Ang Bola / The Ball', language: 'Bilingual' },
];
const ids = (list: { id: string }[]) => list.map((s) => s.id);

describe('story title search', () => {
  it('ignores case, accents and extra spaces', () => {
    expect(normalize('  BUTIT   Kulít ')).toBe('butit kulit');
    expect(matchesTitle(BOOKS[0], 'butit')).toBe(true);
    expect(matchesTitle(BOOKS[0], 'KULIT')).toBe(true);
    expect(matchesTitle(BOOKS[0], 'kulit butit')).toBe(true);
    expect(matchesTitle(BOOKS[0], 'aso')).toBe(false);
  });

  it('shows every story for an empty or blank search', () => {
    expect(ids(filterStories(BOOKS, { query: '' }))).toEqual(ids(BOOKS));
    expect(ids(filterStories(BOOKS, { query: '   ' }))).toEqual(ids(BOOKS));
  });

  it('matches part of a title', () => {
    expect(ids(filterStories(BOOKS, { query: 'ball' }))).toEqual(['red-ball', 'bola']);
    expect(ids(filterStories(BOOKS, { query: 'kinder' }))).toEqual(['mag-kinder']);
  });
});

describe('search combined with the language filter', () => {
  it('filters by language alone', () => {
    expect(ids(filterStories(BOOKS, { language: 'All' }))).toEqual(ids(BOOKS));
    expect(ids(filterStories(BOOKS, { language: 'Filipino' }))).toEqual(['butit-kulit', 'mag-kinder', 'bola']);
    expect(ids(filterStories(BOOKS, { language: 'English' }))).toEqual(['red-ball', 'bola']);
  });

  it('needs both the title and the language to match', () => {
    expect(ids(filterStories(BOOKS, { query: 'ball', language: 'English' }))).toEqual(['red-ball', 'bola']);
    expect(ids(filterStories(BOOKS, { query: 'butit', language: 'Filipino' }))).toEqual(['butit-kulit']);
    expect(filterStories(BOOKS, { query: 'butit', language: 'English' })).toEqual([]);
    expect(filterStories(BOOKS, { query: 'dinosaur', language: 'All' })).toEqual([]);
  });

  it('knows when the learner is filtering', () => {
    expect(isFiltering('', 'All')).toBe(false);
    expect(isFiltering('  ', 'All')).toBe(false);
    expect(isFiltering('a', 'All')).toBe(true);
    expect(isFiltering('', 'English')).toBe(true);
  });

  it('finds the bundled Butit Kulit story', () => {
    expect(ids(filterStories(STORIES, { query: 'butit', language: 'Filipino' }))).toEqual(['butit-kulit']);
    expect(filterStories(STORIES, { query: 'butit', language: 'English' })).toEqual([]);
  });
});
