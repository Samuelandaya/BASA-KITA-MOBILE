import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_tts/flutter_tts.dart';

import '../data/vocab.dart';

/// Reading voices. The phone supplies the real voices; each choice picks the most natural
/// voice installed for the language and shapes it with its own pitch and pace.
class VoiceProfile {
  final String id;
  final String name;
  final String blurb;
  final String who; // female | male
  final int rank;
  final double pitch;
  final double rate;
  final List<String> prefer;
  final String sample;
  final String sampleFil;
  const VoiceProfile(this.id, this.name, this.blurb, this.who, this.rank, this.pitch, this.rate, this.prefer, this.sample, this.sampleFil);
}

const voiceProfiles = <VoiceProfile>[
  VoiceProfile('ana', 'Teacher Ana', 'Warm and friendly woman', 'female', 0, 1.0, 1.0, [], 'Hi! I’m Teacher Ana. Let’s read a story together!', 'Kumusta! Ako si Teacher Ana. Magbasa tayo ng kuwento!'),
  VoiceProfile('ben', 'Teacher Ben', 'Clear and calm man', 'male', 0, 0.97, 1.0, [], 'Hello! I’m Teacher Ben. Are you ready to read?', 'Kumusta! Ako si Teacher Ben. Handa ka na bang magbasa?'),
  VoiceProfile('maya', 'Teen Maya', 'Teenage girl, upbeat and casual', 'female', 0, 1.08, 1.05, ['jenny', 'michelle', 'aria', 'ava', 'emma', 'samantha', 'sonia', 'libby'], 'Hey! I’m Maya. Want to read this one with me? It’s going to be fun!', 'Hi! Ako si Maya. Basahin natin ito nang sabay, tiyak na masaya!'),
  VoiceProfile('jay', 'Teen Jay', 'Teenage boy, easygoing', 'male', 0, 1.1, 1.05, ['eric', 'ryan', 'davis', 'jason', 'tony', 'guy', 'alex'], 'Hey, I’m Jay. Let’s read this together, it’s a good one!', 'Uy, ako si Jay. Basahin natin ito nang magkasama, maganda ito!'),
  VoiceProfile('mia', 'Little Mia', 'Bright and cheerful girl', 'female', 0, 1.15, 1.03, ['ana', 'jenny', 'aria'], 'Yay! I’m Mia. Reading is so much fun!', 'Yehey! Ako si Mia. Masaya ang magbasa!'),
  VoiceProfile('lola', 'Lola Rosa', 'Slow and gentle storyteller', 'female', 2, 0.93, 0.9, [], 'Come close, little one. Let me tell you a story.', 'Halika, apo. Magkukuwento si Lola.'),
];

final _female = RegExp(r'\b(female|woman|aria|jenny|samantha|zira|ava|ana|blessica|clara|leah|allison|susan|karen|moana|rosa|emma|michelle|sara|serena|victoria|fiona|joanna|libby|sonia|natasha|elsa)\b', caseSensitive: false);
final _male = RegExp(r'\b(male|man|guy|davis|tony|alex|daniel|fred|ryan|eric|christopher|andrew|brandon|roger|david|mark|angelo|james|thomas|george|jason|brian)\b', caseSensitive: false);

String _gender(String name) => _female.hasMatch(name) ? 'female' : (_male.hasMatch(name) ? 'male' : 'unknown');

/// Higher means a more natural sounding voice.
int _quality(Map<String, String> v) {
  final n = (v['name'] ?? '').toLowerCase();
  var q = 0;
  if (RegExp(r'natural|neural|online|network').hasMatch(n)) q += 100;
  if (RegExp(r'premium|enhanced|siri').hasMatch(n)) q += 60;
  if (n.contains('google')) q += 40;
  if (RegExp(r'espeak|compact').hasMatch(n)) q -= 100;
  return q;
}

bool isNaturalVoice(Map<String, String>? v) => v != null && _quality(v) >= 60;

/// Text to speech with word-by-word progress.
class Speech {
  Speech._();
  static final Speech i = Speech._();

  final FlutterTts _tts = FlutterTts();
  bool _ready = false;
  List<Map<String, String>> voices = [];
  String voiceId = 'ana';
  double volume = 1;

