import * as M from '../src/state/model';

const user = (email: string, returning = false): M.User => ({ firstName: 'A', lastName: '', username: 'a', email, role: 'learner', returning });

describe('saved state', () => {
  it('falls back to the defaults for missing or broken saves', () => {
    expect(M.parseSaved(null)).toBe(M.DEFAULTS);
    expect(M.parseSaved('{oops')).toBe(M.DEFAULTS);
    const s = M.parseSaved(JSON.stringify({ settings: { volume: 20 } }));
    expect(s.settings.volume).toBe(20);
    expect(s.settings.voice).toBe('ana');
  });

  it('gives a returning learner the sample history and a new learner nothing', () => {
    const sam = M.login(M.DEFAULTS, user('Sam@BasaKita.ph', true));
    expect(M.accountOf(sam).sample).toBe(true);
    expect(M.isNewReader(M.accountOf(sam).progress)).toBe(false);
    const neo = M.login(M.DEFAULTS, user('new@x.ph'));
    expect(M.accountOf(neo)).toEqual(M.EMPTY_ACCOUNT);
  });

  it('keeps each account separate', () => {
    let s = M.login(M.DEFAULTS, user('a@x.ph'));
    s = M.updateAccount(s, M.completeLesson);
    s = M.login({ ...s, user: null }, user('b@x.ph'));
    expect(M.accountOf(s).lessonsDone).toBe(0);
    s = M.login({ ...s, user: null }, user('a@x.ph'));
    expect(M.accountOf(s).lessonsDone).toBe(1);
  });
});

describe('reading progress', () => {
  it('tracks pages, finishing and the story to continue', () => {
    let a = M.openStory(M.EMPTY_ACCOUNT, 'one', 1);
    a = M.savePage(a, 'one', 3, 2);
    a = M.openStory(a, 'two', 3);
    a = M.finishStory(a, 'two', 3, 4);
    expect(a.progress.one).toEqual({ page: 3, lastRead: 2 });
    expect(M.historyIds(a.progress)).toEqual(['two', 'one']);
    expect(M.continueId(a.progress)).toBe('one');
    expect(M.percentRead(5, a.progress.one)).toBe(40);
    expect(M.percentRead(5, a.progress.two)).toBe(100);
    expect(M.percentRead(5, undefined)).toBe(0);
  });

  it('toggles saved stories, newest first', () => {
    let a = M.toggleSaved(M.EMPTY_ACCOUNT, 'x');
    a = M.toggleSaved(a, 'y');
    expect(a.saved).toEqual(['y', 'x']);
    expect(M.toggleSaved(a, 'y').saved).toEqual(['x']);
  });
});

describe('flashcards', () => {
  it('allows five scans a day and starts again the next day', () => {
    const day1 = Date.UTC(2026, 10, 24, 3);
    let a = M.EMPTY_ACCOUNT;
    expect(M.scansLeft(a, day1)).toBe(5);
    for (let i = 0; i < 5; i++) a = M.addCard(a, { fil: 'aso', eng: 'dog', lang: 'Filipino' }, day1 + i);
    expect(a.cards).toHaveLength(5);
    expect(M.scansLeft(a, day1)).toBe(0);
    expect(M.scansLeft(a, day1 + 24 * 60 * 60 * 1000)).toBe(5);
  });

  it('remembers each practiced word once', () => {
    const a = M.practiceWord(M.practiceWord(M.EMPTY_ACCOUNT, 'aso'), 'aso');
    expect(a.words).toEqual(['aso']);
  });
});
