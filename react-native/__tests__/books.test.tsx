// Books screen: Chapter 2 Figure 20, Use Case 6 (Browse Storybooks), Use Case 7 (Search and Filter Stories)
// and Figure 6 (no matching story shows a message and returns to the book list).
import { act, create, type ReactTestInstance, type ReactTestRenderer } from 'react-test-renderer';
import { Text, TextInput } from 'react-native';

const mockPush = jest.fn();
jest.mock('expo-router', () => ({ router: { push: (...a: unknown[]) => mockPush(...a) } }));
jest.mock('@expo/vector-icons/Ionicons', () => () => null);
jest.mock('../src/ui/Cover', () => ({ Cover: () => null }));
jest.mock('../src/state/AppState', () => ({
  useApp: () => ({ account: { saved: [], progress: {} } }),
}));
jest.mock('../src/content', () => {
  const story = (id: string, title: string, language: string) => ({
    id, title, language, lang: language === 'English' ? 'en-US' : 'fil-PH', level: 'Beginner', minutes: 5,
    ages: '4-6', theme: '', color: '#fff', cover: 'x', summary: '', blurb: '', pages: [{ text: '', word: '' }],
  });
  const STORIES = [
    story('butit-kulit', 'Butit Kulit', 'Filipino'),
    story('red-ball', 'The Red Ball', 'English'),
    story('bola', 'Ang Bola / The Ball', 'Bilingual'),
  ];
  return { STORIES, storyById: (id: string) => STORIES.find((s) => s.id === id), shownLevel: (s: { level: string }) => s.level };
});

import Books from '../src/app/(tabs)/books';

let tree: ReactTestRenderer;
const root = () => tree.root;
const cards = () => root().findAll((n) => n.props.accessibilityRole === 'button' && typeof n.props.accessibilityLabel === 'string' && n.props.onPress && n.props.style && Array.isArray(n.props.style) && n.props.style.some((x: { width?: number }) => x && x.width === 158)).map((n) => n.props.accessibilityLabel as string);
const unique = (labels: string[]) => [...new Set(labels)];
const titles = () => unique(cards());
const text = () => root().findAllByType(Text).map((n) => [n.props.children].flat().join('')).join('\n');
const press = (n: ReactTestInstance) => act(() => { n.props.onPress(); });
const button = (label: string) => root().find((n) => n.props.accessibilityRole === 'button' && n.props.onPress && (n.props.accessibilityLabel === label || n.findAllByType(Text).some((t) => [t.props.children].flat().join('') === label)));
const type = (q: string) => act(() => { root().findByType(TextInput).props.onChangeText(q); });

beforeEach(() => {
  mockPush.mockClear();
  act(() => { tree = create(<Books />); });
});
afterEach(() => act(() => tree.unmount()));

describe('Books screen', () => {
  it('shows every story with the search bar and All, Filipino and English filters', () => {
    expect(text()).toContain('Explore Storybooks');
    expect(root().findByType(TextInput).props.placeholder).toBe('Search stories');
    for (const f of ['All', 'Filipino', 'English']) expect(button(f)).toBeTruthy();
    expect(titles()).toEqual(['Butit Kulit', 'The Red Ball', 'Ang Bola / The Ball']);
  });

  it('combines the title search with the language filter', () => {
    type('ball');
    expect(titles()).toEqual(['The Red Ball', 'Ang Bola / The Ball']);
    press(button('Filipino'));
    expect(titles()).toEqual(['Ang Bola / The Ball']);
    press(button('English'));
    expect(titles()).toEqual(['The Red Ball', 'Ang Bola / The Ball']);
    type('');
    press(button('Filipino'));
    expect(titles()).toEqual(['Butit Kulit', 'Ang Bola / The Ball']);
  });

  it('shows a message when nothing matches and returns to the full list', () => {
    type('butit');
    press(button('English'));
    expect(titles()).toEqual([]);
    expect(text()).toContain('No stories found');
    expect(text()).toContain('No English story is called “butit”.');
    press(button('Show all stories'));
    expect(text()).not.toContain('No stories found');
    expect(root().findByType(TextInput).props.value).toBe('');
    expect(titles()).toEqual(['Butit Kulit', 'The Red Ball', 'Ang Bola / The Ball']);
  });

  it('clears the search with the clear button', () => {
    type('zzz');
    expect(text()).toContain('No stories found');
    press(button('Clear search'));
    expect(titles()).toHaveLength(3);
  });

  it('opens the matching story when the learner taps it', () => {
    type('red');
    expect(titles()).toEqual(['The Red Ball']);
    press(button('The Red Ball'));
    expect(mockPush).toHaveBeenCalledTimes(1);
    expect(mockPush).toHaveBeenCalledWith('/story/red-ball');
  });
});