  void Function(int start, int end)? _onWord;
  VoidCallback? _onEnd;
  Timer? _fallback;
  bool _gotWord = false;

  Future<void> init() async {
    if (_ready) return;
    _ready = true;
    try {
      _tts.setProgressHandler((text, start, end, word) {
        _gotWord = true;
        _fallback?.cancel();
        _onWord?.call(start, end);
      });
      _tts.setCompletionHandler(_finish);
      _tts.setCancelHandler(() {});
      _tts.setErrorHandler((_) => _finish());
      final raw = await _tts.getVoices;
      voices = (raw as List? ?? [])
          .whereType<Map>()
          .map((m) => m.map((k, v) => MapEntry(k.toString(), v.toString())))
          .toList();
    } catch (e) {
      debugPrint('Text to speech is not available: $e');
    }
  }

  void _finish() {
    _fallback?.cancel();
    final cb = _onEnd;
    _onEnd = null;
    _onWord = null;
    cb?.call();
  }

  VoiceProfile get profile => voiceProfiles.firstWhere((p) => p.id == voiceId, orElse: () => voiceProfiles.first);

  /// The most natural installed voice for a language and a voice choice.
  Map<String, String>? pickVoice(String lang, [String? id]) {
    final p = voiceProfiles.firstWhere((x) => x.id == (id ?? voiceId), orElse: () => voiceProfiles.first);
    final code = lang.toLowerCase().replaceAll('_', '-');
    bool locale(Map<String, String> v) => (v['locale'] ?? '').toLowerCase().replaceAll('_', '-') == code;
    var pool = voices.where(locale).toList();
    if (pool.isEmpty) pool = voices.where((v) => (v['locale'] ?? '').toLowerCase().startsWith(code.substring(0, 2))).toList();
    if (pool.isEmpty) return null;
    pool.sort((a, b) => _quality(b).compareTo(_quality(a)));
    final wanted = pool.where((v) => _gender(v['name'] ?? '') == p.who).toList();
    final from = wanted.isEmpty ? pool : wanted;
    final top = _quality(from.first);
    final tier = from.where((v) => _quality(v) >= top - 40).toList();
    final liked = <Map<String, String>>[];
    for (final n in p.prefer) {
      final re = RegExp('\\b$n\\b', caseSensitive: false);
      for (final v in tier) {
        if (re.hasMatch(v['name'] ?? '') && !liked.contains(v)) liked.add(v);
      }
    }
    final list = liked.isEmpty ? tier : liked;
    return list[p.rank % list.length];
  }

  /// Reads [text] aloud. [onWord] gets the start and end position of each word as it is spoken.
  Future<void> speak(String text, {String lang = 'en-US', double rate = 1, void Function(int start, int end)? onWord, VoidCallback? onEnd, String? voiceId}) async {
    await init();
    await stop();
    final p = voiceProfiles.firstWhere((x) => x.id == (voiceId ?? this.voiceId), orElse: () => voiceProfiles.first);
    _onWord = onWord;
    _onEnd = onEnd;
    _gotWord = false;
    try {
      await _tts.setLanguage(lang);
      final v = pickVoice(lang, p.id);
      if (v != null) await _tts.setVoice({'name': v['name']!, 'locale': v['locale']!});
      await _tts.setSpeechRate((0.5 * rate * p.rate).clamp(0.25, 0.9));
      await _tts.setPitch(p.pitch.clamp(0.5, 2.0));
      await _tts.setVolume(volume);
      // Some voices send no word events, so estimate the timing from the text length.
      if (onWord != null) {
        _fallback = Timer(const Duration(milliseconds: 700), () {
          if (_gotWord) return;
          final perChar = 75 / (rate * p.rate);
          for (final t in tokenize(text)) {
            final delay = (t.start * perChar - 700).clamp(0, 1e9).round();
            Timer(Duration(milliseconds: delay), () {
              if (!_gotWord && _onWord != null) _onWord!(t.start, t.end);
            });
          }
        });
      }
      await _tts.speak(text);
    } catch (e) {
      debugPrint('speak failed: $e');
      _finish();
    }
  }

  Future<void> stop() async {
    _fallback?.cancel();
    _onWord = null;
    _onEnd = null;
    try {
      await _tts.stop();
    } catch (_) {}
  }
}
