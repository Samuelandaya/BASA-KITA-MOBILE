import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/content.dart';
import '../data/models.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';

bool _matches(Story s, String filter, List<String> saved) => filter == 'Saved'
    ? saved.contains(s.id)
    : filter == 'All' || s.language == filter || s.language == 'Bilingual';

class StorybooksScreen extends StatefulWidget {
  const StorybooksScreen({super.key});
  @override
  State<StorybooksScreen> createState() => _StorybooksScreenState();
}

class _StorybooksScreenState extends State<StorybooksScreen> {
  String query = '';
  String filter = 'All';

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final list = stories.where((s) => _matches(s, filter, app.saved) && s.title.toLowerCase().contains(query.trim().toLowerCase())).toList();
    return AppPage(
      banner: 'Explore Storybooks',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Ready for another fun reading adventure?', style: T.body(15, color: C.muted)),
          const SizedBox(height: 12),
          // Search bar with the search-by-picture icon (opens the camera scanner on the phone).
          Container(
            height: 58,
            padding: const EdgeInsets.only(left: 18, right: 6),
            decoration: BoxDecoration(color: const Color(0xFFFFFAF0), borderRadius: BorderRadius.circular(32), border: Border.all(color: const Color(0xFFD9CFBD), width: 1.5), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 10, offset: Offset(0, 5))]),
            child: Row(children: [
              const Icon(Icons.search, color: Color(0xFF7A8794), size: 26),
              const SizedBox(width: 10),
              Expanded(
                child: TextField(
                  onChanged: (v) => setState(() => query = v),
                  style: T.body(16),
                  decoration: InputDecoration(hintText: 'Search stories...', filled: false, border: InputBorder.none, enabledBorder: InputBorder.none, focusedBorder: InputBorder.none, contentPadding: EdgeInsets.zero, hintStyle: T.body(16, color: C.muted)),
                ),
              ),
              IconButton(onPressed: () => context.push('/scan'), tooltip: 'Search by picture: scan a book', icon: const Icon(Icons.center_focus_strong_outlined, color: Color(0xFF7A8794), size: 28)),
            ]),
          ),
          const SizedBox(height: 12),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(children: [
              for (final f in const ['All', 'Filipino', 'English', 'Saved']) ...[
                GestureDetector(
                  onTap: () => setState(() => filter = f),
                  child: Container(
                    height: 44,
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    alignment: Alignment.center,
                    decoration: BoxDecoration(color: filter == f ? C.orange : Colors.white, borderRadius: BorderRadius.circular(26), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 8, offset: Offset(0, 4))]),
                    child: Row(mainAxisSize: MainAxisSize.min, children: [
                      if (f == 'Saved') const Padding(padding: EdgeInsets.only(right: 6), child: Icon(Icons.favorite, size: 16, color: C.ink)),
                      Text(f.toUpperCase(), style: T.pill(16)),
                    ]),
                  ),
                ),
                const SizedBox(width: 10),
              ],
            ]),
          ),
          const SizedBox(height: 16),
          if (list.isEmpty)
            Padding(
              padding: const EdgeInsets.all(30),
              child: Center(child: Text(filter == 'Saved' && app.saved.isEmpty ? 'Tap the heart on a story to save it here.' : 'No stories match “$query”. Try another word!', textAlign: TextAlign.center, style: T.body(15, color: C.muted, weight: FontWeight.w800))),
            )
          else
            for (final s in list) Padding(padding: const EdgeInsets.only(bottom: 14), child: BookRow(s)),
        ]),
      ),
    );
  }
}

class StoryDetailsScreen extends StatelessWidget {
  final String id;
  const StoryDetailsScreen({super.key, required this.id});

