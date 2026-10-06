import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../services/book_match.dart';
import '../services/ocr.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

/// Scan a book page or a flashcard with the phone's camera. The words are read on the phone
/// (Google ML Kit), matched to the library, and read aloud.
class ScanScreen extends StatefulWidget {
  final bool cardMode;
  const ScanScreen({super.key, this.cardMode = false});
  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> {
  late bool cardMode = widget.cardMode;
  final _picker = ImagePicker();
  XFile? file;
  Uint8List? bytes;
  bool reading = false;
  String error = '';
  String text = '';
  BookMatch? match;
  ({int deck, int card})? cardMatch;
  bool done = false;

  void _reset() {
    text = '';
    match = null;
    cardMatch = null;
    done = false;
    error = '';
  }

  Future<void> pick(ImageSource source) async {
    try {
      final f = await _picker.pickImage(source: source, maxWidth: 1800, imageQuality: 88);
      if (f == null) return;
      final b = await f.readAsBytes();
      setState(() {
        _reset();
        file = f;
        bytes = b;
      });
    } catch (e) {
      setState(() => error = source == ImageSource.camera ? 'Please allow the camera, or choose a picture of the page instead.' : 'I couldn’t open that picture. Try another one.');
    }
  }

  Future<void> readPage() async {
    final f = file;
    if (f == null) return;
    setState(() {
      reading = true;
      error = '';
    });
    try {
      final t = await recognizeText(f.path);
      if (t.isEmpty) {
        setState(() => error = 'I couldn’t find any words in this picture. Try a brighter, closer photo.');
        return;
      }
      setState(() {
        text = t;
        match = cardMode ? null : recognizeBook(t);
        cardMatch = cardMode ? recognizeFlashCard(t) : null;
        done = true;
      });
    } catch (e) {
      setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => reading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final lang = match?.story.lang ?? (app.settings.language == 'Filipino' ? 'fil-PH' : 'en-US');
    return AppPage(
      back: true,
      banner: 'Scan a Book',
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text('Show me a page from your book and I’ll read it with you!', style: T.body(15, color: C.muted)),
        const SizedBox(height: 12),
        Seg<bool>(options: const [false, true], value: cardMode, label: (v) => v ? 'Flash card' : 'Book page', onChanged: (v) => setState(() {
              cardMode = v;
              _reset();
            })),
        const SizedBox(height: 12),
        Panel(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            AspectRatio(
              aspectRatio: 4 / 3,
              child: Container(
                decoration: BoxDecoration(color: const Color(0xFFFFFBEF), borderRadius: BorderRadius.circular(24), border: Border.all(color: C.yellow2, width: 3)),
                clipBehavior: Clip.antiAlias,
                child: bytes != null
                    ? Image.memory(bytes!, fit: BoxFit.contain, semanticLabel: 'The picture you added')
                    : Column(mainAxisAlignment: MainAxisAlignment.center, children: [
                        const Icon(Icons.photo_camera_outlined, size: 54, color: Color(0xFF6B7A89)),
                        const SizedBox(height: 8),
                        Text(cardMode ? 'Point the camera at a flashcard' : 'Take a picture of the page', style: T.body(16, weight: FontWeight.w800, color: C.muted)),
                      ]),
              ),
            ),
            const SizedBox(height: 12),
            Wrap(spacing: 10, runSpacing: 6, children: [
              Btn('Take photo', icon: Icons.photo_camera, kind: BtnKind.blue, onPressed: () => pick(ImageSource.camera)),
              Btn('Choose picture', icon: Icons.photo_library_outlined, kind: BtnKind.white, onPressed: () => pick(ImageSource.gallery)),
              if (file != null) Btn('Remove', kind: BtnKind.white, onPressed: () => setState(() {
                    file = null;
                    bytes = null;
                    _reset();
                  })),
            ]),
            if (file != null) ...[
              const SizedBox(height: 8),
              Btn(reading ? 'Reading…' : (cardMode ? 'Find this card' : 'Read this page'), icon: Icons.auto_stories, expand: true, big: true, onPressed: reading ? null : readPage),
            ],
            if (reading) const Padding(padding: EdgeInsets.only(top: 8), child: LinearProgressIndicator(color: C.orange, minHeight: 8)),
            if (error.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(error, style: T.body(14, color: C.red, weight: FontWeight.w800))),
          ]),
        ),
        const SizedBox(height: 14),
        if (!done)
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('How it works', style: T.head(18)),
              const SizedBox(height: 6),
              for (final l in ['Take or choose a picture of one page.', 'Tap Read this page.', 'I read the words, find the book and read it to you.']) Padding(padding: const EdgeInsets.only(bottom: 4), child: Text('•  $l', style: T.body(14, color: C.muted))),
            ]),
          )
        else ...[
          if (!cardMode && match != null)
            WhiteCard(
              color: const Color(0xFFEAF8F0),
              border: C.green,
              child: Row(children: [
                const Icon(Icons.check_circle, color: C.green, size: 30),
                const SizedBox(width: 12),
                Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text('Book found!', style: T.body(13, color: C.greenText, weight: FontWeight.w800)), Text('${match!.story.title} · page ${match!.page + 1}', style: T.head(18))])),
                Btn('Open this book', onPressed: () => context.push('/books/${match!.story.id}')),
              ]),
            )
          else if (cardMode && cardMatch != null)
            WhiteCard(
              color: const Color(0xFFEAF8F0),
              border: C.green,
              child: Row(children: [
                const Icon(Icons.check_circle, color: C.green, size: 30),
                const SizedBox(width: 12),
                Expanded(child: Text('Card found! Open it to see it in AR.', style: T.head(17))),
                Btn('Open card', onPressed: () => context.go('/cards?card=${decksCardFil(cardMatch!)}')),
              ]),
            )
          else
            WhiteCard(child: Text(cardMode ? 'I could not find a flashcard in this picture. Try a closer photo of the card.' : 'This page is not in my library yet, but I can still read it to you.', style: T.body(14, color: C.muted, weight: FontWeight.w700))),
          const SizedBox(height: 12),
          WhiteCard(child: ReadAloudText(text: text.replaceAll(RegExp(r'\s*\n\s*'), ' '), lang: lang, rate: app.settings.speed, fontSize: 24)),
        ],
      ]),
    );
  }
}
