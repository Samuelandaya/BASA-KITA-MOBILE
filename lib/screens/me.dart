import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/content.dart';
import '../data/models.dart';
import '../learner.dart';
import '../state/app_state.dart';
import '../state/models.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/reading.dart';

class Avatar extends StatelessWidget {
  final double size;
  const Avatar({super.key, this.size = 72});
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    return Container(
      width: size, height: size, alignment: Alignment.center,
      decoration: BoxDecoration(color: Color(app.avatarColor), shape: BoxShape.circle, border: Border.all(color: Colors.white, width: 4), boxShadow: const [BoxShadow(color: C.yellow, spreadRadius: 3)]),
      child: Text(app.displayName[0].toUpperCase(), style: T.fun(size * .5, color: C.ink)),
    );
  }
}

// ------------------------------------------------------------------ Me tab
class MeScreen extends StatelessWidget {
  const MeScreen({super.key});
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final level = learnerLevel(app.account);
    final items = [
      (Icons.photo_camera_outlined, 'Scan a Book', '/scan'),
      (Icons.edit_outlined, 'Create a Story', '/create'),
      (Icons.trending_up, 'My Progress', '/progress'),
      (Icons.library_books_outlined, 'My Stories', '/my-stories'),
      (Icons.settings_outlined, 'Settings', '/settings'),
      (Icons.person_outline, 'My Profile', '/profile'),
      (Icons.help_outline, 'Help', '/help'),
    ];
    return AppPage(
      title: 'Me',
      child: Column(children: [
        WhiteCard(
          child: Row(children: [
            const Avatar(),
            const SizedBox(width: 16),
            Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text([app.user?.firstName, app.user?.lastName].where((e) => e != null && e.isNotEmpty).join(' '), style: T.head(22, weight: FontWeight.w700)),
              Text('@${app.user?.username ?? ''}', style: T.body(14, color: C.muted, weight: FontWeight.w800)),
              const SizedBox(height: 6),
              Wrap(spacing: 6, children: [const Tag('Kindergarten', bg: C.cream2), Tag('${level.name} reader', bg: levelBg(level.name), fg: levelFg(level.name))]),
            ])),
          ]),
        ),
        const SizedBox(height: 14),
        for (final it in items)
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: WhiteCard(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
              onTap: () => context.push(it.$3),
              child: Row(children: [Icon(it.$1, color: C.navy), const SizedBox(width: 14), Expanded(child: Text(it.$2, style: T.body(16, weight: FontWeight.w800, color: C.navy))), const Icon(Icons.chevron_right, color: C.muted)]),
            ),
          ),
        const SizedBox(height: 6),
        Btn('Logout', icon: Icons.logout, kind: BtnKind.white, expand: true, onPressed: () => _confirmLogout(context)),
      ]),
    );
  }
}

Future<void> _confirmLogout(BuildContext context) => showDialog<void>(
      context: context,
      builder: (c) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        title: Text('Leaving already?', style: T.head(22), textAlign: TextAlign.center),
        content: Text('Your reading progress is saved. See you next time!', style: T.body(15, color: C.muted), textAlign: TextAlign.center),
        actionsAlignment: MainAxisAlignment.center,
        actions: [
          Btn('Keep reading', kind: BtnKind.white, onPressed: () => Navigator.pop(c)),
          Btn('Log out', onPressed: () {
            Navigator.pop(c);
            context.read<AppState>().logout();
          }),
        ],
      ),
    );

Future<void> confirmLogout(BuildContext context) => _confirmLogout(context);

