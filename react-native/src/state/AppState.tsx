import AsyncStorage from '@react-native-async-storage/async-storage';
import { createContext, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { relock } from '../shared/lock.js';
import * as M from './model';

type Api = M.SavedState & {
  ready: boolean;
  account: M.Account;
  displayName: string;
  login: (user: M.User) => void;
  logout: () => void;
  updateUser: (patch: Partial<M.User>) => void;
  updateSettings: (patch: Partial<M.Settings>) => void;
  openStory: (id: string) => void;
  savePage: (id: string, page: number) => void;
  finishStory: (id: string, stars: number) => void;
  toggleSaved: (id: string) => void;
  recordQuiz: (id: string, correct: number, total: number) => void;
  completeLesson: () => void;
  addCard: (card: Omit<M.MadeCard, 'id' | 'made'>) => void;
  practiceWord: (word: string) => void;
};

const Ctx = createContext<Api | null>(null);

/** The app's single source of truth, saved on the phone (the backend will keep it later). */
export function AppStateProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<M.SavedState>(M.DEFAULTS);
  const [ready, setReady] = useState(false);
  const loaded = useRef(false);

  useEffect(() => {
    AsyncStorage.getItem(M.STORAGE_KEY)
      .then((raw) => setState(M.parseSaved(raw)))
      .catch(() => {})
      .finally(() => { loaded.current = true; setReady(true); });
  }, []);

  useEffect(() => {
    if (loaded.current) AsyncStorage.setItem(M.STORAGE_KEY, JSON.stringify(state)).catch(() => {});
  }, [state]);

  const api = useMemo<Api>(() => {
    const change = (fn: (a: M.Account) => M.Account) => setState((s) => M.updateAccount(s, fn));
    return {
      ...state,
      ready,
      account: M.accountOf(state),
      displayName: state.user?.firstName || 'Reader',
      login: (user) => setState((s) => M.login(s, user)),
      logout: () => { relock(); setState((s) => ({ ...s, user: null })); },
      updateUser: (patch) => setState((s) => (s.user ? { ...s, user: { ...s.user, ...patch } } : s)),
      updateSettings: (patch) => setState((s) => ({ ...s, settings: { ...s.settings, ...patch } })),
      openStory: (id) => change((a) => M.openStory(a, id)),
      savePage: (id, page) => change((a) => M.savePage(a, id, page)),
      finishStory: (id, stars) => change((a) => M.finishStory(a, id, stars)),
      toggleSaved: (id) => change((a) => M.toggleSaved(a, id)),
      recordQuiz: (id, correct, total) => change((a) => M.recordQuiz(a, id, correct, total)),
      completeLesson: () => change(M.completeLesson),
      addCard: (card) => change((a) => M.addCard(a, card)),
      practiceWord: (word) => change((a) => M.practiceWord(a, word)),
    };
  }, [state, ready]);

  return <Ctx.Provider value={api}>{children}</Ctx.Provider>;
}

export function useApp(): Api {
  const api = useContext(Ctx);
  if (!api) throw new Error('useApp must be used inside <AppStateProvider>');
  return api;
}
