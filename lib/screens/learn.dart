import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/content.dart';
import '../data/models.dart';
import '../data/vocab.dart';
import '../services/mic.dart';
import '../services/sounds.dart';
import '../services/speech.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

const _vowels = ['a', 'e', 'i', 'o', 'u'];

// English phonics: short-vowel word families, and how to say each letter's sound.
const _families = {
  'a': ['cat', 'map', 'bag', 'hat', 'sad'],
  'e': ['bed', 'net', 'pen', 'red', 'leg'],
  'i': ['pig', 'sit', 'big', 'lip', 'fin'],
  'o': ['dog', 'top', 'hot', 'box', 'mop'],
  'u': ['sun', 'bus', 'cup', 'bug', 'mud'],
};
const _soundOf = {
  'a': 'ah', 'e': 'eh', 'i': 'ih', 'o': 'aw', 'u': 'uh', 'b': 'buh', 'c': 'kuh', 'd': 'duh', 'f': 'fff', 'g': 'guh', 'h': 'huh', 'j': 'juh',
  'k': 'kuh', 'l': 'lll', 'm': 'mmm', 'n': 'nnn', 'p': 'puh', 'r': 'rrr', 's': 'sss', 't': 'tuh', 'v': 'vvv', 'w': 'wuh', 'x': 'ks', 'y': 'yuh', 'z': 'zzz',
};

class LearnScreen extends StatefulWidget {
  const LearnScreen({super.key});
  @override
  State<LearnScreen> createState() => _LearnScreenState();
}

class _LearnScreenState extends State<LearnScreen> {
  int tab = 0;
  bool filipino = true;

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    return AppPage(
      title: 'Let’s Learn Our Sounds!',
      subtitle: 'Marungko Method · one letter sound at a time, then practice!',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          ScrollSeg<int>(options: const [0, 1, 2], value: tab, label: (i) => const ['Letter Sounds', 'Marungko Practice', 'English Phonics'][i], onChanged: (i) => setState(() => tab = i)),
          const SizedBox(height: 14),
          if (tab == 0) _letterSounds(app) else if (tab == 1) MarungkoPractice(lessonsDone: app.lessonsDone) else const PhonicsPractice(),
        ]),
      ),
    );
  }

  Widget _letterSounds(AppState app) {
    final done = app.lessonsDone;
    final allDone = done >= lessons.length;
    final next = lessons[allDone ? lessons.length - 1 : done];
    final practiceStory = stories.firstWhere((s) => s.focus.letter == next.letter, orElse: () => stories.first);
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      WhiteCard(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            GestureDetector(
              onTap: () => Speech.i.speak(next.letter.substring(next.letter.length - 1), lang: 'fil-PH', rate: .8),
              child: Container(width: 96, height: 96, alignment: Alignment.center, decoration: BoxDecoration(color: C.orange, borderRadius: BorderRadius.circular(26)), child: Text(next.letter, style: T.head(48, color: C.ink, weight: FontWeight.w700))),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Up next · Lesson ${next.id}', style: T.body(13, color: const Color(0xFFB4520F), weight: FontWeight.w800)),
                Text('The ${next.letter} sound', style: T.head(22, weight: FontWeight.w700)),
                Text('Tap the letter to hear it, then say it: ${next.fil} · ${next.eng}', style: T.body(13, color: C.muted)),
              ]),
            ),
          ]),
          const SizedBox(height: 12),
          Bar((done / lessons.length * 100).round()),
          const SizedBox(height: 4),
          Text('$done of ${lessons.length} letter sounds learned', style: T.body(12, color: C.muted)),
          const SizedBox(height: 12),
          Btn(allDone ? 'All sounds learned!' : 'I can say it!', icon: Icons.check, expand: true, onPressed: allDone
              ? null
              : () {
                  Sounds.i.play('good');
                  app.completeLesson();
                }),
          Btn('Practice in a story', kind: BtnKind.white, expand: true, onPressed: () => context.push('/books/${practiceStory.id}/read')),
        ]),
      ),
      const SizedBox(height: 16),
      Row(children: [
        Expanded(child: Text('All lessons', style: T.head(20))),
        Seg<bool>(options: const [true, false], value: filipino, label: (v) => v ? 'Filipino' : 'English', onChanged: (v) => setState(() => filipino = v)),
      ]),
      const SizedBox(height: 10),
      GridView.count(
        crossAxisCount: 3,
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        mainAxisSpacing: 10,
        crossAxisSpacing: 10,
        childAspectRatio: 1.05,
        children: [
          for (var i = 0; i < lessons.length; i++) _lessonTile(lessons[i], i < done ? 'done' : (i == done ? 'now' : 'locked')),
        ],
      ),
    ]);
  }

  Widget _lessonTile(Lesson l, String status) {
    final word = filipino ? l.fil : l.eng;
    return GestureDetector(
      onTap: status == 'locked' ? null : () => Speech.i.speak('${l.letter.substring(l.letter.length - 1)}. $word', lang: filipino ? 'fil-PH' : 'en-US', rate: .85),
      child: Container(
        decoration: BoxDecoration(
          color: status == 'locked' ? const Color(0xFFF6F3EC) : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: status == 'done' ? C.green : (status == 'now' ? C.orange : C.cardBorder), width: 2),
        ),
        child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
          Text(l.letter, style: T.head(28, weight: FontWeight.w700, color: status == 'locked' ? C.muted : C.navy)),
          Text(word, style: T.body(12, weight: FontWeight.w800, color: status == 'locked' ? C.muted : C.navy)),
          const SizedBox(height: 2),
          if (status == 'done') const Icon(Icons.check, size: 14, color: C.greenText),
          if (status == 'now') Text('Now', style: T.body(11, weight: FontWeight.w800, color: C.orangeDark)),
          if (status == 'locked') const Icon(Icons.lock, size: 13, color: C.muted),
        ]),
      ),
    );
  }
}

