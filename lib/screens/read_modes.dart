import 'dart:async';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/models.dart';
import '../data/vocab.dart';
import '../services/mic.dart';
import '../services/sounds.dart';
import '../services/speech.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

Widget _missing(BuildContext context) => AppPage(back: true, title: 'Story not found', child: Btn('Back to Storybooks', onPressed: () => context.go('/books')));

Widget _pageChips(Story story, int page) => Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
      Tag('${story.language} · ${story.shownLevel}', bg: const Color(0xFFFFF3D6), fg: C.navy),
      Tag('Page ${page + 1} of ${story.pages.length}', bg: const Color(0xFFFFF3D6), fg: C.navy),
    ]);

// ------------------------------------------------------------------ Listen
class ListenScreen extends StatefulWidget {
  final String id;
  const ListenScreen({super.key, required this.id});
  @override
  State<ListenScreen> createState() => _ListenScreenState();
}

class _ListenScreenState extends State<ListenScreen> {
  int page = 0;
  bool playing = false;
  int active = -1;
  late final Story? story = storyById(widget.id);

  @override
  void initState() {
    super.initState();
    if (story != null) WidgetsBinding.instance.addPostFrameCallback((_) => context.read<AppState>().openStory(story!.id));
  }

  @override
  void dispose() {
    Speech.i.stop();
    super.dispose();
  }

  void readPage(int index) {
    final s = story!;
    final app = context.read<AppState>();
    final tokens = tokenize(s.pages[index].text);
    setState(() {
      page = index;
      active = 0;
    });
    app.savePage(s.id, index + 1);
    Speech.i.speak(
      s.pages[index].text,
      lang: s.lang,
      rate: app.settings.speed,
      onWord: (start, end) {
        final i = tokens.indexWhere((t) => start >= t.start && start < t.end + 1);
        if (mounted && i >= 0) setState(() => active = i);
      },
      onEnd: () {
        if (!mounted) return;
        setState(() => active = -1);
        if (!playing) return;
        if (index + 1 < s.pages.length) {
          Future.delayed(const Duration(milliseconds: 700), () {
            if (mounted && playing) readPage(index + 1);
          });
        } else {
          setState(() => playing = false);
        }
      },
    );
  }

  void toggle() {
    if (playing) {
      setState(() {
        playing = false;
        active = -1;
      });
      Speech.i.stop();
    } else {
      setState(() => playing = true);
      readPage(page);
    }
  }

  void go(int delta) {
    final s = story!;
    final next = page + delta;
    if (next < 0) return;
    if (next >= s.pages.length) {
      Speech.i.stop();
      context.pushReplacement('/books/${s.id}/complete');
      return;
    }
    if (playing) {
      readPage(next);
    } else {
      Speech.i.stop();
      setState(() {
        active = -1;
        page = next;
      });
      context.read<AppState>().savePage(s.id, next + 1);
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = story;
    if (s == null) return _missing(context);
    final app = context.watch<AppState>();
    final last = page == s.pages.length - 1;
    return AppPage(
      back: true,
      title: s.title,
      subtitle: 'Listen and follow each word as it lights up. Tap any word to learn it!',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          _pageChips(s, page),
          const SizedBox(height: 12),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 350),
            transitionBuilder: (child, anim) => SlideTransition(position: Tween(begin: const Offset(.25, 0), end: Offset.zero).animate(anim), child: FadeTransition(opacity: anim, child: child)),
            child: WhiteCard(
              key: ValueKey(page),
              padding: const EdgeInsets.all(16),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                AnimatedScale(scale: playing ? 1.02 : 1, duration: const Duration(seconds: 2), child: StoryCover(s, height: 170, radius: 22)),
                const SizedBox(height: 14),
                WordWrap(text: s.pages[page].text, lang: s.lang, active: active, fontSize: textSizeFor(app.settings.textSize), highlight: app.settings.highlight),
              ]),
            ),
          ),
          if (!app.settings.narration) Padding(padding: const EdgeInsets.only(top: 10), child: Text('Story narration is off. Turn it on in Settings to hear the story.', style: T.body(14, color: C.red, weight: FontWeight.w800))),
          const SizedBox(height: 14),
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Btn('', icon: Icons.arrow_back, kind: BtnKind.white, onPressed: page == 0 ? null : () => go(-1)),
            Btn(playing ? 'Pause' : (page == 0 ? 'Play story' : 'Play'), icon: playing ? Icons.pause : Icons.play_arrow, kind: BtnKind.blue, big: true, onPressed: app.settings.narration ? toggle : null),
            Btn(last ? 'Finish' : '', icon: last ? Icons.check : Icons.arrow_forward, onPressed: () => go(1)),
          ]),
          const SizedBox(height: 8),
          Row(children: [
            Text('Speed:', style: T.body(14, weight: FontWeight.w800)),
            const SizedBox(width: 10),
            Seg<double>(options: const [0.75, 1.0, 1.25], value: app.settings.speed, label: (v) => '$v×', onChanged: (v) => app.updateSettings((x) => x.speed = v)),
          ]),
          const SizedBox(height: 8),
          Align(alignment: Alignment.centerRight, child: TextButton(onPressed: () => context.pushReplacement('/books/${s.id}/read'), child: Text('Try Read and Learn', style: T.body(14, color: C.blueBtn, weight: FontWeight.w800)))),
        ]),
      ),
    );
  }
}

