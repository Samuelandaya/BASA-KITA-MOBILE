import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../data/vocab.dart';
import '../learner.dart';
import '../services/speech.dart';
import '../state/app_state.dart';
import '../theme.dart';
import 'common.dart';

double textSizeFor(String size) => switch (size) { 'Normal' => 24, 'Bigger' => 36, _ => 30 };

/// Text that the voice reads aloud, lighting up each word as it is spoken.
/// Tap a word to open its vocabulary card.
class ReadAloudText extends StatefulWidget {
  final String text;
  final String lang;
  final double rate;
  final bool showButton;
  final bool autoPlay;
  final bool highlight;
  final double fontSize;
  final VoidCallback? onDone;
  final bool wordsTappable;
  const ReadAloudText({
    super.key,
    required this.text,
    this.lang = 'en-US',
    this.rate = 1,
    this.showButton = true,
    this.autoPlay = false,
    this.highlight = true,
    this.fontSize = 30,
    this.onDone,
    this.wordsTappable = true,
  });
  @override
  State<ReadAloudText> createState() => ReadAloudTextState();
}

class ReadAloudTextState extends State<ReadAloudText> {
  int active = -1;
  bool playing = false;
  late List<Token> tokens = tokenize(widget.text);

  @override
  void initState() {
    super.initState();
    if (widget.autoPlay) WidgetsBinding.instance.addPostFrameCallback((_) => play());
  }

  @override
  void didUpdateWidget(covariant ReadAloudText oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.text != widget.text) {
      Speech.i.stop();
      tokens = tokenize(widget.text);
      active = -1;
      playing = false;
    }
  }

  @override
  void dispose() {
    Speech.i.stop();
    super.dispose();
  }

  void play() {
    setState(() {
      playing = true;
      active = 0;
    });
    Speech.i.speak(
      widget.text,
      lang: widget.lang,
      rate: widget.rate,
      onWord: (start, end) {
        if (!mounted) return;
        final i = tokens.indexWhere((t) => start >= t.start && start < t.end + 1);
        if (i >= 0) setState(() => active = i);
      },
      onEnd: () {
        if (!mounted) return;
        setState(() {
          playing = false;
          active = -1;
        });
        widget.onDone?.call();
      },
    );
  }

  void stop() {
    Speech.i.stop();
    setState(() {
      playing = false;
      active = -1;
    });
  }

  @override
  Widget build(BuildContext context) {
    final narration = context.select<AppState, bool>((s) => s.settings.narration);
    return Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
      Wrap(
        spacing: 2,
        runSpacing: 4,
        children: [
          for (var i = 0; i < tokens.length; i++)
            GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTap: widget.wordsTappable ? () => showVocab(context, tokens[i].word, widget.lang) : null,
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 120),
                padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                decoration: BoxDecoration(color: widget.highlight && i == active ? C.yellow : Colors.transparent, borderRadius: BorderRadius.circular(10)),
                child: Text(tokens[i].word, style: T.head(widget.fontSize, color: C.navy, weight: FontWeight.w600, height: 1.25)),
              ),
            ),
        ],
      ),
      if (widget.showButton) ...[
        const SizedBox(height: 12),
        Btn(playing ? 'Stop' : 'Read it to me', icon: playing ? Icons.stop : Icons.volume_up, kind: BtnKind.blue, onPressed: narration ? (playing ? stop : play) : null),
      ],
    ]);
  }
}

/// The vocabulary card: word parts, meaning, and the word in the other language.
Future<void> showVocab(BuildContext context, String word, String lang) {
  Speech.i.speak(normalizeWord(word), lang: lang, rate: .8);
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    builder: (_) => VocabCard(word: word, lang: lang),
  );
}

