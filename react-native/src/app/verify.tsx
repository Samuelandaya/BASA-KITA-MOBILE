import { useState } from 'react';
import { Text } from 'react-native';
import { resendCode, verifyEmail } from '../shared/api/auth.js';
import { useApp } from '../state/AppState';
import type { User } from '../state/model';
import { ErrorText, Field } from '../ui/Field';
import { Btn, Card, Page } from '../ui/kit';
import { C, T } from '../ui/theme';

/** Email code step for a new account. Demo: any 6 digits work. */
export default function Verify() {
  const app = useApp();
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [note, setNote] = useState('');

  const submit = async () => {
    if (!/^\d{6}$/.test(code)) return setError('Please type the 6-digit code from your email.');
    try {
      app.login((await verifyEmail(app.user, code)) as User);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    }
  };

  return (
    <Page>
      <Text style={T.head(26, C.navy, 'Bold')}>Check your email</Text>
      <Text style={T.body(15, C.muted)}>We sent a 6-digit code to {app.user?.email}. (Demo: any 6 digits work.)</Text>
      <Card>
        <Field label="Code" value={code} onChangeText={setCode} keyboardType="number-pad" maxLength={6} placeholder="123456" />
        <ErrorText text={error} />
        {note ? <Text style={T.body(14, C.greenText, 'Bold')}>{note}</Text> : null}
        <Btn label="Verify" onPress={submit} />
        <Btn label="Send a new code" kind="white" onPress={async () => { await resendCode(app.user?.email ?? ''); setNote('A new code is on its way.'); }} />
      </Card>
      <Btn label="Use a different account" kind="white" onPress={app.logout} />
    </Page>
  );
}
