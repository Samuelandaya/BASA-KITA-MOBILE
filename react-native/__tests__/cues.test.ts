import { cuesBetween, timeCues } from '../src/ar/cues';
import { STORIES } from '../src/content';

describe('AR story cues', () => {
  it('pins cues to words by position or exact word, in reading order', () => {
    const timed = timeCues('Ako ay may alaga. Siya si Butit. Butit ang pangalan.', [
      { word: 'Butit.', actor: 'butit', action: 'jump' },
      { at: 0, actor: 'butit', action: 'walkTo' },
      { word: 'wala', actor: 'butit', action: 'eat' }, // not in the text: dropped
    ]);
    expect(timed.map((t) => [t.index, t.cue.action])).toEqual([[0, 'walkTo'], [6, 'jump']]);
  });

  it('fires each cue once as the voice moves forward', () => {
    const timed = timeCues('a b c d e', [{ at: 1, actor: 'x', action: 'one' }, { at: 3, actor: 'x', action: 'two' }]);
    expect(cuesBetween(timed, -1, 0)).toHaveLength(0);
    expect(cuesBetween(timed, 0, 2).map((t) => t.cue.action)).toEqual(['one']);
    expect(cuesBetween(timed, 2, 4).map((t) => t.cue.action)).toEqual(['two']);
  });

  it('finds every Butit Kulit cue in its page text', () => {
    for (const page of STORIES.flatMap((s) => s.pages)) {
      expect(timeCues(page.text, page.arCues)).toHaveLength(page.arCues?.length ?? 0);
    }
  });

  it('has no narration on printed page 5, which is only a drawing', () => {
    expect(STORIES[0].pages[2].text).toBe('');
  });
});