class VocabCard extends StatelessWidget {
  final String word;
  final String lang;
  const VocabCard({super.key, required this.word, required this.lang});
  @override
  Widget build(BuildContext context) {
    final clean = normalizeWord(word);
    final entry = vocabFor(clean);
    final fil = lang.startsWith('fil');
    final parts = syllables(clean, filipino: fil);
    return Container(
      margin: const EdgeInsets.all(12),
      padding: const EdgeInsets.fromLTRB(22, 14, 22, 22),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(32), border: Border.all(color: C.yellow, width: 3)),
      child: SafeArea(
        top: false,
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Container(width: 44, height: 5, decoration: BoxDecoration(color: C.cardBorder, borderRadius: BorderRadius.circular(3))),
          const SizedBox(height: 14),
          Text(clean, style: T.head(40, weight: FontWeight.w700)),
          const SizedBox(height: 12),
          Wrap(spacing: 8, runSpacing: 8, alignment: WrapAlignment.center, children: [
            for (final p in parts)
              GestureDetector(
                onTap: () => Speech.i.speak(p, lang: lang, rate: .7),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
                  decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18), border: Border.all(color: C.yellow, width: 3)),
                  child: Text(p, style: T.head(30, weight: FontWeight.w700)),
                ),
              ),
          ]),
          const SizedBox(height: 14),
          if (entry != null) ...[
            Text(entry.meaning, textAlign: TextAlign.center, style: T.body(17, height: 1.35)),
            const SizedBox(height: 8),
            Text.rich(TextSpan(children: [
              TextSpan(text: fil ? 'In English: ' : 'Sa Filipino: ', style: T.body(15, color: C.muted)),
              TextSpan(text: entry.pair, style: T.body(17, color: C.navy, weight: FontWeight.w800)),
            ])),
          ] else
            Text('Tap the parts to hear them, then say the whole word!', textAlign: TextAlign.center, style: T.body(15, color: C.muted)),
          const SizedBox(height: 16),
          Btn('Hear it again', icon: Icons.volume_up, onPressed: () => Speech.i.speak(clean, lang: lang, rate: .8)),
        ]),
      ),
    );
  }
}

/// Score after the child reads aloud: pronunciation (English) or word reading accuracy (Filipino).
({int score, int correct, int total, bool english}) scoreReading(List<String> words, Set<String> heard, double? confidence, String lang) {
  final total = words.isEmpty ? 1 : words.length;
  final correct = words.where((w) => heard.contains(normalizeWord(w))).length;
  final accuracy = correct / total;
  final english = !lang.startsWith('fil');
  final s = english && confidence != null ? accuracy * (0.6 + 0.4 * confidence) : accuracy;
  return (score: (s * 100).round(), correct: correct, total: total, english: english);
}

class ReadingScoreCard extends StatefulWidget {
  final List<String> words;
  final Set<String> heard;
  final double? confidence;
  final String lang;
  final String storyId;
  final int page; // 1-based
  const ReadingScoreCard({super.key, required this.words, required this.heard, required this.confidence, required this.lang, required this.storyId, required this.page});
  @override
  State<ReadingScoreCard> createState() => _ReadingScoreCardState();
}

class _ReadingScoreCardState extends State<ReadingScoreCard> {
  int? _recorded;

  @override
  Widget build(BuildContext context) {
    if (widget.heard.isEmpty) return const SizedBox.shrink();
    final r = scoreReading(widget.words, widget.heard, widget.confidence, widget.lang);
    if (_recorded != r.score) {
      _recorded = r.score;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) context.read<AppState>().recordScore(widget.storyId, widget.page, r.score, r.english);
      });
    }
    final missed = {for (final w in widget.words) if (!widget.heard.contains(normalizeWord(w))) normalizeWord(w)}..remove('');
    final cheer = r.score >= 90 ? 'Excellent!' : r.score >= 70 ? 'Great job!' : 'Keep practicing!';
    return WhiteCard(
      child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
        SizedBox(
          width: 76, height: 76,
          child: Stack(alignment: Alignment.center, children: [
            SizedBox(width: 76, height: 76, child: CircularProgressIndicator(value: r.score / 100, strokeWidth: 9, color: C.green, backgroundColor: const Color(0xFFD9E2EC))),
            Text('${r.score}%', style: T.head(20, weight: FontWeight.w700)),
          ]),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(r.english ? 'Pronunciation score' : 'Word reading accuracy', style: T.body(16, weight: FontWeight.w800, color: C.navy)),
            Text('$cheer ${r.correct} of ${r.total} words read correctly.', style: T.body(13, color: C.muted)),
            if (missed.isNotEmpty) ...[
              const SizedBox(height: 8),
              Wrap(spacing: 6, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
                Text('Practice:', style: T.body(13, color: C.muted)),
                for (final w in missed)
                  GestureDetector(
                    onTap: () => Speech.i.speak(w, lang: widget.lang, rate: .75),
                    child: Container(padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5), decoration: BoxDecoration(color: C.cream2, borderRadius: BorderRadius.circular(16)), child: Text(w, style: T.body(14, weight: FontWeight.w800))),
                  ),
              ]),
            ],
          ]),
        ),
      ]),
    );
  }
}

