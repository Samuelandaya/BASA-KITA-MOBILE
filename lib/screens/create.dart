import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../state/app_state.dart';
import '../state/models.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

const _characters = ['Mia', 'Duck', 'Puppy', 'Cat'];
const _places = ['Park', 'School', 'Home', 'Farm'];

/// Write a story page by page, add a picture to each page, and save it to My Stories.
/// Opening Create always starts a new story; `?story=<id>` edits a saved one.
class CreateScreen extends StatefulWidget {
  final String? editId;
  const CreateScreen({super.key, this.editId});
  @override
  State<CreateScreen> createState() => _CreateScreenState();
}

class _CreateScreenState extends State<CreateScreen> {
  late final Draft? existing = context.read<AppState>().drafts.where((d) => d.id == widget.editId).firstOrNull;
  late final String draftId = existing?.id ?? 'draft-${DateTime.now().millisecondsSinceEpoch}';
  late final title = TextEditingController(text: existing?.title ?? '');
  late String character = existing?.character ?? 'Cat';
  late String place = existing?.place ?? 'Home';
  late String language = existing?.language ?? 'Filipino';
  late List<String> pages = List.of(existing?.texts ?? ['', '', '']);
  late List<String> images = List.of(existing?.images ?? []);
  late final text = TextEditingController(text: pages.first);
  int current = 0;
  String saved = '';

  void _keepText() => pages[current] = text.text;

  void goPage(int i) {
    _keepText();
    setState(() {
      current = i;
      text.text = pages[i];
    });
  }

  Future<void> choosePicture() async {
    try {
      // Pictures are shrunk so saved stories stay small.
      final f = await ImagePicker().pickImage(source: ImageSource.gallery, maxWidth: 640, imageQuality: 75);
      if (f == null) return;
      final b64 = base64Encode(await f.readAsBytes());
      setState(() {
        while (images.length <= current) {
          images.add('');
        }
        images[current] = b64;
        saved = '';
      });
    } catch (_) {
      setState(() => saved = '');
    }
  }

  void save() {
    _keepText();
    while (images.length < pages.length) {
      images.add('');
    }
    context.read<AppState>().saveDraft(Draft(
          id: draftId,
          title: title.text.trim().isEmpty ? 'My Story' : title.text.trim(),
          language: language,
          character: character,
          place: place,
          texts: List.of(pages),
          images: List.of(images),
        ));
    setState(() => saved = 'Saved! Find it in My Stories.');
  }

  Widget _choice(String label, bool on, VoidCallback onTap) => GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 11),
          decoration: BoxDecoration(color: on ? const Color(0xFFEAF3FB) : Colors.white, borderRadius: BorderRadius.circular(22), border: Border.all(color: on ? C.blueBtn : C.cardBorder, width: 2)),
          child: Text(label, style: T.body(14, weight: FontWeight.w800)),
        ),
      );

  @override
  Widget build(BuildContext context) {
    final img = current < images.length ? images[current] : '';
    return AppPage(
      back: true,
      title: 'Create Your Own Story',
      subtitle: 'Pick a friend, pick a place, and write your story one page at a time.',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Story title', style: T.body(14, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              TextField(controller: title, style: T.body(16), decoration: const InputDecoration(hintText: 'My Story'), onChanged: (_) => setState(() => saved = '')),
              const SizedBox(height: 14),
              Text('Main character', style: T.body(14, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              Wrap(spacing: 8, runSpacing: 8, children: [for (final c in _characters) _choice(c, character == c, () => setState(() => character = c))]),
              const SizedBox(height: 14),
              Text('Place', style: T.body(14, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              Wrap(spacing: 8, runSpacing: 8, children: [for (final p in _places) _choice(p, place == p, () => setState(() => place = p))]),
              const SizedBox(height: 14),
              Text('Language', style: T.body(14, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              Seg<String>(options: const ['Filipino', 'English', 'Bilingual'], value: language, label: (v) => v, onChanged: (v) => setState(() => language = v)),
            ]),
          ),
          const SizedBox(height: 14),
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(children: [Expanded(child: Text('Page ${current + 1}', style: T.head(20))), Text('${pages.length} pages', style: T.body(13, color: C.muted, weight: FontWeight.w800))]),
              const SizedBox(height: 10),
              Container(
                width: double.infinity,
                constraints: const BoxConstraints(minHeight: 180),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(color: const Color(0xFFFFFBEF), borderRadius: BorderRadius.circular(20), border: Border.all(color: C.yellow2, width: img.isEmpty ? 3 : 3, style: img.isEmpty ? BorderStyle.solid : BorderStyle.solid)),
                child: Column(children: [
                  if (img.isNotEmpty) ClipRRect(borderRadius: BorderRadius.circular(12), child: Image.memory(base64Decode(img), height: 180, fit: BoxFit.contain, semanticLabel: 'Picture for page ${current + 1}'))
                  else ...[const Icon(Icons.image_outlined, size: 40, color: Color(0xFF6B7A89)), const SizedBox(height: 6), Text('Choose a picture for $character at the ${place.toLowerCase()}', textAlign: TextAlign.center, style: T.body(14, weight: FontWeight.w800, color: C.muted))],
                  const SizedBox(height: 8),
                  Wrap(spacing: 8, children: [
                    Btn(img.isEmpty ? 'Add picture' : 'Change picture', icon: Icons.add_photo_alternate_outlined, kind: BtnKind.white, onPressed: choosePicture),
                    if (img.isNotEmpty) Btn('Remove', kind: BtnKind.white, onPressed: () => setState(() => images[current] = '')),
                  ]),
                ]),
              ),
              const SizedBox(height: 12),
              Text('What happens on this page?', style: T.body(14, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              TextField(controller: text, maxLines: 4, style: T.body(16), decoration: InputDecoration(hintText: '$character goes to the ${place.toLowerCase()}...'), onChanged: (_) => setState(() => saved = '')),
              const SizedBox(height: 12),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(children: [
                  for (var i = 0; i < pages.length; i++)
                    Padding(
                      padding: const EdgeInsets.only(right: 8),
                      child: GestureDetector(
                        onTap: () => goPage(i),
                        child: Container(width: 64, height: 54, alignment: Alignment.center, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12), border: Border.all(color: current == i ? C.orange : C.cardBorder, width: 2)), child: Text('${i + 1}', style: T.body(16, weight: FontWeight.w800, color: C.navy))),
                      ),
                    ),
                  GestureDetector(
                    onTap: () {
                      _keepText();
                      setState(() {
                        pages.add('');
                        current = pages.length - 1;
                        text.text = '';
                      });
                    },
                    child: Container(width: 64, height: 54, alignment: Alignment.center, decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12), border: Border.all(color: C.cardBorder, width: 2)), child: const Icon(Icons.add, color: C.muted)),
                  ),
                ]),
              ),
            ]),
          ),
          const SizedBox(height: 14),
          if (saved.isNotEmpty) Center(child: Padding(padding: const EdgeInsets.only(bottom: 8), child: Text(saved, style: T.body(14, color: C.greenText, weight: FontWeight.w800)))),
          Btn('Save story', icon: Icons.check, expand: true, big: true, onPressed: save),
        ]),
      ),
    );
  }
}

