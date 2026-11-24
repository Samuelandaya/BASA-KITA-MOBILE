import { router } from 'expo-router';
import { useState } from 'react';
import { Text } from 'react-native';
import { useApp } from '../state/AppState';
import { ErrorText, Field } from '../ui/Field';
import { Btn, Card, Page } from '../ui/kit';
import { C, T } from '../ui/theme';

/** Edit Profile (Figure 5): change the name or email, then Update & Save. The backend will store it later. */
export default function EditProfile() {
  const app = useApp();
  const [name, setName] = useState(`${app.user?.firstName ?? ''} ${app.user?.lastName ?? ''}`.trim());
  const [email, setEmail] = useState(app.user?.email ?? '');
  const [error, setError] = useState('');

  const save = () => {
    if (!name.trim()) return setError('Please type your name.');
    if (!/^\S+@\S+\.\S+$/.test(email.trim())) return setError('Please enter a valid email address.');
    const [firstName, ...rest] = name.trim().split(/\s+/);
    app.updateUser({ firstName, lastName: rest.join(' '), email: email.trim().toLowerCase() });
    router.back();
  };

  return (
    <Page title="Edit profile">
      <Text style={T.body(15, C.muted)}>Change your name or email, then save.</Text>
      <Card>
        <Field label="Your name" value={name} onChangeText={setName} autoComplete="name" />
        <Field label="Email" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />
        <ErrorText text={error} />
        <Btn label="Save changes" icon="checkmark" onPress={save} />
      </Card>
    </Page>
  );
}
