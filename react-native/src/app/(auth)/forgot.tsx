import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { requestPasswordReset, resetPassword } from '../../shared/api/auth.js';
import { ErrorText, Field } from '../../ui/Field';
import { Btn, Card, Page } from '../../ui/kit';
import { C, T } from '../../ui/theme';

type Step = 'email' | 'sent' | 'password' | 'done';

/**
 * Forgot password (Figure 4): enter the registered email, the reset link is sent, the link opens
 * the new-password form, and the password is updated. Then back to log in.
 * The backend sends the real email; until then "Open the reset link" stands in for tapping it.
 */
export default function Forgot() {
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [pw1, setPw1] = useState('');
  const [pw2, setPw2] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const send = async () => {
    if (!/^\S+@\S+\.\S+$/.test(email.trim())) return setError('Please enter the email you used to sign up.');
    setError('');
    setBusy(true);
    try {
      await requestPasswordReset(email.trim());
      setStep('sent');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  const update = async () => {
    if (pw1.length < 8) return setError('Your new password needs at least 8 characters.');
    if (pw1 !== pw2) return setError('The two passwords are not the same.');
    setError('');
    setBusy(true);
    try {
      await resetPassword('', pw1);
      setStep('done');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  if (step === 'done') {
    return (
      <Page>
        <View style={st.center}>
          <Ionicons name="checkmark-circle" size={96} color={C.green} />
          <Text style={[T.head(28, C.navy, 'Bold'), st.text]}>Password Reset Successful!</Text>
          <Text style={[T.body(16, C.muted), st.text]}>You can now log in with your new password.</Text>
        </View>
        <Btn label="Back to log in" big onPress={() => router.replace('/login')} />
      </Page>
    );
  }

  return (
    <Page title={step === 'password' ? 'New password' : 'Forgot password'}>
      {step === 'email' ? (
        <Card>
          <Text style={T.head(24, C.navy, 'Bold')}>Reset your password</Text>
          <Text style={T.body(15, C.muted)}>Type the email you used to sign up. We will send you a reset link.</Text>
          <Field label="Registered email" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" placeholder="name@email.com" />
          <ErrorText text={error} />
          <Btn label={busy ? 'Sending…' : 'Send reset link'} onPress={send} disabled={busy} />
        </Card>
      ) : step === 'sent' ? (
        <Card>
          <Ionicons name="mail-open-outline" size={56} color={C.orange} />
          <Text style={T.head(24, C.navy, 'Bold')}>Link sent!</Text>
          <Text style={T.body(15, C.muted)}>We sent a password reset link to {email.trim()}. Open it to make a new password.</Text>
          <Btn label="Open the reset link" kind="blue" onPress={() => setStep('password')} />
        </Card>
      ) : (
        <Card>
          <Text style={T.head(24, C.navy, 'Bold')}>Make a new password</Text>
          <Text style={T.body(15, C.muted)}>For {email.trim()}</Text>
          <Field label="New password" value={pw1} onChangeText={setPw1} secureTextEntry placeholder="At least 8 characters" />
          <Field label="Type it again" value={pw2} onChangeText={setPw2} secureTextEntry placeholder="The same password" />
          <ErrorText text={error} />
          <Btn label={busy ? 'Updating…' : 'Update password'} onPress={update} disabled={busy} />
        </Card>
      )}
    </Page>
  );
}

const st = StyleSheet.create({
  center: { alignItems: 'center', gap: 12, paddingVertical: 32 },
  text: { textAlign: 'center' },
});
