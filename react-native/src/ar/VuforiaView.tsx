import { useEffect, useRef, useState, type ComponentType } from 'react';
import { StyleSheet, View } from 'react-native';
import { BRIDGE_OBJECT, configure, parseFromUnity, showCard, stop, type ArMode, type FromUnity, type ToUnity } from './bridge';
import { unityAvailable, vuforiaLicenseKey } from './config';

type UnityRef = { postMessage: (gameObject: string, method: string, message: string) => void };
type UnityProps = { ref?: unknown; style?: object; androidKeepPlayerMounted?: boolean; onUnityMessage?: (e: { nativeEvent: { message: string } }) => void };

// Loaded only when the app was built with the Unity export, so builds without it never touch the native view.
const UnityView: ComponentType<UnityProps> | null = unityAvailable ? require('@azesmway/react-native-unity').default : null;

/**
 * The camera view with Vuforia AR, drawn by Unity. It sends the license key and the flashcard word
 * to Unity and reports what Unity sees through `onState`.
 */
export function VuforiaView({ word, mode, onState }: { word: string; mode: ArMode; onState: (s: FromUnity) => void }) {
  const ref = useRef<UnityRef>(null);
  const [started, setStarted] = useState(false);

  const send = (m: ToUnity) => ref.current?.postMessage(BRIDGE_OBJECT, m.method, JSON.stringify(m.payload));

  // Unity says "ready" once its scene loads; then it gets the key and the card to show.
  useEffect(() => {
    if (started) send(showCard(word, mode));
  }, [started, word, mode]);

  useEffect(() => () => send(stop()), []);

  if (!UnityView) return null;
  return (
    <View style={StyleSheet.absoluteFill}>
      <UnityView
        ref={ref}
        style={{ flex: 1 }}
        androidKeepPlayerMounted
        onUnityMessage={(e) => {
          const m = parseFromUnity(e.nativeEvent.message);
          if (!m) return;
          if (m.type === 'ready' && !started) {
            send(configure(vuforiaLicenseKey));
            setStarted(true);
          }
          onState(m);
        }}
      />
    </View>
  );
}