// ------------------------------------------------------------------ Progress
class ProgressScreen extends StatelessWidget {
  const ProgressScreen({super.key});
  static const _week = [('Mon', 8), ('Tue', 10), ('Wed', 5), ('Thu', 12), ('Fri', 13), ('Sat', 0), ('Sun', 0)];

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final a = app.account;
    final level = learnerLevel(a);
    final sk = level.skills;
    final finished = stories.where((s) => a.progress[s.id]?.finished == true).toList();
    final skills = [('Letter sounds', sk.letterSounds), ('English pronunciation', sk.english), ('Filipino word reading accuracy', sk.filipino), ('Understanding the story', sk.understanding)];
    final badges = [
      ('First Story', Icons.menu_book, C.blueBtn, Colors.white, finished.isNotEmpty),
      ('Sharing Star', Icons.star, C.orange, C.ink, a.progress['mia-and-the-red-ball']?.finished == true),
      ('Brave Reader', Icons.mic, C.green, C.ink, finished.length >= 3),
      ('7-Day Streak', Icons.lock, const Color(0xFFECE8DF), C.muted, false),
    ];
    final maxMin = _week.map((e) => e.$2).reduce((x, y) => x > y ? x : y);
    final today = DateTime.now().weekday - 1;
    return AppPage(
      back: true,
      title: 'My Progress',
      subtitle: app.sampleData ? 'Look how much you’ve grown!' : 'Every story you read helps you grow!',
      child: Column(children: [
        const LevelCard(),
        const SizedBox(height: 14),
        if (!app.sampleData)
          WhiteCard(
            child: Row(children: [
              Container(width: 60, height: 60, decoration: const BoxDecoration(color: C.yellow, shape: BoxShape.circle), child: const Icon(Icons.star, size: 32, color: Color(0xFFB4520F))),
              const SizedBox(width: 14),
              Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(finished.isNotEmpty ? 'You’ve finished ${finished.length} ${finished.length == 1 ? 'story' : 'stories'}!' : 'Your stars will appear here!', style: T.head(18)),
                Text('${finished.isNotEmpty ? 'Keep reading to fill your chart.' : 'Finish your first story to start your progress chart.'}${a.lessonsDone > 0 ? ' You know ${a.lessonsDone} letter ${a.lessonsDone == 1 ? 'sound' : 'sounds'}!' : ''}', style: T.body(13, color: C.muted)),
              ])),
            ]),
          )
        else ...[
          Row(children: [
            for (final (label, value) in [('Stories finished', '${finished.length}'), ('Minutes this week', '${_week.fold<int>(0, (n, e) => n + e.$2)}'), ('Letter sounds', '${a.lessonsDone}/${lessons.length}')])
              Expanded(child: Padding(padding: const EdgeInsets.symmetric(horizontal: 4), child: WhiteCard(padding: const EdgeInsets.all(12), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(label, style: T.body(11, color: C.muted, weight: FontWeight.w800)), Text(value, style: T.head(26, weight: FontWeight.w700))])))),
          ]),
          const SizedBox(height: 14),
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Minutes read this week', style: T.head(18)),
              const SizedBox(height: 12),
              SizedBox(
                height: 150,
                child: Row(crossAxisAlignment: CrossAxisAlignment.end, children: [
                  for (final (i, e) in _week.indexed)
                    Expanded(child: Column(mainAxisAlignment: MainAxisAlignment.end, children: [
                      Text('${e.$2}', style: T.body(11, weight: FontWeight.w800)),
                      const SizedBox(height: 4),
                      Container(width: 26, height: e.$2 == 0 ? 4 : e.$2 / maxMin * 100, decoration: BoxDecoration(color: e.$2 == 0 ? const Color(0xFFC9D5DF) : (i == today ? C.orange : C.blueBtn), borderRadius: BorderRadius.circular(8))),
                      const SizedBox(height: 4),
                      Text(e.$1, style: T.body(11, color: C.muted, weight: FontWeight.w800)),
                    ])),
                ]),
              ),
            ]),
          ),
        ],
        const SizedBox(height: 14),
        WhiteCard(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Reading skills', style: T.head(18)),
            const SizedBox(height: 10),
            for (final s in skills)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Row(children: [Expanded(child: Text(s.$1, style: T.body(14, weight: FontWeight.w800))), Text(s.$2 == null ? 'Not yet' : '${s.$2}%', style: T.body(14, weight: FontWeight.w800))]),
                  const SizedBox(height: 5),
                  Bar(s.$2 ?? 0, color: C.green, height: 12),
                ]),
              ),
          ]),
        ),
        const SizedBox(height: 14),
        WhiteCard(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(app.sampleData ? 'My badges' : 'Badges to earn', style: T.head(18)),
            const SizedBox(height: 12),
            Wrap(spacing: 14, runSpacing: 12, children: [
              for (final b in badges)
                SizedBox(
                  width: 74,
                  child: Column(children: [
                    Container(width: 60, height: 60, decoration: BoxDecoration(color: b.$5 ? b.$3 : const Color(0xFFECE8DF), shape: BoxShape.circle), child: Icon(b.$5 ? b.$2 : Icons.lock, color: b.$5 ? b.$4 : C.muted, size: 28)),
                    const SizedBox(height: 4),
                    Text(b.$1, textAlign: TextAlign.center, style: T.body(11, weight: FontWeight.w800, color: b.$5 ? C.navy : C.muted)),
                  ]),
                ),
            ]),
          ]),
        ),
      ]),
    );
  }
}

