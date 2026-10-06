import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/content.dart';
import '../data/models.dart';
import '../learner.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';

String _greeting() {
  final h = DateTime.now().hour;
  return h < 12 ? 'Good morning' : (h < 18 ? 'Good afternoon' : 'Good evening');
}

const _months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
const _days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
String _dateLabel() {
  final d = DateTime.now();
  return '${_days[d.weekday - 1]}, ${_months[d.month - 1]} ${d.day}, ${d.year}';
}

/// Easiest stories first, in the chosen language (Bilingual stories fit both).
List<Story> starterStories(String language) {
  bool fits(Story s) => language == 'Bilingual' || s.language == language || s.language == 'Bilingual';
  final list = stories.where(fits).toList()..sort((a, b) => a.minutes != b.minutes ? a.minutes - b.minutes : a.pages.length - b.pages.length);
  return list;
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final isNew = isNewReader(app.progress);
    return AppPage(
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const SizedBox(height: 34),
        Row(children: [
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('${_greeting()}, ${app.displayName}!', style: T.head(26, weight: FontWeight.w700, height: 1.1)),
              const SizedBox(height: 4),
              Text(isNew ? 'Your reading adventure starts today!' : 'Ready for another fun reading adventure?', style: T.body(14, color: C.muted)),
              const SizedBox(height: 10),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(20), border: Border.all(color: C.orange, width: 1.5)),
                child: Text(_dateLabel(), style: T.body(12, color: const Color(0xFFB4520F), weight: FontWeight.w800)),
              ),
            ]),
          ),
          Image.asset('assets/images/teacher-1.png', width: 84, height: 84, semanticLabel: ''),
        ]),
        const SizedBox(height: 16),
        const _QuickActions(),
        const SizedBox(height: 16),
        if (isNew) const _NewReaderHome() else const _ReturningHome(),
      ]),
    );
  }
}

class _QuickActions extends StatelessWidget {
  const _QuickActions();
  @override
  Widget build(BuildContext context) => Row(children: [
        Expanded(child: _Quick(icon: Icons.photo_camera, label: 'Scan a book', color: C.blueBtn, fg: Colors.white, onTap: () => context.push('/scan'))),
        const SizedBox(width: 12),
        Expanded(child: _Quick(icon: Icons.view_in_ar, label: 'AR flashcards', color: C.orange, fg: C.ink, onTap: () => context.go('/cards'))),
      ]);
}

class _Quick extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final Color fg;
  final VoidCallback onTap;
  const _Quick({required this.icon, required this.label, required this.color, required this.fg, required this.onTap});
  @override
  Widget build(BuildContext context) => Material(
        color: color,
        borderRadius: BorderRadius.circular(22),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(22),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 14),
            child: Row(children: [
              Icon(icon, color: fg, size: 28),
              const SizedBox(width: 10),
              Expanded(child: Text(label, style: T.body(15, color: fg, weight: FontWeight.w800))),
            ]),
          ),
        ),
      );
}