  @override
  Widget build(BuildContext context) {
    final story = storyById(id);
    if (story == null) return const _Missing();
    final app = context.watch<AppState>();
    final entry = app.progress[id];
    final pct = percentRead(story, entry);
    return AppPage(
      back: true,
      banner: story.title,
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Panel(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [Tag(story.language), const SizedBox(width: 6), if (story.shownLevel != story.language) Tag(story.shownLevel, bg: C.orange), const Spacer(), SaveHeart(story)]),
            const SizedBox(height: 12),
            StoryCover(story, height: 190, radius: 28),
            const SizedBox(height: 12),
            Text(story.blurb, style: T.body(16, weight: FontWeight.w800, height: 1.35)),
            const SizedBox(height: 10),
            Wrap(spacing: 8, runSpacing: 8, children: [
              _Meta(Icons.schedule, '${story.minutes} minutes', const Color(0xFFE2F0FB)),
              _Meta(Icons.favorite_border, story.theme, const Color(0xFFDDF3E6)),
            ]),
            if (entry != null) ...[
              const SizedBox(height: 12),
              Text(entry.finished ? 'You finished this story!' : 'You’re $pct% through · page ${entry.page.clamp(1, story.pages.length)} of ${story.pages.length}', style: T.body(14, weight: FontWeight.w800, color: C.navy)),
              const SizedBox(height: 6),
              Bar(pct),
              if (!entry.finished && entry.page > 1) ...[
                const SizedBox(height: 10),
                Btn('Continue Reading', icon: Icons.play_arrow, kind: BtnKind.white, onPressed: () => context.push('/books/$id/normal')),
              ],
            ],
          ]),
        ),
        const SizedBox(height: 16),
        Panel(
          child: Column(children: [
            Row(children: [
              Expanded(child: Text('Choose your Reading Mode', style: T.head(22, weight: FontWeight.w700, height: 1.1))),
              ClipOval(child: Container(width: 54, height: 54, color: C.navy, child: const Icon(Icons.menu_book, color: C.yellow2, size: 30))),
            ]),
            const SizedBox(height: 14),
            _ModeCard(color1: const Color(0xFFFFB27A), color2: C.orange, icon: Icons.headphones, text: 'Let the AI read the whole story aloud while you follow along.', button: 'Listen to Story', onTap: () => context.push('/books/$id/listen')),
            const SizedBox(height: 12),
            _ModeCard(color1: const Color(0xFF9CCBF5), color2: const Color(0xFF1F7FE0), icon: Icons.menu_book, text: story.language == 'English' ? 'Learn and Practice Reading with English Phonics' : 'Learn and Practice Reading with the Marungko Method', button: 'Start Reading', onTap: () => context.push('/books/$id/read')),
            const SizedBox(height: 14),
            Text('More ways to read:', style: T.body(14, weight: FontWeight.w800, color: C.navy)),
            const SizedBox(height: 8),
            Wrap(spacing: 10, runSpacing: 6, alignment: WrapAlignment.center, children: [
              Btn('Read With Me', icon: Icons.mic, kind: BtnKind.white, onPressed: () => context.push('/books/$id/with-me')),
              Btn('Read on My Own', icon: Icons.menu_book_outlined, kind: BtnKind.white, onPressed: () => context.push('/books/$id/normal')),
            ]),
          ]),
        ),
      ]),
    );
  }
}

class _Meta extends StatelessWidget {
  final IconData icon;
  final String text;
  final Color bg;
  const _Meta(this.icon, this.text, this.bg);
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(20)),
        child: Row(mainAxisSize: MainAxisSize.min, children: [Icon(icon, size: 15, color: C.navy), const SizedBox(width: 6), Text(text.toUpperCase(), style: T.body(11, weight: FontWeight.w800, color: C.navy).copyWith(letterSpacing: .5))]),
      );
}

class _ModeCard extends StatelessWidget {
  final Color color1;
  final Color color2;
  final IconData icon;
  final String text;
  final String button;
  final VoidCallback onTap;
  const _ModeCard({required this.color1, required this.color2, required this.icon, required this.text, required this.button, required this.onTap});
  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(18, 18, 18, 14),
        decoration: BoxDecoration(borderRadius: BorderRadius.circular(32), border: Border.all(color: Colors.white, width: 3), gradient: LinearGradient(begin: Alignment.topCenter, end: Alignment.bottomCenter, colors: [color1, color2])),
        child: Column(children: [
          Container(width: 76, height: 76, decoration: BoxDecoration(color: Colors.white, shape: BoxShape.circle, boxShadow: [BoxShadow(color: Colors.white.withValues(alpha: .4), spreadRadius: 8)]), child: Icon(icon, size: 40, color: color2)),
          const SizedBox(height: 14),
          Text(text, textAlign: TextAlign.center, style: T.body(15, color: C.ink, weight: FontWeight.w700, height: 1.3)),
          const SizedBox(height: 12),
          Material(
            color: C.yellow,
            borderRadius: BorderRadius.circular(30),
            child: InkWell(onTap: onTap, borderRadius: BorderRadius.circular(30), child: Container(padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14), child: Text(button.toUpperCase(), style: T.pill(18)))),
          ),
        ]),
      );
}

class _Missing extends StatelessWidget {
  const _Missing();
  @override
  Widget build(BuildContext context) => AppPage(back: true, title: 'Story not found', child: Btn('Back to Storybooks', onPressed: () => context.go('/books')));
}