// ------------------------------------------------------------------ My Stories
String _when(int ts) {
  final days = DateTime.now().difference(DateTime.fromMillisecondsSinceEpoch(ts)).inDays;
  if (days < 1) return 'Today';
  if (days == 1) return 'Yesterday';
  if (days < 7) return '$days days ago';
  final d = DateTime.fromMillisecondsSinceEpoch(ts);
  return '${const ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][d.month - 1]} ${d.day}';
}

class MyStoriesScreen extends StatefulWidget {
  const MyStoriesScreen({super.key});
  @override
  State<MyStoriesScreen> createState() => _MyStoriesState();
}

class _MyStoriesState extends State<MyStoriesScreen> {
  int tab = 0;
  static const _keys = ['reading', 'saved', 'history', 'finished', 'made'];
  static const _labels = ['Reading now', 'Saved', 'History', 'Finished', 'Made by me'];
  static const _empty = [
    'Stories you start will wait for you here.',
    'Tap the heart on a story to save it here.',
    'Every story you open is added to your reading history.',
    'Finished stories and your stars will show up here.',
    'Stories you write will be saved here.',
  ];

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final p = app.progress;
    final history = historyIds(p).map(storyById).whereType<Story>().toList();
    final reading = history.where((s) => !p[s.id]!.finished).toList();
    final savedStories = app.saved.map(storyById).whereType<Story>().toList();
    final finished = stories.where((s) => p[s.id]?.finished == true).toList();
    final counts = [reading.length, savedStories.length, history.length, finished.length, app.drafts.length];

    Widget body;
    if (counts[tab] == 0) {
      body = Padding(padding: const EdgeInsets.all(20), child: Center(child: Text(_empty[tab], textAlign: TextAlign.center, style: T.body(15, color: C.muted, weight: FontWeight.w800))));
    } else {
      switch (_keys[tab]) {
        case 'history':
          body = Column(children: [
            for (final s in history)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: WhiteCard(
                  padding: const EdgeInsets.all(10),
                  onTap: () => context.push('/books/${s.id}'),
                  child: Row(children: [
                    SizedBox(width: 60, height: 60, child: StoryCover(s, radius: 14)),
                    const SizedBox(width: 12),
                    Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(s.title, style: T.head(16)), const SizedBox(height: 4), Bar(percentRead(s, p[s.id]), height: 7), Text('${p[s.id]!.finished ? 'Finished' : '${percentRead(s, p[s.id])}% read'} · ${s.language}', style: T.body(12, color: C.muted, weight: FontWeight.w700))])),
                    const SizedBox(width: 8),
                    Text(_when(p[s.id]!.lastRead ?? 0), style: T.body(11, color: C.muted, weight: FontWeight.w800)),
                  ]),
                ),
              ),
          ]);
        case 'made':
          body = Column(children: [
            for (final d in app.drafts)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: WhiteCard(
                  onTap: () => context.push('/made/${d.id}'),
                  child: Row(children: [
                    Container(width: 60, height: 60, alignment: Alignment.center, decoration: BoxDecoration(color: C.yellow, borderRadius: BorderRadius.circular(14)), child: Text('Aa', style: T.head(24, weight: FontWeight.w700))),
                    const SizedBox(width: 12),
                    Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(d.title, style: T.head(17)), Text('${d.language} · ${d.pages} pages', style: T.body(12, color: C.muted, weight: FontWeight.w700))])),
                    Btn('Edit', kind: BtnKind.white, onPressed: () => context.push('/create?story=${d.id}')),
                  ]),
                ),
              ),
          ]);
        default:
          final list = switch (_keys[tab]) { 'reading' => reading, 'saved' => savedStories, _ => finished };
          body = Column(children: [for (final s in list) Padding(padding: const EdgeInsets.only(bottom: 14), child: BookRow(s))]);
      }
    }

    return AppPage(
      back: true,
      title: 'My Stories',
      subtitle: 'Stories you’re reading, saved, finished and made, plus your reading history.',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          ScrollSeg<int>(options: const [0, 1, 2, 3, 4], value: tab, label: (i) => '${_labels[i]} (${counts[i]})', onChanged: (i) => setState(() => tab = i)),
          const SizedBox(height: 14),
          body,
          if (tab == 4) Btn('Write a new story', icon: Icons.add, kind: BtnKind.white, onPressed: () => context.push('/create')),
        ]),
      ),
    );
  }
}

