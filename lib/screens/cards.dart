import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:model_viewer_plus/model_viewer_plus.dart';
import 'package:provider/provider.dart';

import '../data/content.dart';
import '../data/vocab.dart';
import '../services/mic.dart';
import '../services/sounds.dart';
import '../services/speech.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

const _steps = ['Sound', 'Syllables', 'Word', 'Say it!'];

/// Marungko flashcards. Each card has a 3D object you can turn with your finger,
/// and an AR button that puts the object in the room through the phone's camera.
class FlashcardsScreen extends StatefulWidget {
  final String? startCard;
  const FlashcardsScreen({super.key, this.startCard});
  @override
  State<FlashcardsScreen> createState() => _FlashcardsState();
}

class _FlashcardsState extends State<FlashcardsScreen> {
  int deckIdx = 0;
  int cardIdx = 0;
  int step = 0;
  final Set<String> stars = {};
  late final MicCheck mic = MicCheck('fil-PH')..addListener(_refresh);
  String? _creditedKey;

  @override
  void initState() {
    super.initState();
    final start = widget.startCard;
    if (start != null) {
      for (var d = 0; d < decks.length; d++) {
        final c = decks[d].cards.indexWhere((x) => x.fil == start);
        if (c >= 0) {
          deckIdx = d;
          cardIdx = c;
        }
      }
    }
  }

  void _refresh() {
    if (!mounted) return;
    final card = decks[deckIdx].cards[cardIdx];
    final key = '${decks[deckIdx].letter}-${card.fil}';
    if (mic.heard.contains(normalizeWord(card.fil)) && _creditedKey != key) {
      _creditedKey = key;
      stars.add(key);
      Sounds.i.play('good');
    }
    setState(() {});
  }

  @override
  void dispose() {
    mic.removeListener(_refresh);
    mic.dispose();
    Speech.i.stop();
    super.dispose();
  }

  void say(String t, [double rate = .8]) => Speech.i.speak(t, lang: 'fil-PH', rate: rate);

  void goStep(int i) {
    setState(() => step = i);
    mic.reset();
    final deck = decks[deckIdx];
    if (i == 0) say(deck.letter.substring(deck.letter.length - 1), .7);
    if (i == 2) say(deck.cards[cardIdx].fil);
  }

  void pickCard(int i) {
    mic.reset();
    setState(() {
      cardIdx = i;
      step = 0;
    });
  }