// ------------------------------------------------------------------ Read and Learn
const _marungko = ['Sounds', 'Syllables', 'Words', 'Sentence'];
const _phonics = ['Letter Sound', 'Blend', 'Words', 'Sentence'];

BoxDecoration _tile({Color border = C.yellow}) => BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(20), border: Border.all(color: border, width: 3));

class _Tile extends StatelessWidget {
  final String text;
  final VoidCallback onTap;
  final Color border;
  const _Tile(this.text, this.onTap, {this.border = C.yellow});
  @override
  Widget build(BuildContext context) => GestureDetector(
        onTap: onTap,
        child: Container(constraints: const BoxConstraints(minWidth: 70, minHeight: 70), padding: const EdgeInsets.symmetric(horizontal: 14), alignment: Alignment.center, decoration: _tile(border: border), child: Text(text, style: T.head(34, weight: FontWeight.w700))),
      );
}

class ReadAndLearnScreen extends StatefulWidget {
  final String id;
  const ReadAndLearnScreen({super.key, required this.id});
  @override
  State<ReadAndLearnScreen> createState() => _ReadAndLearnState();
}

class _ReadAndLearnState extends State<ReadAndLearnScreen> {
  late final Story? story = storyById(widget.id);
  late final MicCheck mic = MicCheck(story?.lang ?? 'en-US')..addListener(_refresh);
  int page = 0;
  int step = 0;

  void _refresh() {
    if (mounted) setState(() {});
  }

  @override
  void initState() {
    super.initState();
    if (story != null) WidgetsBinding.instance.addPostFrameCallback((_) => context.read<AppState>().openStory(story!.id));
  }

  @override
  void dispose() {
    mic.removeListener(_refresh);
    mic.dispose();
    Speech.i.stop();
    super.dispose();
  }

  void say(String t) => Speech.i.speak(t, lang: story!.lang, rate: (context.read<AppState>().settings.speed.clamp(0.5, 1.0)) * .85);

  void go(int delta) {
    final s = story!;
    final next = page + delta;
    if (next < 0) return;
    if (next >= s.pages.length) {
      context.pushReplacement('/books/${s.id}/complete');
      return;
    }
    mic.reset();
    setState(() {
      page = next;
      step = 0;
    });
    context.read<AppState>().savePage(s.id, next + 1);
  }

