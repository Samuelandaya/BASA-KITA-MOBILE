import 'package:flutter/foundation.dart';
import 'package:speech_to_text/speech_to_text.dart';

import '../data/vocab.dart';

/// Listens through the microphone and reports which target words were heard,
/// with how clearly the phone understood them (used for the pronunciation score).
class MicCheck extends ChangeNotifier {
  MicCheck(this.lang);
  final String lang; // en-US | fil-PH

  final SpeechToText _stt = SpeechToText();
  bool _inited = false;
  bool _available = false;
  bool listening = false;
  Set<String> heard = {};
  String notice = '';
  double? confidence;

  Future<bool> _init() async {
    if (_inited) return _available;
    _inited = true;
    try {
      _available = await _stt.initialize(
        onStatus: (s) {
          if (s == 'done' || s == 'notListening') {
            listening = false;
            notifyListeners();
          }
        },
        onError: (e) {
          listening = false;
          notice = e.errorMsg == 'error_permission' ? 'Please allow the microphone so BASA-KITA can hear you.' : 'I couldn’t hear you. Let’s try again!';
          notifyListeners();
        },
      );
    } catch (_) {
      _available = false;
    }
    return _available;
  }

  void reset() {
    _stt.cancel();
    heard = {};
    notice = '';
    confidence = null;
    listening = false;
    notifyListeners();
  }

  Future<void> listen(List<String> targetWords) async {
    if (listening) {
      await _stt.stop();
      listening = false;
      notifyListeners();
      return;
    }
    if (!await _init()) {
      notice = 'This phone can’t listen yet. Say it to a grown-up and tap “I said it!”.';
      notifyListeners();
      return;
    }
    final targets = targetWords.map(normalizeWord).toList();
    notice = '';
    listening = true;
    notifyListeners();
    await _stt.listen(
      listenOptions: SpeechListenOptions(partialResults: true, cancelOnError: true, localeId: lang.replaceAll('-', '_')),
      onResult: (r) {
        final said = r.recognizedWords.split(RegExp(r'\s+')).map(normalizeWord).toSet();
        heard = targets.where(said.contains).toSet();
        if (r.finalResult && r.confidence > 0) confidence = r.confidence;
        notifyListeners();
      },
    );
  }

  /// For grown-ups on phones without speech recognition: mark the words as read.
  void markAll(List<String> targetWords) {
    _stt.cancel();
    listening = false;
    heard = targetWords.map(normalizeWord).toSet();
    notifyListeners();
  }

  @override
  void dispose() {
    _stt.cancel();
    super.dispose();
  }
}