  void pickDeck(int i) {
    mic.reset();
    setState(() {
      deckIdx = i;
      cardIdx = 0;
      step = 0;
    });
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final unlocked = app.lessonsDone + 1; // every learned sound plus the one being learned now
    final deck = decks[deckIdx];
    final card = deck.cards[cardIdx];
    final key = '${deck.letter}-${card.fil}';
    final sound = deck.letter.substring(deck.letter.length - 1);
    final saidIt = mic.heard.contains(normalizeWord(card.fil));

    Widget stepBody;
    switch (step) {
      case 0:
        stepBody = Column(children: [
          GestureDetector(
            onTap: () => say(sound, .7),
            child: Container(width: 110, height: 90, alignment: Alignment.center, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(24), border: Border.all(color: C.orange, width: 4)), child: Text(deck.letter, style: T.head(48, weight: FontWeight.w700))),
          ),
          const SizedBox(height: 6),
          Text('The letter ${sound.toUpperCase()} says /$sound/', style: T.body(15, color: const Color(0xFFB4520F), weight: FontWeight.w800)),
        ]);
      case 1:
        stepBody = Column(children: [
          Wrap(spacing: 10, runSpacing: 10, alignment: WrapAlignment.center, children: [
            for (final s in card.syl)
              GestureDetector(onTap: () => say(s, .7), child: Container(constraints: const BoxConstraints(minWidth: 70, minHeight: 66), padding: const EdgeInsets.symmetric(horizontal: 16), alignment: Alignment.center, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18), border: Border.all(color: C.yellow, width: 3)), child: Text(s, style: T.head(32, weight: FontWeight.w700)))),
          ]),
          const SizedBox(height: 6),
          Text('Tap each part to hear it, then blend them!', style: T.body(14, color: C.muted)),
        ]);
      case 2:
        stepBody = Column(children: [
          GestureDetector(onTap: () => say(card.fil), child: Text(card.fil, style: T.head(44, weight: FontWeight.w700))),
          Text(card.syl.join(' · '), style: T.body(15, color: C.muted, weight: FontWeight.w800)),
          Text(card.eng, style: T.body(14, color: C.muted)),
        ]);
      default:
        stepBody = Column(children: [
          Text(card.fil, style: T.head(44, weight: FontWeight.w700, color: saidIt ? C.green : C.navy)),
          Text(saidIt ? 'Great job! You said it!' : (mic.listening ? 'Listening… say the word now!' : 'Tap the microphone and say the word.'), style: T.body(14, color: C.muted, weight: FontWeight.w800)),
          const SizedBox(height: 10),
          MicButton(listening: mic.listening, label: 'Say it!', onPressed: () => mic.listen([card.fil])),
          if (mic.notice.isNotEmpty) Text(mic.notice, textAlign: TextAlign.center, style: T.body(13, color: C.red, weight: FontWeight.w800)),
          if (!saidIt) Btn('I said it!', icon: Icons.check, kind: BtnKind.white, onPressed: () => mic.markAll([card.fil])),
          if (saidIt && cardIdx < deck.cards.length - 1) Btn('Next card', icon: Icons.arrow_forward, onPressed: () => pickCard(cardIdx + 1)),
        ]);
    }

    return AppPage(
      banner: 'Flashcards',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(children: [
              for (var i = 0; i < decks.length; i++)
                Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: GestureDetector(
                    onTap: i >= unlocked ? null : () => pickDeck(i),
                    child: Container(
                      width: 56, height: 52, alignment: Alignment.center,
                      decoration: BoxDecoration(color: i >= unlocked ? const Color(0xFFECE8DF) : (i == deckIdx ? C.orange : Colors.white), borderRadius: BorderRadius.circular(16), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 6, offset: Offset(0, 3))]),
                      child: i >= unlocked ? const Icon(Icons.lock, size: 18, color: C.muted) : Text(decks[i].letter, style: T.head(20, weight: FontWeight.w700)),
                    ),
                  ),
                ),
            ]),
          ),
          if (unlocked <= decks.length) Padding(padding: const EdgeInsets.only(top: 6), child: GestureDetector(onTap: () => context.go('/learn'), child: Text('Practice more sounds to unlock decks', style: T.body(13, color: C.blueBtn, weight: FontWeight.w800)))),
          const SizedBox(height: 10),
          ScrollSeg<int>(options: const [0, 1, 2, 3], value: step, label: (i) => '${i + 1} · ${_steps[i]}', onChanged: goStep),
          const SizedBox(height: 12),
          WhiteCard(
            border: C.yellow,
            padding: const EdgeInsets.fromLTRB(12, 12, 12, 16),
            child: Column(children: [
              SizedBox(
                height: 260,
                child: ModelViewer(
                  key: ValueKey(card.fil),
                  src: 'assets/models/${card.fil}.glb',
                  alt: '3D model of ${card.eng}',
                  ar: true,
                  arModes: const ['scene-viewer', 'webxr', 'quick-look'],
                  autoRotate: true,
                  cameraControls: true,
                  disableZoom: true,
                  backgroundColor: Colors.transparent,
                  debugLogging: false,
                ),
              ),
              Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                const Icon(Icons.view_in_ar, size: 18, color: C.blueBtn),
                const SizedBox(width: 6),
                Flexible(child: Text('Turn it with your finger. Tap the AR button to see it in your room.', textAlign: TextAlign.center, style: T.body(12, color: C.muted, weight: FontWeight.w700))),
              ]),
              const SizedBox(height: 12),
              stepBody,
            ]),
          ),
          const SizedBox(height: 12),
          Text('Deck ${deck.letter}', style: T.head(18)),
          const SizedBox(height: 8),
          for (var i = 0; i < deck.cards.length; i++)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: GestureDetector(
                onTap: () => pickCard(i),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  decoration: BoxDecoration(color: i == cardIdx ? const Color(0xFFEAF3FB) : Colors.white, borderRadius: BorderRadius.circular(18), border: Border.all(color: i == cardIdx ? C.blueBtn : C.cardBorder, width: 2)),
                  child: Row(children: [
                    Container(width: 28, height: 28, decoration: BoxDecoration(color: Color(deck.cards[i].color), shape: BoxShape.circle)),
                    const SizedBox(width: 12),
                    Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(deck.cards[i].fil, style: T.body(16, weight: FontWeight.w800)), Text(deck.cards[i].eng, style: T.body(12, color: C.muted))])),
                    if (stars.contains('${deck.letter}-${deck.cards[i].fil}')) const Icon(Icons.star, color: C.yellow2, size: 22),
                  ]),
                ),
              ),
            ),
          if (step < 3) Btn('Next step', icon: Icons.arrow_forward, expand: true, onPressed: () => goStep(step + 1)),
          const SizedBox(height: 4),
          Btn('Scan a flashcard', icon: Icons.photo_camera, kind: BtnKind.blue, expand: true, onPressed: () => context.push('/scan?mode=card')),
          if (stars.contains(key)) const SizedBox.shrink(),
        ]),
      ),
    );
  }
}