  @override
  Widget build(BuildContext context) {
    final s = story;
    if (s == null) return _missing(context);
    final phonics = !s.isFilipino;
    final steps = phonics ? _phonics : _marungko;
    final sentence = s.pages[page].text;
    final words = sentence.split(RegExp(r'\s+'));
    final focus = s.focus;
    final correct = words.where((w) => mic.heard.contains(normalizeWord(w))).length;
    final allRead = correct == words.length;

    Widget content;
    switch (step) {
      case 0:
        content = Row(children: [
          GestureDetector(
            onTap: () => say(focus.letter.substring(focus.letter.length - 1)),
            child: Container(width: 110, height: 110, alignment: Alignment.center, decoration: _tile(border: C.orange), child: Text(focus.letter, style: T.head(56, weight: FontWeight.w700))),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('The letter ${focus.letter[0]} says ${focus.sound}', style: T.head(20)),
              const SizedBox(height: 4),
              Text('as in ${focus.exampleFil} · ${focus.example}', style: T.body(14, color: C.muted)),
              const SizedBox(height: 8),
              Btn('Hear the words', icon: Icons.volume_up, kind: BtnKind.white, onPressed: () => say('${focus.exampleFil}. ${focus.example}')),
            ]),
          ),
        ]);
      case 1:
        content = phonics
            ? Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Say each sound, then blend them together. Tap to hear!', style: T.head(18)),
                const SizedBox(height: 12),
                Wrap(spacing: 8, runSpacing: 8, crossAxisAlignment: WrapCrossAlignment.center, children: [
                  for (var i = 0; i < focus.parts.length; i++) ...[
                    if (i > 0) Text('+', style: T.head(26, color: C.orange, weight: FontWeight.w700)),
                    _Tile(focus.parts[i], () => say(focus.parts[i])),
                  ],
                  Text('=', style: T.head(26, color: C.orange, weight: FontWeight.w700)),
                  _Tile(focus.example, () => say(focus.example), border: C.orange),
                ]),
              ])
            : Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Blend ${focus.letter[0]} with the vowels. Tap to hear!', style: T.head(18)),
                const SizedBox(height: 12),
                Wrap(spacing: 10, runSpacing: 10, children: [for (final syl in focus.syllables) _Tile(syl, () => say(syl))]),
              ]);
      case 2:
        final groups = [('English', focus.parts, focus.example), ('Filipino', focus.partsFil, focus.exampleFil)];
        content = Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          for (final g in (phonics ? groups : groups.reversed)) ...[
            Text(g.$1, style: T.body(14, color: C.muted, weight: FontWeight.w800)),
            const SizedBox(height: 6),
            Wrap(spacing: 8, runSpacing: 8, children: [for (final p in g.$2) _Tile(p, () => say(p))]),
            const SizedBox(height: 6),
            Btn(g.$3, icon: Icons.volume_up, kind: BtnKind.white, onPressed: () => say(g.$3)),
            const SizedBox(height: 8),
          ],
        ]);
      default:
        content = Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Read the whole sentence aloud', style: T.head(18)),
          const SizedBox(height: 4),
          Text('Tap the microphone, then read slowly. Words you read correctly turn green.', style: T.body(14, color: C.muted)),
          const SizedBox(height: 10),
          Row(children: [
            Btn('Hear it first', icon: Icons.volume_up, kind: BtnKind.white, onPressed: () => say(sentence)),
            const SizedBox(width: 10),
            Expanded(child: Text(mic.heard.isNotEmpty ? (allRead ? 'Amazing! You read every word!' : '$correct of ${words.length} words read') : '', style: T.body(14, weight: FontWeight.w800, color: allRead ? C.greenText : C.navy))),
          ]),
          const SizedBox(height: 8),
          ReadingScoreCard(words: words, heard: mic.heard, confidence: mic.confidence, lang: s.lang, storyId: s.id, page: page + 1),
        ]);
    }

    return AppPage(
      back: true,
      title: s.title,
      subtitle: 'Read and Learn · Practice with ${phonics ? 'English Phonics' : 'the Marungko Method'}',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          ScrollSeg<int>(options: const [0, 1, 2, 3], value: step, label: (i) => '${i + 1} · ${steps[i]}', onChanged: (i) => setState(() => step = i)),
          const SizedBox(height: 8),
          Align(alignment: Alignment.centerRight, child: Tag('Page ${page + 1} of ${s.pages.length}', bg: const Color(0xFFFFF3D6), fg: C.navy)),
          const SizedBox(height: 8),
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('TODAY’S SENTENCE', style: T.body(12, color: C.muted, weight: FontWeight.w800).copyWith(letterSpacing: 1)),
              const SizedBox(height: 6),
              WordWrap(text: sentence, lang: s.lang, fontSize: 28, tappable: false, focusWord: s.pages[page].word, said: mic.heard),
            ]),
          ),
          const SizedBox(height: 12),
          WhiteCard(child: content),
          if (mic.notice.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 10), child: Text(mic.notice, style: T.body(14, color: C.red, weight: FontWeight.w800))),
          const SizedBox(height: 14),
          Center(child: MicButton(listening: mic.listening, onPressed: () {
            setState(() => step = 3);
            mic.listen(words);
          })),
          if ((mic.notice.isNotEmpty || step == 3) && !mic.listening && !allRead) Center(child: Btn('I read it!', icon: Icons.check, kind: BtnKind.white, onPressed: () => mic.markAll(words))),
          const SizedBox(height: 8),
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Btn('Previous', icon: Icons.arrow_back, kind: BtnKind.white, onPressed: page == 0 ? null : () => go(-1)),
            Btn(page == s.pages.length - 1 ? 'Finish' : 'Next', icon: page == s.pages.length - 1 ? Icons.check : Icons.arrow_forward, onPressed: () => go(1)),
          ]),
        ]),
      ),
    );
  }
}