/// One word at a time: tap the parts, hear the word, then say it into the microphone.
class WordDrill extends StatefulWidget {
  final List<String> words;
  final String lang;
  final List<String> Function(String) parts;
  final String Function(String) partSound;
  final String joiner;
  const WordDrill({super.key, required this.words, required this.lang, required this.parts, required this.partSound, required this.joiner});
  @override
  State<WordDrill> createState() => _WordDrillState();
}

class _WordDrillState extends State<WordDrill> {
  late MicCheck mic = MicCheck(widget.lang)..addListener(_refresh);
  int idx = 0;
  final Set<String> stars = {};
  bool _credited = false;

  void _refresh() {
    if (!mounted) return;
    setState(() {});
    if (mic.heard.contains(normalizeWord(widget.words[idx])) && !_credited) {
      _credited = true;
      stars.add(widget.words[idx]);
      Sounds.i.play('good');
    }
  }

  @override
  void didUpdateWidget(covariant WordDrill old) {
    super.didUpdateWidget(old);
    if (old.words.join() != widget.words.join()) {
      idx = 0;
      stars.clear();
      _credited = false;
      mic.reset();
    }
  }

  @override
  void dispose() {
    mic.removeListener(_refresh);
    mic.dispose();
    super.dispose();
  }

  void pick(int i) {
    mic.reset();
    setState(() {
      idx = i;
      _credited = false;
    });
  }

  void say(String t, [double rate = .75]) => Speech.i.speak(t, lang: widget.lang, rate: rate);

  @override
  Widget build(BuildContext context) {
    final word = widget.words[idx];
    final said = mic.heard.contains(normalizeWord(word));
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      SizedBox(
        height: 46,
        child: ListView(scrollDirection: Axis.horizontal, children: [
          for (var i = 0; i < widget.words.length; i++)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: GestureDetector(
                onTap: () => pick(i),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  alignment: Alignment.center,
                  decoration: BoxDecoration(color: i == idx ? const Color(0xFFEAF3FB) : Colors.white, borderRadius: BorderRadius.circular(16), border: Border.all(color: i == idx ? C.blueBtn : C.cardBorder, width: 2)),
                  child: Row(children: [Text(widget.words[i], style: T.body(15, weight: FontWeight.w800)), if (stars.contains(widget.words[i])) const Padding(padding: EdgeInsets.only(left: 6), child: Icon(Icons.star, size: 16, color: C.yellow2))]),
                ),
              ),
            ),
        ]),
      ),
      const SizedBox(height: 12),
      WhiteCard(
        padding: const EdgeInsets.all(18),
        child: Column(children: [
          Wrap(alignment: WrapAlignment.center, crossAxisAlignment: WrapCrossAlignment.center, spacing: 8, runSpacing: 8, children: [
            for (final (i, p) in widget.parts(word).indexed) ...[
              if (i > 0) Text(widget.joiner, style: T.head(26, color: C.orange, weight: FontWeight.w700)),
              GestureDetector(
                onTap: () => say(widget.partSound(p), .7),
                child: Container(constraints: const BoxConstraints(minWidth: 62, minHeight: 62), padding: const EdgeInsets.symmetric(horizontal: 12), alignment: Alignment.center, decoration: _tileBox, child: Text(p, style: T.head(30, weight: FontWeight.w700))),
              ),
            ],
            Text('=', style: T.head(26, color: C.orange, weight: FontWeight.w700)),
            GestureDetector(onTap: () => say(word), child: Text(word, style: T.head(36, color: said ? C.green : C.navy, weight: FontWeight.w700))),
          ]),
          const SizedBox(height: 10),
          Text(said ? 'Great job! You said it!' : (mic.listening ? 'Listening… say the word now!' : 'Tap each part, blend them, then say the word.'), textAlign: TextAlign.center, style: T.body(14, color: C.muted, weight: FontWeight.w800)),
          const SizedBox(height: 10),
          MicButton(listening: mic.listening, label: 'Say it!', onPressed: () => mic.listen([word])),
          if (mic.notice.isNotEmpty) Text(mic.notice, textAlign: TextAlign.center, style: T.body(13, color: C.red, weight: FontWeight.w800)),
          Wrap(spacing: 10, children: [
            if (!said) Btn('I said it!', icon: Icons.check, kind: BtnKind.white, onPressed: () => mic.markAll([word])),
            if (said && idx < widget.words.length - 1) Btn('Next word', icon: Icons.arrow_forward, onPressed: () => pick(idx + 1)),
          ]),
          Text('${stars.length} of ${widget.words.length} words said', style: T.body(12, color: C.muted)),
        ]),
      ),
    ]);
  }
}