// ------------------------------------------------------------------ Profile
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});
  @override
  State<ProfileScreen> createState() => _ProfileState();
}

class _ProfileState extends State<ProfileScreen> {
  late final User u = context.read<AppState>().user!;
  late final first = TextEditingController(text: u.firstName);
  late final last = TextEditingController(text: u.lastName);
  late final username = TextEditingController(text: u.username);
  late final email = TextEditingController(text: u.email);
  late final school = TextEditingController(text: u.school);
  late final teacher = TextEditingController(text: u.teacher);
  bool saved = false;
  static const _colors = [(0xFF7FB3E0, 'Blue'), (0xFFFFB27A, 'Orange'), (0xFF8FD6AE, 'Green'), (0xFFF6B6C8, 'Pink'), (0xFFFFE588, 'Yellow')];

  Widget _field(String label, TextEditingController c, {TextInputType? type}) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label, style: T.body(14, weight: FontWeight.w800)),
          const SizedBox(height: 6),
          TextField(controller: c, keyboardType: type, style: T.body(16), onChanged: (_) => setState(() => saved = false)),
        ]),
      );

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final level = learnerLevel(app.account);
    return AppPage(
      back: true,
      title: 'My Profile',
      subtitle: 'This is you, ${first.text.isEmpty ? 'reader' : first.text}!',
      child: Column(children: [
        WhiteCard(
          child: Column(children: [
            const Avatar(size: 104),
            const SizedBox(height: 10),
            Tag('${level.name} reader', bg: levelBg(level.name), fg: levelFg(level.name)),
            const SizedBox(height: 12),
            Text('Choose your avatar color', style: T.body(14, weight: FontWeight.w800)),
            const SizedBox(height: 8),
            Wrap(spacing: 10, children: [
              for (final c in _colors)
                GestureDetector(
                  onTap: () => app.setAvatarColor(c.$1),
                  child: Semantics(label: '${c.$2} avatar', button: true, child: Container(width: 46, height: 46, decoration: BoxDecoration(color: Color(c.$1), shape: BoxShape.circle, border: Border.all(color: app.avatarColor == c.$1 ? C.navy : Colors.transparent, width: 4)))),
                ),
            ]),
          ]),
        ),
        const SizedBox(height: 14),
        WhiteCard(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('About me', style: T.head(20)),
            const SizedBox(height: 12),
            _field('First name', first),
            _field('Last name', last),
            _field('Username', username),
            _field('Parent’s email', email, type: TextInputType.emailAddress),
            _field('School', school),
            _field('Teacher', teacher),
            Row(mainAxisAlignment: MainAxisAlignment.end, children: [
              if (saved) Padding(padding: const EdgeInsets.only(right: 12), child: Text('Saved!', style: T.body(14, color: C.greenText, weight: FontWeight.w800))),
              Btn('Save changes', onPressed: () {
                app.updateUser((x) {
                  x.firstName = first.text;
                  x.lastName = last.text;
                  x.username = username.text;
                  x.email = email.text;
                  x.school = school.text;
                  x.teacher = teacher.text;
                });
                setState(() => saved = true);
              }),
            ]),
          ]),
        ),
      ]),
    );
  }
}

