import Ionicons from '@expo/vector-icons/Ionicons';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { resendCode, verifyEmail } from '../shared/api/auth.js';
import { useApp } from '../state/AppState';
import type { User } from '../state/model';
import { ErrorText, Field } from '../ui/Field';
import { Btn, Card, Page } from '../ui/kit';
import { C, T } from '../ui/theme';

/** Email code step for a new account (Figure 4), then "Account Created Successfully". */
export default function Verify() {
  const app = useApp();
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [note, setNote] = useState('');
  const [created, setCreated] = useState<User | null>(null);

  const submit = async () => {
    setNote('');
    if (!/^\d{6}$/.test(code)) return setError('That code is not right. Please type the 6-digit code from your email.');
    try {
      setError('');
      setCreated((await verifyEmail(app.user, code)) as User);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'That code is not right. Please try again.');
    }
  };

  if (created) {
    return (
      <Page>
        <View style={st.center}>
          <Ionicons name="checkmark-circle" size={96} color={C.green} />
          <Text style={[T.head(28, C.navy, 'Bold'), st.text]}>Account Created Successfully!</Text>
          <Text style={[T.body(16, C.muted), st.text]}>Welcome to BASA-KITA, {created.firstName}! Let’s start reading.</Text>
        </View>
        <Btn label="Start reading" icon="sparkles" big onPress={() => app.login(created)} />
      </Page>
    );
  }

  return (
    <Page>
      <Text style={T.head(26, C.navy, 'Bold')}>Check your email</Text>
      <Text style={T.body(15, C.muted)}>We sent a 6-digit code to {app.user?.email}.</Text>
      <Card>
        <Field label="Verification code" value={code} onChangeText={setCode} keyboardType="number-pad" maxLength={6} placeholder="6 digits" />
        <ErrorText text={error} />
        {note ? <Text style={T.body(14, C.greenText, 'Bold')}>{note}</Text> : null}
        <Btn label="Verify" kind="blue" onPress={submit} />
        <Btn label="Send a new code" kind="white" onPress={async () => { setError(''); await resendCode(app.user?.email ?? ''); setNote('A new code is on its way.'); }} />
      </Card>
      <Btn label="Use a different account" kind="white" onPress={app.logout} />
    </Page>
  );
}

const st = StyleSheet.create({
  center: { alignItems: 'center', gap: 12, paddingVertical: 32 },
  text: { textAlign: 'center' },
});
