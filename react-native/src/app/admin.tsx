import { Text } from 'react-native';
import { useApp } from '../state/AppState';
import { Btn, Card, Page } from '../ui/kit';
import { C, T } from '../ui/theme';

/** Teacher view. The full dashboard (learners, books, reports) comes in a later stage of the port. */
export default function Admin() {
  const app = useApp();
  return (
    <Page>
      <Text style={T.head(26, C.navy, 'Bold')}>Hello, Teacher {app.displayName}!</Text>
      <Card>
        <Text style={T.body(15, C.muted)}>
          The teacher dashboard is not in the React Native app yet. It is ready in the website and the Kotlin app,
          and will be moved here in a later stage.
        </Text>
      </Card>
      <Btn label="Log out" kind="white" icon="log-out-outline" onPress={app.logout} />
    </Page>
  );
}
