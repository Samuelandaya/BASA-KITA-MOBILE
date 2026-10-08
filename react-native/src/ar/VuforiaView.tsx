import { useEffect, useRef, useState, type ComponentType } from 'react';
import { StyleSheet, View } from 'react-native';
import { BRIDGE_OBJECT, configure, parseFromUnity, stop, type FromUnity, type ToUnity } from './bridge';
import { unityAvailable, vuforiaLicenseKey } from './config';

type UnityRef = { postMessage: (gameObject: string, method: string, message: string) => void };
type UnityProps = { ref?: unknown; style?: object; onUnityMessage?: (e: { nativeEvent: { message: string } }) => void };

// Loaded only when the app was built with the Unity export, so builds without it never touch the native view.
const UnityView: ComponentType<UnityProps> | null = unityAvailable ? require('@azesmway/react-native-unity').default : null;

/**
 * The camera view with Vuforia AR, drawn by Unity. It sends the license key and what to show
 * (`show`: a flashcard, see showCard, or a story page, see showPage) to Unity and reports what
 * Unity sees through `onState`.
 */
export function VuforiaView({ show, onState }: { show: ToUnity; onState: (s: FromUnity) => void }) {
  const ref = useRef<UnityRef>(null);
  const [started, setStarted] = useState(false);

  const send = (m: ToUnity) => ref.current?.postMessage(BRIDGE_OBJECT, m.method, JSON.stringify(m.payload));

  // Unity says "ready" once its scene loads; then it gets the key and the card to show.
  // In case that first message is missed, start anyway after a short wait (Unity ignores repeats).
  useEffect(() => {
    const t = setTimeout(() => {
      send(configure(vuforiaLicenseKey));
      setStarted(true);
    }, 2500);
    return () => clearTimeout(t);
  }, []);

  const showKey = JSON.stringify(show);
  useEffect(() => {
    if (started) send(show);
  }, [started, showKey]);

  useEffect(() => () => send(stop()), []);

  if (!UnityView) return null;
  return (
    <View style={StyleSheet.absoluteFill}>
      <UnityView
        ref={ref}
        style={{ flex: 1 }}
        // Not androidKeepPlayerMounted: with it, leaving the AR screen leaves Unity attached to the
        // destroyed view and opening AR again crashes ("Graphics device is null"). Without it the
        // library parks Unity in the background and reattaches it next time.
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