final _tileBox = BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18), border: Border.all(color: C.yellow, width: 3));

/// Marungko practice: the letter sounds learned so far, blended into syllables and words.
class MarungkoPractice extends StatefulWidget {
  final int lessonsDone;
  const MarungkoPractice({super.key, required this.lessonsDone});
  @override
  State<MarungkoPractice> createState() => _MarungkoPracticeState();
}

class _MarungkoPracticeState extends State<MarungkoPractice> {
  String? letter;

  @override
  Widget build(BuildContext context) {
    final open = lessons.take(widget.lessonsDone + 1).toList();
    final current = letter ?? open.last.letter;
    final lesson = lessons.firstWhere((l) => l.letter == current);
    final sound = current == 'NGng' ? 'ng' : current.substring(current.length - 1).toLowerCase();
    final isVowel = _vowels.contains(sound);
    final sylls = isVowel ? [sound] : _vowels.map((v) => '$sound$v').toList();
    final deck = decks.where((d) => d.letter == current).firstOrNull;
    final words = {...?deck?.cards.map((c) => c.fil), lesson.fil}.toList();
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Wrap(spacing: 8, runSpacing: 8, children: [
        for (final l in open)
          GestureDetector(
            onTap: () => setState(() => letter = l.letter),
            child: Container(
              width: 56, height: 52, alignment: Alignment.center,
              decoration: BoxDecoration(color: l.letter == current ? C.orange : Colors.white, borderRadius: BorderRadius.circular(16), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 6, offset: Offset(0, 3))]),
              child: Text(l.letter, style: T.head(20, weight: FontWeight.w700)),
            ),
          ),
      ]),
      const SizedBox(height: 12),
      WhiteCard(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(isVowel ? 'The vowel $current' : 'Blend $current with the vowels', style: T.head(18)),
          const SizedBox(height: 10),
          Wrap(spacing: 10, runSpacing: 10, children: [
            for (final s in sylls)
              GestureDetector(
                onTap: () => Speech.i.speak(s, lang: 'fil-PH', rate: .75),
                child: Container(constraints: const BoxConstraints(minWidth: 64, minHeight: 64), padding: const EdgeInsets.symmetric(horizontal: 14), alignment: Alignment.center, decoration: _tileBox, child: Text(s, style: T.head(30, weight: FontWeight.w700))),
              ),
          ]),
        ]),
      ),
      const SizedBox(height: 12),
      WordDrill(words: words, lang: 'fil-PH', parts: (w) => syllables(w), partSound: (p) => p, joiner: '·'),
    ]);
  }
}

/// English phonics practice: sound out short-vowel words letter by letter.
class PhonicsPractice extends StatefulWidget {
  const PhonicsPractice({super.key});
  @override
  State<PhonicsPractice> createState() => _PhonicsPracticeState();
}

class _PhonicsPracticeState extends State<PhonicsPractice> {
  String vowel = 'a';
  @override
  Widget build(BuildContext context) => Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Wrap(spacing: 8, runSpacing: 8, children: [
          for (final v in _vowels)
            GestureDetector(
              onTap: () {
                setState(() => vowel = v);
                Speech.i.speak(_soundOf[v]!, lang: 'en-US', rate: .7);
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                decoration: BoxDecoration(color: v == vowel ? C.orange : Colors.white, borderRadius: BorderRadius.circular(16), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 6, offset: Offset(0, 3))]),
                child: Text('short $v', style: T.head(16, weight: FontWeight.w700)),
              ),
            ),
        ]),
        const SizedBox(height: 12),
        WordDrill(words: _families[vowel]!, lang: 'en-US', parts: (w) => w.split(''), partSound: (p) => _soundOf[p] ?? p, joiner: '+'),
      ]);
}
