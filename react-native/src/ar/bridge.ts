import type { ArScene } from '../content';
// The messages that pass between the app and the Unity + Vuforia AR scene.
// The Unity side is unity/BasaKitaAR/Assets/Scripts/BasaKitaBridge.cs; keep the two in step.
//
// App -> Unity: UnityView.postMessage(BRIDGE_OBJECT, method, json)
//   ShowPage puts 3D models on a printed story page: the page picture is made into a Vuforia Image Target
//   at runtime (StreamingAssets/pages/<target>.jpg), so no target database is needed.
// Unity -> App: a JSON string, e.g. {"type":"tracking","word":"bola"}

/** Name of the GameObject in the Unity scene that holds BasaKitaBridge.cs. */
export const BRIDGE_OBJECT = 'BasaKitaBridge';

/**
 * How the 3D picture is placed:
 * - 'ground': on a table or the floor (Vuforia Ground Plane). Works with no printed card.
 * - 'card': on top of a printed flashcard (Vuforia Image Target named after the word, e.g. "bola").
 */
export type ArMode = 'ground' | 'card';

export type ToUnity =
  | { method: 'Configure'; payload: { licenseKey: string } }
  | { method: 'ShowCard'; payload: { word: string; mode: ArMode } }
  | { method: 'ShowPage'; payload: { target: string; models: string[]; widthMeters: number; scene?: ArScene } }
  | { method: 'Cue'; payload: { actor: string; action: string; x: number; z: number } }
  | { method: 'ResetScene'; payload: Record<string, never> }
  | { method: 'ShowBook'; payload: { model: string; page: number; scene?: ArScene } }
  | { method: 'PlaceBook' | 'MoveBook'; payload: Record<string, never> }
  | { method: 'OpenBook' | 'ShowBookPage'; payload: { page: number; scene?: ArScene } }
  | { method: 'Stop'; payload: Record<string, never> };

export type FromUnity =
  | { type: 'ready' } // Vuforia started and the camera is on
  | { type: 'searching'; mode: ArMode } // looking for a surface or a card
  | { type: 'tracking'; word: string } // the 3D picture is showing
  | { type: 'lost'; word: string } // the card or surface went out of view
  | { type: 'error'; code: string; message: string }
  | { type: 'book'; state: BookState };

/** The placed 3D storybook: being placed, placed (closed), open, and when a page's scene has loaded. */
export type BookState = 'placing' | 'placed' | 'placed-surface' | 'open' | 'scene-ready';

export const configure = (licenseKey: string): ToUnity => ({ method: 'Configure', payload: { licenseKey } });
export const showCard = (word: string, mode: ArMode): ToUnity => ({ method: 'ShowCard', payload: { word, mode } });
export const stop = (): ToUnity => ({ method: 'Stop', payload: {} });
/** A printed A4 page is about 21 cm wide; Vuforia uses this to size the 3D models. */
export const showPage = (target: string, models: string[], widthMeters = 0.21, scene?: ArScene): ToUnity => ({ method: 'ShowPage', payload: { target, models, widthMeters, scene } });
export const cue = (actor: string, action: string, x = 0, z = 0): ToUnity => ({ method: 'Cue', payload: { actor, action, x, z } });
export const resetScene = (): ToUnity => ({ method: 'ResetScene', payload: {} });
/** The 3D storybook (unity StreamingAssets/models/<model>.glb) appears in front of the camera to be placed. */
export const showBook = (model: string, page: number, scene?: ArScene): ToUnity => ({ method: 'ShowBook', payload: { model, page, scene } });
export const placeBook = (): ToUnity => ({ method: 'PlaceBook', payload: {} });
export const moveBook = (): ToUnity => ({ method: 'MoveBook', payload: {} });
/** `page` is the printed page number (Butit Kulit's story starts on page 3). */
export const openBook = (page: number, scene?: ArScene): ToUnity => ({ method: 'OpenBook', payload: { page, scene } });
export const showBookPage = (page: number, scene?: ArScene): ToUnity => ({ method: 'ShowBookPage', payload: { page, scene } });

/** Turns Unity's message text into a typed message, or null if it isn't one we know. */
export function parseFromUnity(text: string): FromUnity | null {
  let m: unknown;
  try {
    m = JSON.parse(text);
  } catch {
    return null;
  }
  if (!m || typeof m !== 'object') return null;
  const o = m as Record<string, unknown>;
  const str = (k: string) => (typeof o[k] === 'string' ? (o[k] as string) : '');
  switch (o.type) {
    case 'ready':
      return { type: 'ready' };
    case 'searching':
      return { type: 'searching', mode: o.mode === 'card' ? 'card' : 'ground' };
    case 'tracking':
    case 'lost':
      return { type: o.type, word: str('word') };
    case 'error':
      return { type: 'error', code: str('code') || 'UNKNOWN', message: str('message') };
    case 'book':
      return ['placing', 'placed', 'placed-surface', 'open', 'scene-ready'].includes(str('mode')) ? { type: 'book', state: str('mode') as BookState } : null;
    default:
      return null;
  }
}

/** True when the key is missing or still the placeholder from .env.example. */
export const isPlaceholderKey = (key: string) => !key.trim() || key.trim() === 'YOUR_VUFORIA_LICENSE_KEY';

/** What to tell the child for each AR state. */
export function arMessage(state: FromUnity | { type: 'starting' }, eng: string, fil: string, mode: ArMode): string {
  switch (state.type) {
    case 'starting':
    case 'ready':
    case 'book':
      return 'Getting the camera ready…';
    case 'searching':
      return mode === 'card'
        ? `Point the camera at the ${eng} flashcard.`
        : 'Point the camera at the floor or a table and move the phone slowly.';
    case 'tracking':
      return `Here is the ${eng}! Say it: ${fil}`;
    case 'lost':
      return mode === 'card' ? 'Show the flashcard to the camera again.' : 'Move the phone slowly to find the table again.';
    case 'error':
      if (state.code === 'LICENSE') return 'AR needs a Vuforia license key. Ask a grown-up to add it in the app settings file.';
      if (state.code === 'NO_CARD_TARGET') return 'This flashcard can’t be scanned yet. Try “On a table” instead.';
      return 'AR could not start on this phone. You can still see the flashcard.';
  }
}

/** What to tell the child while AR looks for a story page. */
export function pageArMessage(state: FromUnity | { type: 'starting' }, page: number): string {
  switch (state.type) {
    case 'starting':
    case 'ready':
    case 'book':
      return 'Getting the camera ready…';
    case 'searching':
      return `Point the camera at page ${page} of the book.`;
    case 'tracking':
      return 'There it is! See the story in 3D.';
    case 'lost':
      return `Show page ${page} to the camera again.`;
    case 'error':
      if (state.code === 'LICENSE') return 'AR needs a Vuforia license key. Ask a grown-up to add it.';
      if (state.code === 'NO_PAGE_TARGET') return 'This page can’t be scanned yet.';
      return 'AR could not start on this phone.';
  }
}
