import { useState } from 'react';
import { signUp } from '../../shared/api/auth.js';
import { useApp } from '../../state/AppState';
import type { User } from '../../state/model';
import { ErrorText, Field } from '../../ui/Field';
import { router } from 'expo-router';
import { Text } from 'react-native';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

export default function SignUp() {
  const app = useApp();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    if (!name.trim() || !email.trim() || !password) return setError('Please fill in every box.');
    if (!/^\S+@\S+\.\S+$/.test(email.trim())) return setError('Please enter a valid email address.');
    if (password.length < 8) return setError('Your password needs at least 8 characters.');
    setError('');
    setBusy(true);
    try {
      app.login((await signUp({ name, username: email.trim().split('@')[0], email, password })) as User);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Page title="Create an account">
      <Text style={T.body(15, C.muted)}>A grown-up can help you fill this in.</Text>
      <Card>
        <Field label="Your name" value={name} onChangeText={setName} autoComplete="name" placeholder="Juan Dela Cruz" />
        <Field label="Email" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" placeholder="you@example.com" />
        <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry placeholder="At least 8 characters" />
        <ErrorText text={error} />
        <Btn label={busy ? 'Creating…' : 'Create account'} onPress={submit} disabled={busy} />
      </Card>
      <Text style={[T.body(14, C.muted), { textAlign: 'center' }]}>Already have an account?</Text>
      <Btn label="Log in" kind="white" onPress={() => router.replace('/login')} />
    </Page>
  );
}
