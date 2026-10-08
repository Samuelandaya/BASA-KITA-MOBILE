import { StyleSheet, Text, TextInput, View, type TextInputProps } from 'react-native';
import { C, T } from './theme';

/** Labelled text box used on the sign-in screens. */
export function Field({ label, ...props }: TextInputProps & { label: string }) {
  return (
    <View style={{ gap: 6 }}>
      <Text style={T.body(14, C.navy, 'ExtraBold')}>{label}</Text>
      <TextInput placeholderTextColor="#9AA8B5" style={[T.body(16), st.input]} accessibilityLabel={label} {...props} />
    </View>
  );
}

export function ErrorText({ text }: { text: string }) {
  return text ? <Text accessibilityRole="alert" style={T.body(14, C.red, 'Bold')}>{text}</Text> : null;
}

const st = StyleSheet.create({
  input: { backgroundColor: C.white, borderRadius: 16, borderWidth: 1.5, borderColor: C.cardBorder, paddingHorizontal: 16, paddingVertical: 12 },
});
