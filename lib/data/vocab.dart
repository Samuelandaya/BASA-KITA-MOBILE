import 'content.dart';
import 'models.dart';

const _vowels = 'aeiouáéíóú';

// English words don't follow simple consonant-vowel rules, so their parts are listed.
const _enParts = <String, List<String>>{
  'teacher': ['tea', 'cher'], 'together': ['to', 'geth', 'er'], 'under': ['un', 'der'], 'magic': ['ma', 'gic'],
  'sharing': ['shar', 'ing'], 'bounced': ['boun', 'ced'], 'picked': ['pick', 'ed'], 'squirrel': ['squir', 'rel'],
  'puppy': ['pup', 'py'], 'happy': ['hap', 'py'], 'apple': ['ap', 'ple'], 'rabbit': ['rab', 'bit'], 'insect': ['in', 'sect'],
  'octopus': ['oc', 'to', 'pus'], 'umbrella': ['um', 'brel', 'la'], 'little': ['lit', 'tle'], 'playtime': ['play', 'time'],
};

/// Lowercase letters and digits only, so "Ball," matches "ball".
String normalizeWord(String w) => w.toLowerCase().replaceAll(RegExp(r'[^\p{L}\p{N}]', unicode: true), '');

VocabEntry? vocabFor(String word) => vocab[normalizeWord(word)];

bool _isVowel(String c) => _vowels.contains(c);

/// Word parts for kids: Filipino uses consonant-vowel syllables ("ng" kept together);
/// English uses the list above, otherwise the whole word.
List<String> syllables(String word, {bool filipino = true}) {
  final raw = word.toLowerCase().replaceAll(RegExp(r'[^\p{L}]', unicode: true), '');
  if (!filipino) return _enParts[raw] ?? [raw];
  final w = raw.replaceAll('ng', 'ŋ');
  final parts = <String>[];
  var cur = '';
  for (var i = 0; i < w.length; i++) {
    cur += w[i];
    final vowel = _isVowel(w[i]);
    final next = i + 1 < w.length ? w[i + 1] : null;
    final afterNext = i + 2 < w.length ? w[i + 2] : null;
    if (vowel && next != null && !_isVowel(next)) {
      // keep a single consonant for the next syllable; split clusters
      if (afterNext != null && !_isVowel(afterNext)) {
        cur += next;
        i++;
      }
      parts.add(cur);
      cur = '';
    } else if (vowel && next != null && _isVowel(next)) {
      parts.add(cur);
      cur = '';
    }
  }
  if (cur.isNotEmpty) {
    if (parts.isNotEmpty && !cur.split('').any(_isVowel)) {
      parts[parts.length - 1] += cur;
    } else {
      parts.add(cur);
    }
  }
  return (parts.isEmpty ? [w] : parts).map((p) => p.replaceAll('ŋ', 'ng')).toList();
}

/// A word with where it sits in the text, so narration can highlight it.
class Token {
  final String word;
  final int start;
  final int end;
  const Token(this.word, this.start, this.end);
}

List<Token> tokenize(String text) =>
    RegExp(r'\S+').allMatches(text).map((m) => Token(m.group(0)!, m.start, m.end)).toList();
