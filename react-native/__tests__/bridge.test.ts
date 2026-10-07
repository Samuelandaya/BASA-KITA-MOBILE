import { arMessage, configure, isPlaceholderKey, parseFromUnity, showCard } from '../src/ar/bridge';

describe('AR bridge messages', () => {
  it('builds the messages Unity expects', () => {
    expect(configure('KEY')).toEqual({ method: 'Configure', payload: { licenseKey: 'KEY' } });
    expect(showCard('bola', 'card')).toEqual({ method: 'ShowCard', payload: { word: 'bola', mode: 'card' } });
  });

  it('reads what Unity sends, including JsonUtility’s empty fields', () => {
    expect(parseFromUnity('{"type":"ready","word":"","mode":"","code":"","message":""}')).toEqual({ type: 'ready' });
    expect(parseFromUnity('{"type":"tracking","word":"bola"}')).toEqual({ type: 'tracking', word: 'bola' });
    expect(parseFromUnity('{"type":"searching","mode":"card"}')).toEqual({ type: 'searching', mode: 'card' });
    expect(parseFromUnity('{"type":"error","code":"LICENSE","message":"x"}')).toEqual({ type: 'error', code: 'LICENSE', message: 'x' });
    expect(parseFromUnity('not json')).toBeNull();
    expect(parseFromUnity('{"type":"dance"}')).toBeNull();
  });

  it('spots a missing or placeholder license key', () => {
    expect(isPlaceholderKey('')).toBe(true);
    expect(isPlaceholderKey('YOUR_VUFORIA_LICENSE_KEY')).toBe(true);
    expect(isPlaceholderKey('AbC123')).toBe(false);
  });

  it('tells the child what to do', () => {
    expect(arMessage({ type: 'tracking', word: 'bola' }, 'ball', 'bola', 'ground')).toBe('Here is the ball! Say it: bola');
    expect(arMessage({ type: 'searching', mode: 'card' }, 'ball', 'bola', 'card')).toContain('ball flashcard');
    expect(arMessage({ type: 'error', code: 'LICENSE', message: '' }, 'ball', 'bola', 'ground')).toContain('license key');
  });
});