// ------------------------------------------------------------------ Help
class HelpScreen extends StatefulWidget {
  const HelpScreen({super.key});
  @override
  State<HelpScreen> createState() => _HelpState();
}

class _HelpState extends State<HelpScreen> {
  String q = '';
  static const _faq = [
    ('How do I start reading a story?', 'Go to Books, tap a story, then choose Listen to Story (the story is read to you) or Start Reading (you practice reading with Marungko or phonics support).'),
    ('What is the Marungko Method?', 'A Filipino phonics approach that teaches letter sounds in a set order (M, A, S, I, O…), then blends them into syllables, words and sentences.'),
    ('The microphone isn’t working.', 'Allow microphone access when your phone asks. If you tapped “Don’t allow”, open your phone’s Settings, find BASA-KITA and turn the microphone on.'),
    ('The story isn’t being read aloud.', 'Check that your phone volume is up and that Story narration is on in Settings.'),
    ('How do I see a flashcard in AR?', 'Open Cards, choose a card, then tap the AR button on the 3D object and point your camera at the floor or a table.'),
    ('How do I scan a book?', 'Tap the camera icon in the Books search bar, take a photo of one page, then tap Read this page.'),
    ('How do I change the story language?', 'Open Me → Settings → Reading → Story language and choose Filipino, English or Bilingual.'),
    ('I forgot my password.', 'On the login screen, tap Forgot Password? and we’ll email you a reset link.'),
  ];

  @override
  Widget build(BuildContext context) {
    final list = _faq.where((e) => '${e.$1} ${e.$2}'.toLowerCase().contains(q.trim().toLowerCase())).toList();
    return AppPage(
      back: true,
      title: 'Need Help?',
      subtitle: 'Find answers for kids, parents and teachers.',
      child: Panel(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          TextField(onChanged: (v) => setState(() => q = v), style: T.body(16), decoration: InputDecoration(hintText: 'Search help topics...', prefixIcon: const Icon(Icons.search), border: OutlineInputBorder(borderRadius: BorderRadius.circular(26)), enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(26), borderSide: const BorderSide(color: Color(0xFFD9E2EC))))),
          const SizedBox(height: 12),
          if (list.isEmpty)
            Padding(padding: const EdgeInsets.all(16), child: Text('No answers for “$q”. Try another word.', style: T.body(15, color: C.muted, weight: FontWeight.w800)))
          else
            WhiteCard(
              padding: const EdgeInsets.symmetric(horizontal: 6),
              child: Column(children: [
                for (final (i, e) in list.indexed)
                  Theme(
                    data: Theme.of(context).copyWith(dividerColor: i == list.length - 1 ? Colors.transparent : C.cardBorder),
                    child: ExpansionTile(initiallyExpanded: i == 0 && q.isEmpty, tilePadding: const EdgeInsets.symmetric(horizontal: 12), childrenPadding: const EdgeInsets.fromLTRB(12, 0, 12, 14), title: Text(e.$1, style: T.body(15, weight: FontWeight.w800, color: C.navy)), expandedAlignment: Alignment.centerLeft, children: [Text(e.$2, style: T.body(14, color: C.muted, height: 1.4))]),
                  ),
              ]),
            ),
          const SizedBox(height: 14),
          WhiteCard(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Still need help?', style: T.head(18)),
              const SizedBox(height: 4),
              Text('Grown-ups can message the BASA-KITA team or the learner’s teacher.', style: T.body(14, color: C.muted)),
            ]),
          ),
        ]),
      ),
    );
  }
}
