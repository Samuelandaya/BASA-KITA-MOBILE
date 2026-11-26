// Story title search and language filter for the Books screen.
// Paper: Chapter 2, Figure 20 (Storybooks Page: search bar with All, Filipino and English filters),
// Use Case 7 (Search and Filter Stories) and Figure 6 (Storybook flowchart: no match shows a message
// and returns to the book list).

export type LanguageFilter = 'All' | 'Filipino' | 'English';
export const LANGUAGE_FILTERS: LanguageFilter[] = ['All', 'Filipino', 'English'];

type Searchable = { title: string; language: string };

/** Lowercase, trimmed, with accents and extra spaces removed, so "  BUTIT  kulít " matches "Butit Kulit". */
export const normalize = (text: string) =>
  text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().replace(/\s+/g, ' ').trim();

/** Bilingual stories have both languages, so they appear under Filipino and under English. */
export const matchesLanguage = (story: Searchable, language: LanguageFilter) =>
  language === 'All' || story.language === language || story.language === 'Bilingual';

/** True when every word the learner typed appears in the title, in any order. An empty search matches every title. */
export const matchesTitle = (story: Searchable, query: string) => {
  const title = normalize(story.title);
  return normalize(query).split(' ').filter(Boolean).every((word) => title.includes(word));
};

/** The stories whose title matches the search and whose language matches the filter, in their original order. */
export function filterStories<S extends Searchable>(stories: S[], { query = '', language = 'All' }: { query?: string; language?: LanguageFilter }): S[] {
  return stories.filter((s) => matchesLanguage(s, language) && matchesTitle(s, query));
}

/** True when the learner has typed a search or picked a language, so an empty list means "nothing matches". */
export const isFiltering = (query: string, language: LanguageFilter) => normalize(query) !== '' || language !== 'All';
