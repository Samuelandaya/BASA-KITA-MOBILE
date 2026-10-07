import { useState } from 'react';
import { signUp } from '../../shared/api/auth.js';
import { useApp } from '../../state/AppState';
import type { User } from '../../state/model';
import { ErrorText, Field } from '../../ui/Field';
import { Btn, Card, Page } from '../../ui/kit';

export default function SignUp() {
  const app = useApp();
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    if (!name.trim() || !username.trim() || !email.trim() || !password) return setError('Please fill in every box.');
    if (!/^\S+@\S+\.\S+$/.test(email.trim())) return setError('Please enter a valid email address.');
    if (password.length < 8) return setError('Your password needs at least 8 characters.');
    setError('');
    setBusy(true);
    try {
      app.login((await signUp({ name, username, email, password })) as User);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Page title="Create an account">
      <Card>
        <Field label="Your name" value={name} onChangeText={setName} autoComplete="name" placeholder="Juan Dela Cruz" />
        <Field label="Username" value={username} onChangeText={setUsername} autoCapitalize="none" placeholder="juan" />
        <Field label="Email" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" placeholder="you@example.com" />
        <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry placeholder="At least 8 characters" />
        <ErrorText text={error} />
        <Btn label={busy ? 'Creating…' : 'Create account'} onPress={submit} disabled={busy} />
      </Card>
    </Page>
  );
}