/// Learner level with the steps to the next level.
class LevelCard extends StatelessWidget {
  const LevelCard({super.key});
  @override
  Widget build(BuildContext context) {
    final level = learnerLevel(context.watch<AppState>().account);
    return WhiteCard(
      child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Container(
          width: 68, height: 68,
          decoration: BoxDecoration(color: levelBg(level.name), shape: BoxShape.circle, border: Border.all(color: C.yellow, width: 3)),
          child: Icon(Icons.workspace_premium, size: 34, color: levelFg(level.name)),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('My reading level', style: T.body(13, color: C.muted, weight: FontWeight.w800)),
            Text('${level.name} Reader', style: T.head(24, weight: FontWeight.w700)),
            const SizedBox(height: 6),
            Wrap(spacing: 6, children: [
              for (var i = 0; i < levelNames.length; i++)
                Tag(levelNames[i], bg: i == level.index ? C.orange : (i < level.index ? const Color(0xFFDDF3E6) : const Color(0xFFF1EDE4)), fg: i == level.index ? C.ink : (i < level.index ? const Color(0xFF1D6B42) : C.muted)),
            ]),
            const SizedBox(height: 10),
            if (level.next != null) ...[
              Bar(level.toNext, color: C.green),
              const SizedBox(height: 6),
              Text('To reach ${level.next}: ${level.goal.toLowerCase()}.', style: T.body(13, color: C.muted)),
            ] else
              Text('You’re a confident reader. ${level.goal}', style: T.body(13, color: C.muted)),
          ]),
        ),
      ]),
    );
  }
}

/// Big round microphone button.
class MicButton extends StatelessWidget {
  final bool listening;
  final VoidCallback onPressed;
  final String label;
  const MicButton({super.key, required this.listening, required this.onPressed, this.label = 'Tap to read aloud'});
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 4),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: onPressed,
            borderRadius: BorderRadius.circular(40),
            child: Container(
              constraints: const BoxConstraints(minHeight: 62),
              padding: const EdgeInsets.symmetric(horizontal: 26, vertical: 12),
              decoration: BoxDecoration(
                color: listening ? C.red : C.blueBtn,
                borderRadius: BorderRadius.circular(40),
                boxShadow: [listening ? const BoxShadow(color: Color(0x2EC7302A), spreadRadius: 8) : const BoxShadow(color: C.blueDark, offset: Offset(0, 4))],
              ),
              child: Row(mainAxisSize: MainAxisSize.min, children: [
                const Icon(Icons.mic, color: Colors.white, size: 26),
                const SizedBox(width: 10),
                Flexible(child: Text(listening ? 'Listening… tap to stop' : label, style: T.body(16, color: Colors.white, weight: FontWeight.w800))),
              ]),
            ),
          ),
        ),
      );
}

/// A paragraph of tappable words, with one word lit up while it is being read.
class WordWrap extends StatelessWidget {
  final String text;
  final String lang;
  final int active;
  final double fontSize;
  final bool highlight;
  final bool tappable;
  final String? focusWord;
  final Set<String> said;
  const WordWrap({super.key, required this.text, required this.lang, this.active = -1, this.fontSize = 30, this.highlight = true, this.tappable = true, this.focusWord, this.said = const {}});

  @override
  Widget build(BuildContext context) {
    final tokens = tokenize(text);
    return Wrap(
      spacing: 2,
      runSpacing: 4,
      children: [
        for (var i = 0; i < tokens.length; i++)
          GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: tappable ? () => showVocab(context, tokens[i].word, lang) : null,
            child: Builder(builder: (_) {
              final n = normalizeWord(tokens[i].word);
              final isSaid = said.contains(n);
              final isFocus = focusWord != null && n == normalizeWord(focusWord!);
              final bg = isSaid ? const Color(0xFFDDF3E6) : (highlight && i == active ? C.yellow : (isFocus ? C.yellow : Colors.transparent));
              return AnimatedContainer(
                duration: const Duration(milliseconds: 120),
                padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(10)),
                child: Text(tokens[i].word, style: T.head(fontSize, color: isSaid ? C.greenText : C.navy, weight: FontWeight.w600, height: 1.25)),
              );
            }),
          ),
      ],
    );
  }
}