class _NewReaderHome extends StatelessWidget {
  const _NewReaderHome();
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final list = starterStories(app.settings.language);
    final first = list.isEmpty ? stories.first : list.first;
    final steps = [
      (done: app.progress.isNotEmpty, icon: Icons.headphones, color: C.orange, fg: C.ink, title: 'Listen to a story', route: '/books/${first.id}/listen'),
      (done: app.lessonsDone > 0, icon: Icons.school, color: C.blueBtn, fg: Colors.white, title: 'Learn your first sound: Mm', route: '/learn'),
      (done: false, icon: Icons.star, color: C.green, fg: C.ink, title: 'Earn your first star', route: '/books/${first.id}'),
    ];
    final doneCount = steps.where((s) => s.done).length;
    return Panel(
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        WhiteCard(
          child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Welcome to BASA-KITA!', style: T.body(14, color: const Color(0xFFB4520F), weight: FontWeight.w800)),
                const SizedBox(height: 4),
                Text('Let’s read your first story together, ${app.displayName}!', style: T.head(22, weight: FontWeight.w700, height: 1.15)),
                const SizedBox(height: 12),
                Text('I want to read in:', style: T.body(14, weight: FontWeight.w800)),
                const SizedBox(height: 6),
                ScrollSeg<String>(
                  options: const ['Filipino', 'English', 'Bilingual'],
                  value: app.settings.language,
                  label: (v) => v == 'Bilingual' ? 'Both' : v,
                  onChanged: (v) => app.updateSettings((s) {
                    s.language = v;
                    s.languageChosen = true;
                  }),
                ),
                const SizedBox(height: 14),
                Btn('Start my first story', icon: Icons.play_arrow, big: true, expand: true, onPressed: () => context.push('/books/${first.id}/listen')),
                Text('“${first.title}” · ${first.minutes} min', style: T.body(13, color: C.muted, weight: FontWeight.w800)),
              ]),
            ),
          ]),
        ),
        const SizedBox(height: 18),
        Row(children: [
          Expanded(child: Text('Your first steps', style: T.head(20))),
          Text('$doneCount of ${steps.length} done', style: T.body(13, color: C.muted, weight: FontWeight.w800)),
        ]),
        const SizedBox(height: 10),
        for (final s in steps)
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: WhiteCard(
              padding: const EdgeInsets.all(12),
              onTap: () => context.go(s.route.startsWith('/books') ? s.route : s.route),
              child: Row(children: [
                Container(width: 48, height: 48, decoration: BoxDecoration(color: s.color, borderRadius: BorderRadius.circular(16)), child: Icon(s.icon, color: s.fg, size: 26)),
                const SizedBox(width: 12),
                Expanded(child: Text(s.title, style: T.body(16, weight: FontWeight.w800, color: C.navy))),
                Container(
                  width: 28, height: 28,
                  decoration: BoxDecoration(shape: BoxShape.circle, color: s.done ? C.green : Colors.transparent, border: Border.all(color: s.done ? C.green : const Color(0xFFB7C4D1), width: 2)),
                  child: s.done ? const Icon(Icons.check, color: Colors.white, size: 18) : null,
                ),
              ]),
            ),
          ),
        const SizedBox(height: 6),
        SectionTitle('Easy stories to start with', action: 'See all', onAction: () => context.go('/books')),
        SizedBox(
          height: 218,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: list.take(4).length,
            separatorBuilder: (_, _) => const SizedBox(width: 12),
            itemBuilder: (_, i) => StoryCard(list[i]),
          ),
        ),
        const SizedBox(height: 14),
        Text.rich(TextSpan(style: T.body(13, color: C.muted), children: [TextSpan(text: 'Tip for grown-ups: ', style: T.body(13, color: C.navy, weight: FontWeight.w800)), const TextSpan(text: 'sit with your child for the first story and read along together.')])),
      ]),
    );
  }
}

class _ReturningHome extends StatelessWidget {
  const _ReturningHome();
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final level = learnerLevel(app.account);
    final currentId = continueId(app.progress);
    final current = currentId == null ? null : storyById(currentId);
    final recs = recommendStories(app.account, level, app.settings.language, count: 5).where((r) => r.story.id != currentId).take(4).toList();
    return Panel(
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        if (current != null)
          WhiteCard(
            color: C.cream,
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Continue Reading', style: T.body(14, color: const Color(0xFFB4520F), weight: FontWeight.w800)),
              const SizedBox(height: 4),
              Text(current.title, style: T.head(24, weight: FontWeight.w700)),
              const SizedBox(height: 4),
              Text('You’re ${percentRead(current, app.progress[current.id])}% through this story.', style: T.body(13, color: C.muted)),
              const SizedBox(height: 8),
              Bar(percentRead(current, app.progress[current.id])),
              const SizedBox(height: 14),
              Btn('Continue', icon: Icons.play_arrow, expand: true, onPressed: () => context.push('/books/${current.id}/normal')),
            ]),
          )
        else
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Pick a new story to start!', style: T.head(20)),
              const SizedBox(height: 10),
              Btn('Explore Storybooks', onPressed: () => context.go('/books')),
            ]),
          ),
        const SizedBox(height: 16),
        Row(children: [
          Expanded(child: Text('Recommended for you', style: T.head(20))),
          GestureDetector(onTap: () => context.push('/progress'), child: Tag('${level.name} Reader', bg: levelBg(level.name), fg: levelFg(level.name))),
        ]),
        const SizedBox(height: 10),
        if (recs.isEmpty)
          Text('You’ve finished every story! Try writing your own in Create.', style: T.body(14, color: C.muted))
        else
          SizedBox(
            height: 218,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: recs.length,
              separatorBuilder: (_, _) => const SizedBox(width: 12),
              itemBuilder: (_, i) => StoryCard(recs[i].story, note: recs[i].reason),
            ),
          ),
      ]),
    );
  }
}
