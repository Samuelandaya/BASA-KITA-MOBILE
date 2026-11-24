import { router } from 'expo-router';
import { useState } from 'react';
import { Text } from 'react-native';
import { signIn } from '../../shared/api/auth.js';
import { useApp } from '../../state/AppState';
import type { User } from '../../state/model';
import { ErrorText, Field } from '../../ui/Field';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

export default function Login() {
  const app = useApp();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setError('');
    setBusy(true);
    try {
      app.login((await signIn(email, password)) as User);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Page title="Log in">
      <Text style={[T.head(26, C.navy, 'Bold'), { textAlign: 'center' }]}>Welcome back!</Text>
      <Text style={[T.body(15, C.muted), { textAlign: 'center' }]}>Sign in to start reading.</Text>
      <Card>
        <Field label="Email" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" autoComplete="email" placeholder="you@example.com" />
        <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry autoComplete="password" placeholder="Your password" />
        <Text accessibilityRole="link" onPress={() => router.push('/forgot')} style={[T.body(14, C.blueBtn, 'ExtraBold'), { alignSelf: 'flex-end' }]}>
          Forgot password?
        </Text>
        <ErrorText text={error} />
        <Btn label={busy ? 'Logging in…' : 'Log in'} onPress={submit} disabled={busy} kind="blue" />
      </Card>
      <Text style={[T.body(14, C.muted), { textAlign: 'center' }]}>Don’t have an account yet?</Text>
      <Btn label="Create an account" kind="white" onPress={() => router.replace('/signup')} />
    </Page>
  );
}