// ------------------------------------------------------------------ Read With Me
class ReadWithMeScreen extends StatefulWidget {
  final String id;
  const ReadWithMeScreen({super.key, required this.id});
  @override
  State<ReadWithMeScreen> createState() => _ReadWithMeState();
}

class _ReadWithMeState extends State<ReadWithMeScreen> {
  late final Story? story = storyById(widget.id);
  late final MicCheck mic = MicCheck(story?.lang ?? 'en-US')..addListener(_refresh);
  int page = 0;
  bool turn = false;
  int active = -1;

  void _refresh() {
    if (mounted) setState(() {});
  }

  @override
  void initState() {
    super.initState();
    if (story != null) WidgetsBinding.instance.addPostFrameCallback((_) => context.read<AppState>().openStory(story!.id));
  }

  @override
  void dispose() {
    mic.removeListener(_refresh);
    mic.dispose();
    Speech.i.stop();
    super.dispose();
  }

  void readToMe() {
    final s = story!;
    final tokens = tokenize(s.pages[page].text);
    mic.reset();
    setState(() {
      turn = false;
      active = 0;
    });
    Speech.i.speak(
      s.pages[page].text,
      lang: s.lang,
      rate: context.read<AppState>().settings.speed.clamp(0.5, 1.0) * .9,
      onWord: (start, end) {
        final i = tokens.indexWhere((t) => start >= t.start && start < t.end + 1);
        if (mounted && i >= 0) setState(() => active = i);
      },
      onEnd: () {
        if (mounted) {
          setState(() {
            active = -1;
            turn = true;
          });
        }
      },
    );
  }

  void go(int delta) {
    final s = story!;
    final next = page + delta;
    if (next < 0) return;
    if (next >= s.pages.length) {
      context.pushReplacement('/books/${s.id}/complete');
      return;
    }
    Speech.i.stop();
    mic.reset();
    setState(() {
      page = next;
      turn = false;
      active = -1;
    });
    context.read<AppState>().savePage(s.id, next + 1);
  }

  @override
  Widget build(BuildContext context) {
    final s = story;
    if (s == null) return _missing(context);
    final app = context.watch<AppState>();
    final words = s.pages[page].text.split(RegExp(r'\s+'));
    return AppPage(
      back: true,
      title: s.title,
      subtitle: 'Read With Me · I read a page, then it’s your turn!',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Seg<bool>(options: const [false, true], value: turn, label: (v) => v ? '2 · You read' : '1 · I read', onChanged: (v) {
              Speech.i.stop();
              setState(() {
                turn = v;
                active = -1;
              });
            }),
            const Spacer(),
            Tag('${page + 1}/${s.pages.length}', bg: const Color(0xFFFFF3D6), fg: C.navy),
          ]),
          const SizedBox(height: 12),
          WhiteCard(
            padding: const EdgeInsets.all(16),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              StoryCover(s, height: 150, radius: 20),
              const SizedBox(height: 12),
              WordWrap(text: s.pages[page].text, lang: s.lang, active: active, fontSize: textSizeFor(app.settings.textSize), highlight: app.settings.highlight, said: mic.heard),
            ]),
          ),
          const SizedBox(height: 14),
          if (!turn)
            Center(child: Btn('Read it to me', icon: Icons.volume_up, big: true, onPressed: app.settings.narration ? readToMe : null))
          else ...[
            Center(child: MicButton(listening: mic.listening, label: 'My turn!', onPressed: () => mic.listen(words))),
            if (mic.notice.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(mic.notice, style: T.body(14, color: C.red, weight: FontWeight.w800))),
            if (mic.heard.isEmpty && !mic.listening) Center(child: Btn('I read it!', icon: Icons.check, kind: BtnKind.white, onPressed: () => mic.markAll(words))),
          ],
          const SizedBox(height: 10),
          ReadingScoreCard(words: words, heard: mic.heard, confidence: mic.confidence, lang: s.lang, storyId: s.id, page: page + 1),
          const SizedBox(height: 12),
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Btn('Previous', icon: Icons.arrow_back, kind: BtnKind.white, onPressed: page == 0 ? null : () => go(-1)),
            Btn(page == s.pages.length - 1 ? 'Finish' : 'Next', icon: page == s.pages.length - 1 ? Icons.check : Icons.arrow_forward, onPressed: () => go(1)),
          ]),
        ]),
      ),
    );
  }
}