/// Reads a story the learner wrote, with narration and highlighted words.
class MadeStoryScreen extends StatefulWidget {
  final String id;
  const MadeStoryScreen({super.key, required this.id});
  @override
  State<MadeStoryScreen> createState() => _MadeStoryState();
}

class _MadeStoryState extends State<MadeStoryScreen> {
  int page = 0;

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final draft = app.drafts.where((d) => d.id == widget.id).firstOrNull;
    if (draft == null) return AppPage(back: true, title: 'Story not found', child: Btn('Back to My Stories', onPressed: () => context.go('/my-stories')));
    // Keep each page's picture with its text, and skip pages with no words.
    final slides = [
      for (var i = 0; i < draft.texts.length; i++)
        if (draft.texts[i].trim().isNotEmpty) (text: draft.texts[i].trim(), image: i < draft.images.length ? draft.images[i] : ''),
    ];
    final pages = slides.isEmpty ? [(text: 'This story has no words yet. Edit it to add some!', image: '')] : slides;
    final p = pages[page.clamp(0, pages.length - 1)];
    final lang = draft.language == 'English' ? 'en-US' : 'fil-PH';
    final last = page >= pages.length - 1;
    return AppPage(
      back: true,
      title: draft.title,
      subtitle: 'A story you made · ${draft.language}',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Tag([draft.character, draft.place].join(' · '), bg: const Color(0xFFFFF3D6), fg: C.navy), Tag('Page ${page + 1} of ${pages.length}', bg: const Color(0xFFFFF3D6), fg: C.navy)]),
          const SizedBox(height: 12),
          WhiteCard(
            padding: const EdgeInsets.all(18),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              if (p.image.isNotEmpty) Padding(padding: const EdgeInsets.only(bottom: 12), child: Center(child: ClipRRect(borderRadius: BorderRadius.circular(16), child: Image.memory(base64Decode(p.image), height: 200, fit: BoxFit.contain, semanticLabel: 'Picture for page ${page + 1}')))),
              ReadAloudText(text: p.text, lang: lang, rate: app.settings.speed, fontSize: textSizeFor(app.settings.textSize), highlight: app.settings.highlight),
            ]),
          ),
          const SizedBox(height: 14),
          Wrap(spacing: 10, runSpacing: 6, alignment: WrapAlignment.spaceBetween, children: [
            Btn('', icon: Icons.arrow_back, kind: BtnKind.white, onPressed: page == 0 ? null : () => setState(() => page--)),
            Btn('Edit story', icon: Icons.edit, kind: BtnKind.white, onPressed: () => context.push('/create?story=${draft.id}')),
            last ? Btn('Finish ✓', onPressed: () => context.go('/my-stories')) : Btn('▶', onPressed: () => setState(() => page++)),
          ]),
        ]),
      ),
    );
  }
}
