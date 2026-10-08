import * as Speech from 'expo-speech';

// Read-aloud with the phone's own text-to-speech voices (expo-speech).
// Voice choice and word-by-word highlighting are simpler than on the website for now:
// the page text is read at the chosen speed, and `onWord` fires where the phone reports word boundaries.

export type SpeakOptions = { lang?: string; rate?: number; volume?: number; onDone?: () => void; onWord?: (charIndex: number) => void };

export function speak(text: string, { lang = 'en-US', rate = 1, volume = 1, onDone, onWord }: SpeakOptions = {}) {
  Speech.stop();
  Speech.speak(text.replace(/\bMs\./g, 'Miz'), {
    language: lang,
    rate: Math.max(0.5, Math.min(rate, 1.6)),
    volume,
    onDone,
    onStopped: onDone,
    onError: onDone,
    onBoundary: onWord ? (e: { charIndex: number }) => onWord(e.charIndex) : undefined,
  });
}

export const stopSpeaking = () => Speech.stop();