// ------------------------------------------------------------------ Normal reading
class NormalReadingScreen extends StatefulWidget {
  final String id;
  const NormalReadingScreen({super.key, required this.id});
  @override
  State<NormalReadingScreen> createState() => _NormalReadingState();
}

class _NormalReadingState extends State<NormalReadingScreen> {
  late final Story? story = storyById(widget.id);
  int page = 0;

  @override
  void initState() {
    super.initState();
    final s = story;
    if (s != null) {
      final p = context.read<AppState>().progress[s.id];
      page = p == null || p.finished ? 0 : (p.page - 1).clamp(0, s.pages.length - 1);
      WidgetsBinding.instance.addPostFrameCallback((_) => context.read<AppState>().savePage(s.id, page + 1));
    }
  }

  void go(int d) {
    final s = story!;
    final next = page + d;
    if (next < 0) return;
    if (next >= s.pages.length) {
      context.pushReplacement('/books/${s.id}/complete');
      return;
    }
    setState(() => page = next);
    context.read<AppState>().savePage(s.id, next + 1);
  }

  @override
  Widget build(BuildContext context) {
    final s = story;
    if (s == null) return _missing(context);
    final app = context.watch<AppState>();
    return AppPage(
      back: true,
      title: s.title,
      subtitle: 'Read on your own. Tap any word to learn it!',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [Expanded(child: Bar(((page + 1) / s.pages.length * 100).round(), color: C.green)), const SizedBox(width: 10), Text('Page ${page + 1} of ${s.pages.length}', style: T.body(13, weight: FontWeight.w800, color: C.muted))]),
          const SizedBox(height: 14),
          WhiteCard(
            padding: const EdgeInsets.all(18),
            child: Column(children: [
              StoryCover(s, height: 190, radius: 22),
              const SizedBox(height: 16),
              Align(alignment: Alignment.centerLeft, child: WordWrap(text: s.pages[page].text, lang: s.lang, fontSize: textSizeFor(app.settings.textSize))),
            ]),
          ),
          const SizedBox(height: 16),
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Btn('Previous', icon: Icons.arrow_back, kind: BtnKind.white, onPressed: page == 0 ? null : () => go(-1)),
            Btn(page == s.pages.length - 1 ? 'Finish' : 'Next', icon: page == s.pages.length - 1 ? Icons.check : Icons.arrow_forward, onPressed: () => go(1)),
          ]),
        ]),
      ),
    );
  }
}

// ------------------------------------------------------------------ Story complete + quiz
class CompleteScreen extends StatefulWidget {
  final String id;
  const CompleteScreen({super.key, required this.id});
  @override
  State<CompleteScreen> createState() => _CompleteState();
}

class _CompleteState extends State<CompleteScreen> {
  late final Story? story = storyById(widget.id);
  int q = 0;
  int? picked;
  int score = 0;
  bool firstTry = true;

