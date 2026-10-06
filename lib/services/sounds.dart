import 'dart:math' as math;
import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/foundation.dart';

/// Sound effects and soft background music, synthesized in code so the app needs no audio files.
/// Both follow the Settings: Sound effects, Background music and Volume.
class Sounds {
  Sounds._();
  static final Sounds i = Sounds._();

  static const _rate = 22050;
  final AudioPlayer _fx = AudioPlayer();
  final AudioPlayer _music = AudioPlayer();
  double _volume = 0.7;
  bool _effects = true;
  bool _musicOn = false;
  bool _musicPlaying = false;
  Uint8List? _musicBytes;
  final Map<String, Uint8List> _cache = {};

  void configure({required int volume, required bool effects, required bool music}) {
    _volume = (volume / 100).clamp(0.0, 1.0);
    _effects = effects;
    _music.setVolume(_volume * 0.35).catchError((_) {});
    if (music != _musicOn) {
      _musicOn = music;
      _musicOn ? _startMusic() : _stopMusic();
    }
  }

  /// kinds: good (right answer, word said), star (story finished), oops (try again), tap
  Future<void> play(String kind) async {
    if (!_effects || _volume == 0) return;
    final bytes = _cache.putIfAbsent(kind, () => _effect(kind));
    try {
      await _fx.setVolume(_volume);
      await _fx.play(BytesSource(bytes));
    } catch (e) {
      debugPrint('sound failed: $e');
    }
  }

  Future<void> _startMusic() async {
    if (_musicPlaying) return;
    _musicPlaying = true;
    try {
      _musicBytes ??= _musicLoop();
      await _music.setReleaseMode(ReleaseMode.loop);
      await _music.setVolume(_volume * 0.35);
      await _music.play(BytesSource(_musicBytes!));
    } catch (e) {
      _musicPlaying = false;
      debugPrint('music failed: $e');
    }
  }

  Future<void> _stopMusic() async {
    _musicPlaying = false;
    try {
      await _music.stop();
    } catch (_) {}
  }

  // --- synthesis
  Uint8List _effect(String kind) {
    final notes = switch (kind) {
      'good' => [_N(660, 0, .2), _N(880, .12, .32)],
      'star' => [for (var i = 0; i < 4; i++) _N([523.0, 659.0, 784.0, 1047.0][i], i * .11, .4)],
      'oops' => [_N(330, 0, .2, triangle: true), _N(262, .14, .32, triangle: true)],
      _ => [_N(520, 0, .12)],
    };
    return _wav(_mix(notes, 1.0, 0.5));
  }

  Uint8List _musicLoop() {
    const scale = [262.0, 294.0, 330.0, 392.0, 440.0, 523.0, 587.0];
    final notes = <_N>[];
    for (var i = 0; i < 18; i++) {
      final f = scale[(i * 3 + (i % 3)) % scale.length];
      notes.add(_N(f, i * .9, 1.6));
      if (i % 4 == 0) notes.add(_N(f / 2, i * .9, 2.4, triangle: true, gain: .7));
    }
    return _wav(_mix(notes, 18 * .9 + .2, 0.35), loopable: true);
  }

  List<double> _mix(List<_N> notes, double minSeconds, double master) {
    final end = notes.fold<double>(minSeconds, (m, n) => math.max(m, n.start + n.dur + .05));
    final s = List<double>.filled((end * _rate).ceil(), 0);
    for (final n in notes) {
      final from = (n.start * _rate).floor();
      final len = (n.dur * _rate).floor();
      for (var k = 0; k < len && from + k < s.length; k++) {
        final t = k / _rate;
        final env = math.min(1.0, t / .02) * math.exp(-3.2 * t / n.dur);
        final ph = 2 * math.pi * n.freq * t;
        final wave = n.triangle ? (2 / math.pi) * math.asin(math.sin(ph)) : math.sin(ph);
        s[from + k] += wave * env * n.gain * master * .5;
      }
    }
    return s;
  }

  Uint8List _wav(List<double> samples, {bool loopable = false}) {
    final data = ByteData(44 + samples.length * 2);
    void str(int o, String v) {
      for (var i = 0; i < v.length; i++) {
        data.setUint8(o + i, v.codeUnitAt(i));
      }
    }

    str(0, 'RIFF');
    data.setUint32(4, 36 + samples.length * 2, Endian.little);
    str(8, 'WAVEfmt ');
    data.setUint32(16, 16, Endian.little);
    data.setUint16(20, 1, Endian.little);
    data.setUint16(22, 1, Endian.little);
    data.setUint32(24, _rate, Endian.little);
    data.setUint32(28, _rate * 2, Endian.little);
    data.setUint16(32, 2, Endian.little);
    data.setUint16(34, 16, Endian.little);
    str(36, 'data');
    data.setUint32(40, samples.length * 2, Endian.little);
    for (var i = 0; i < samples.length; i++) {
      data.setInt16(44 + i * 2, (samples[i].clamp(-1.0, 1.0) * 32767).round(), Endian.little);
    }
    return data.buffer.asUint8List();
  }
}

class _N {
  final double freq;
  final double start;
  final double dur;
  final bool triangle;
  final double gain;
  _N(this.freq, this.start, this.dur, {this.triangle = false, this.gain = 1});
}