  @override
  void initState() {
    super.initState();
    if (story != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        context.read<AppState>().finishStory(story!.id, 3);
        Sounds.i.play('star');
      });
    }
  }

  void pick(int i) {
    final quiz = story!.questions[q];
    if (picked != null && quiz.options[picked!].correct) return;
    setState(() => picked = i);
    final ok = quiz.options[i].correct;
    Sounds.i.play(ok ? 'good' : 'oops');
    if (ok && firstTry) score++;
    if (!ok) firstTry = false;
  }

  void next() {
    final s = story!;
    setState(() {
      q++;
      picked = null;
      firstTry = true;
    });
    if (q >= s.questions.length) context.read<AppState>().recordQuiz(s.id, score, s.questions.length);
  }

  @override
  Widget build(BuildContext context) {
    final s = story;
    if (s == null) return _missing(context);
    final app = context.watch<AppState>();
    final words = s.pages.fold<int>(0, (n, p) => n + p.text.split(RegExp(r'\s+')).length);
    final done = q >= s.questions.length;
    final quiz = done ? null : s.questions[q];
    final right = quiz != null && picked != null && quiz.options[picked!].correct;
    return AppPage(
      child: Column(children: [
        const SizedBox(height: 8),
        WhiteCard(
          padding: const EdgeInsets.all(22),
          child: Column(children: [
            Row(mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.end, children: const [
              Icon(Icons.star, size: 48, color: C.yellow2),
              Icon(Icons.star, size: 64, color: C.yellow2),
              Icon(Icons.star, size: 48, color: C.yellow2),
            ]),
            const SizedBox(height: 8),
            Text('Great job, ${app.displayName}!', textAlign: TextAlign.center, style: T.fun(40)),
            const SizedBox(height: 6),
            Text.rich(TextSpan(style: T.body(16, color: C.muted), children: [const TextSpan(text: 'You finished '), TextSpan(text: s.title, style: T.body(16, color: C.navy, weight: FontWeight.w800)), const TextSpan(text: '.')]), textAlign: TextAlign.center),
            const SizedBox(height: 16),
            Row(children: [
              for (final (v, l) in [('${s.pages.length}/${s.pages.length}', 'Pages read'), ('$words', 'Words read'), ('${s.minutes} min', 'Reading time')])
                Expanded(child: Container(margin: const EdgeInsets.symmetric(horizontal: 4), padding: const EdgeInsets.symmetric(vertical: 12), decoration: BoxDecoration(color: C.cream, borderRadius: BorderRadius.circular(16)), child: Column(children: [Text(v, style: T.head(20, weight: FontWeight.w700)), Text(l, style: T.body(11, color: C.muted, weight: FontWeight.w800))]))),
            ]),
            const SizedBox(height: 14),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: const Color(0xFFEAF3FB), borderRadius: BorderRadius.circular(20)),
              child: Row(children: [
                Container(width: 50, height: 50, decoration: const BoxDecoration(color: C.blueBtn, shape: BoxShape.circle), child: const Icon(Icons.workspace_premium, color: Colors.white, size: 28)),
                const SizedBox(width: 12),
                Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text('New badge: ${s.theme} Star', style: T.head(16)), Text('For finishing “${s.title}”', style: T.body(13, color: C.muted))])),
              ]),
            ),
            const Divider(height: 32, color: C.cardBorder),
            if (done) ...[
              Text('You answered $score of ${s.questions.length} questions on the first try!', textAlign: TextAlign.center, style: T.head(19)),
              const SizedBox(height: 6),
              Text(score == s.questions.length ? 'You really understood the story!' : 'Read it again to remember more.', style: T.body(14, color: C.muted)),
            ] else ...[
              Tag('Question ${q + 1} of ${s.questions.length}', bg: const Color(0xFFFFF3D6), fg: C.navy),
              const SizedBox(height: 8),
              Text(quiz!.question, textAlign: TextAlign.center, style: T.head(20)),
              const SizedBox(height: 12),
              Wrap(spacing: 10, runSpacing: 10, alignment: WrapAlignment.center, children: [
                for (var i = 0; i < quiz.options.length; i++)
                  GestureDetector(
                    onTap: () => pick(i),
                    child: Container(
                      width: 104,
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: picked == i ? (quiz.options[i].correct ? const Color(0xFFEAF8F0) : const Color(0xFFFDEEEA)) : Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: picked == i ? (quiz.options[i].correct ? C.green : const Color(0xFFE76F51)) : C.cardBorder, width: 3),
                      ),
                      child: Column(children: [
                        Container(width: 56, height: 56, decoration: BoxDecoration(color: Color(quiz.options[i].color), shape: BoxShape.circle)),
                        const SizedBox(height: 8),
                        Text(quiz.options[i].label, textAlign: TextAlign.center, style: T.head(14)),
                      ]),
                    ),
                  ),
              ]),
              const SizedBox(height: 10),
              Text(picked == null ? '' : (right ? 'Yes! That’s right!' : 'Not quite. Try again!'), style: T.body(15, weight: FontWeight.w800, color: right ? C.greenText : const Color(0xFFB5442A))),
              if (right) Btn(q + 1 < s.questions.length ? 'Next question' : 'See my score', icon: Icons.arrow_forward, onPressed: next),
            ],
          ]),
        ),
        const SizedBox(height: 16),
        Wrap(spacing: 10, runSpacing: 6, alignment: WrapAlignment.center, children: [
          Btn('Read again', kind: BtnKind.white, onPressed: () => context.go('/books/${s.id}')),
          Btn('Next story', kind: BtnKind.blue, onPressed: () => context.go('/books')),
          Btn('Back to Home', onPressed: () => context.go('/home')),
        ]),
      ]),
    );
  }
}
