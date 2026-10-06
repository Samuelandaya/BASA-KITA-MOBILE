import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/content.dart';
import '../learner.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import 'me.dart' show confirmLogout;

class _Learner {
  final String name;
  final String level;
  final int done;
  final int pct;
  final int? en;
  final int? fil;
  final String last;
  final String tag; // '' | this device | new
  const _Learner(this.name, this.level, this.done, this.pct, this.en, this.fil, this.last, [this.tag = '']);
}

const _sample = [
  _Learner('Mia Santos', 'Growing', 9, 82, 84, 90, 'Today'),
  _Learner('Paolo Reyes', 'Beginner', 3, 34, 52, 61, '3 days ago'),
  _Learner('Liza Garcia', 'Confident', 11, 91, 93, 95, 'Yesterday'),
  _Learner('Ben Aquino', 'Beginner', 4, 47, 66, null, 'Yesterday'),
  _Learner('Ana Villanueva', 'Growing', 7, 58, 71, 80, '2 days ago'),
];

String _lastActive(int? ts) {
  if (ts == null) return 'Not yet';
  final days = DateTime.now().difference(DateTime.fromMillisecondsSinceEpoch(ts)).inDays;
  return days < 1 ? 'Today' : (days == 1 ? 'Yesterday' : '$days days ago');
}

/// Teacher view. It works best on a tablet; on a phone it is a compact list of the same data.
class AdminScreen extends StatefulWidget {
  const AdminScreen({super.key});
  @override
  State<AdminScreen> createState() => _AdminState();
}

class _AdminState extends State<AdminScreen> {
  static const _key = 'basa-kita:admin:v1';
  int view = 0;
  String q = '';
  String levelFilter = 'All';
  List<Map<String, dynamic>> addedLearners = [];
  List<Map<String, dynamic>> addedBooks = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    if (raw == null) return;
    try {
      final j = jsonDecode(raw) as Map<String, dynamic>;
      setState(() {
        addedLearners = (j['learners'] as List? ?? []).map((e) => Map<String, dynamic>.from(e as Map)).toList();
        addedBooks = (j['books'] as List? ?? []).map((e) => Map<String, dynamic>.from(e as Map)).toList();
      });
    } catch (_) {}
  }

  Future<void> _save() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, jsonEncode({'learners': addedLearners, 'books': addedBooks}));
  }

  List<_Learner> learners(AppState app) {
    final onDevice = <_Learner>[];
    app.accounts.forEach((email, a) {
      if (email.startsWith('admin')) return;
      final lvl = learnerLevel(a);
      final pct = stories.isEmpty ? 0 : (stories.fold<int>(0, (n, s) => n + percentRead(s, a.progress[s.id])) / stories.length).round();
      final recent = historyIds(a.progress).firstOrNull;
      final handle = email.split('@').first.replaceAll(RegExp(r'[._-]+'), ' ');
      onDevice.add(_Learner(handle.isEmpty ? email : handle[0].toUpperCase() + handle.substring(1), lvl.name, lvl.skills.finished, pct, lvl.skills.english, lvl.skills.filipino, _lastActive(recent == null ? null : a.progress[recent]!.lastRead), 'this device'));
    });
    return [
      for (final l in addedLearners) _Learner(l['name'] as String, 'Beginner', 0, 0, null, null, 'Not yet', 'new'),
      ...onDevice,
      ..._sample,
    ];
  }

  Future<void> _addLearner() async {
    final name = TextEditingController();
    final email = TextEditingController();
    String error = '';
    await showDialog<void>(
      context: context,
      builder: (c) => StatefulBuilder(
        builder: (c, setD) => AlertDialog(
          backgroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
          title: Text('Add learner', style: T.head(22)),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            TextField(controller: name, decoration: const InputDecoration(labelText: 'Learner’s name')),
            const SizedBox(height: 10),
            TextField(controller: email, keyboardType: TextInputType.emailAddress, decoration: const InputDecoration(labelText: 'Parent’s email')),
            if (error.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(error, style: T.body(13, color: C.red, weight: FontWeight.w800))),
          ]),
          actions: [
            Btn('Cancel', kind: BtnKind.white, onPressed: () => Navigator.pop(c)),
            Btn('Add learner', onPressed: () {
              if (name.text.trim().isEmpty) return setD(() => error = 'Please enter the learner’s name.');
              if (!RegExp(r'^\S+@\S+\.\S+$').hasMatch(email.text.trim())) return setD(() => error = 'Please enter a valid parent email.');
              setState(() => addedLearners.insert(0, {'name': name.text.trim(), 'email': email.text.trim()}));
              _save();
              Navigator.pop(c);
            }),
          ],
        ),
      ),
    );
  }

  Future<void> _addBook() async {
    final title = TextEditingController();
    String language = 'Filipino';
    String error = '';
    await showDialog<void>(
      context: context,
      builder: (c) => StatefulBuilder(
        builder: (c, setD) => AlertDialog(
          backgroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
          title: Text('Add storybook', style: T.head(22)),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            TextField(controller: title, decoration: const InputDecoration(labelText: 'Title')),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(initialValue: language, items: const [DropdownMenuItem(value: 'Filipino', child: Text('Filipino')), DropdownMenuItem(value: 'English', child: Text('English')), DropdownMenuItem(value: 'Bilingual', child: Text('Bilingual'))], onChanged: (v) => setD(() => language = v ?? language)),
            if (error.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(error, style: T.body(13, color: C.red, weight: FontWeight.w800))),
          ]),
          actions: [
            Btn('Cancel', kind: BtnKind.white, onPressed: () => Navigator.pop(c)),
            Btn('Add storybook', onPressed: () {
              if (title.text.trim().isEmpty) return setD(() => error = 'Please enter the storybook title.');
              setState(() => addedBooks.insert(0, {'title': title.text.trim(), 'language': language}));
              _save();
              Navigator.pop(c);
            }),
          ],
        ),
      ),
    );
  }

  Widget _learnerCard(_Learner l) => Padding(
        padding: const EdgeInsets.only(bottom: 10),
        child: WhiteCard(
          padding: const EdgeInsets.all(14),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Expanded(child: Text.rich(TextSpan(children: [TextSpan(text: l.name, style: T.head(17)), if (l.tag.isNotEmpty) TextSpan(text: ' · ${l.tag}', style: T.body(12, color: C.muted))]))),
              Tag(l.level, bg: levelBg(l.level), fg: levelFg(l.level)),
            ]),
            const SizedBox(height: 8),
            Row(children: [Expanded(child: Bar(l.pct, color: l.pct < 40 ? C.red : C.blueBtn, height: 8)), const SizedBox(width: 8), Text('${l.pct}%', style: T.body(13, weight: FontWeight.w800))]),
            const SizedBox(height: 6),
            Text('${l.done} stories · English ${l.en == null ? '—' : '${l.en}%'} · Filipino ${l.fil == null ? '—' : '${l.fil}%'} · ${l.last}', style: T.body(12, color: C.muted, weight: FontWeight.w700)),
          ]),
        ),
      );

  Widget _stats(List<_Learner> all, int books) {
    final avg = all.isEmpty ? 0 : (all.fold<int>(0, (n, l) => n + l.pct) / all.length).round();
    final support = all.where((l) => l.pct < 40 && l.last != 'Not yet').length;
    final active = all.where((l) => l.last == 'Today' || l.last == 'Yesterday').length;
    Widget tile(String label, String value, String note, Color noteColor) => Expanded(child: Padding(padding: const EdgeInsets.all(4), child: WhiteCard(padding: const EdgeInsets.all(12), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(label, style: T.body(11, color: C.muted, weight: FontWeight.w800)), Text(value, style: T.head(28, weight: FontWeight.w700)), Text(note, style: T.body(11, color: noteColor, weight: FontWeight.w700))]))));
    return Column(children: [
      Row(children: [tile('Learners', '${all.length}', '$active active in 2 days', C.greenText), tile('Storybooks', '$books', 'In the library', C.muted)]),
      Row(children: [tile('Avg. progress', '$avg%', 'Across all learners', C.muted), tile('Need support', '$support', 'Below 40% progress', const Color(0xFFB5442A))]),
    ]);
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final all = learners(app);
    final books = stories.length + addedBooks.length;
    final rows = all.where((l) => l.name.toLowerCase().contains(q.trim().toLowerCase()) && (levelFilter == 'All' || l.level == levelFilter)).toList();
    const labels = ['Overview', 'Learners', 'Books', 'Reports', 'Account'];

    Widget body;
    switch (view) {
      case 1:
        body = Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          ScrollSeg<String>(options: const ['All', ...levelNames], value: levelFilter, label: (v) => v, onChanged: (v) => setState(() => levelFilter = v)),
          const SizedBox(height: 10),
          TextField(onChanged: (v) => setState(() => q = v), decoration: const InputDecoration(hintText: 'Search learners...', prefixIcon: Icon(Icons.search))),
          const SizedBox(height: 12),
          if (rows.isEmpty) Text('No learners match.', style: T.body(14, color: C.muted)) else for (final l in rows) _learnerCard(l),
        ]);
      case 2:
        body = Column(children: [
          for (final b in [for (final b in addedBooks) (b['title'] as String, b['language'] as String, 'Draft'), for (final s in stories) (s.title, s.language, '${s.pages.length} pages')])
            Padding(padding: const EdgeInsets.only(bottom: 8), child: WhiteCard(padding: const EdgeInsets.all(12), child: Row(children: [Expanded(child: Text(b.$1, style: T.head(16))), Tag(b.$2), const SizedBox(width: 8), Text(b.$3, style: T.body(12, color: C.muted, weight: FontWeight.w800))]))),
        ]);
      case 3:
        final support = all.where((l) => l.pct < 40 && l.last != 'Not yet').toList();
        int? mean(Iterable<int?> xs) {
          final v = xs.whereType<int>().toList();
          return v.isEmpty ? null : (v.reduce((a, b) => a + b) / v.length).round();
        }
        final en = mean(all.map((l) => l.en));
        final fil = mean(all.map((l) => l.fil));
        String csv() => ['Name,Level,Stories done,Progress %,English pronunciation %,Filipino accuracy %,Last active', for (final l in all) '"${l.name}",${l.level},${l.done},${l.pct},${l.en ?? ''},${l.fil ?? ''},${l.last}'].join('\n');
        body = Column(children: [
          _stats(all, books),
          const SizedBox(height: 6),
          WhiteCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Reading levels', style: T.head(18)),
            const SizedBox(height: 10),
            for (final n in levelNames)
              Padding(padding: const EdgeInsets.only(bottom: 8), child: Row(children: [SizedBox(width: 96, child: Tag(n, bg: levelBg(n), fg: levelFg(n))), const SizedBox(width: 10), Expanded(child: Bar(all.isEmpty ? 0 : (all.where((l) => l.level == n).length / all.length * 100).round(), height: 10)), const SizedBox(width: 8), Text('${all.where((l) => l.level == n).length}', style: T.body(14, weight: FontWeight.w800))])),
            const SizedBox(height: 8),
            Text('Class averages', style: T.head(18)),
            const SizedBox(height: 8),
            for (final (name, v) in [('English pronunciation', en), ('Filipino word reading accuracy', fil)])
              Padding(padding: const EdgeInsets.only(bottom: 10), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Row(children: [Expanded(child: Text(name, style: T.body(14, weight: FontWeight.w800))), Text(v == null ? 'Not yet' : '$v%', style: T.body(14, weight: FontWeight.w800))]), const SizedBox(height: 4), Bar(v ?? 0, color: C.green, height: 12)])),
          ])),
          const SizedBox(height: 12),
          WhiteCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Learners who need support', style: T.head(18)),
            const SizedBox(height: 8),
            if (support.isEmpty) Text('Everyone is on track.', style: T.body(14, color: C.muted)) else for (final l in support) Padding(padding: const EdgeInsets.only(bottom: 6), child: Row(children: [Expanded(child: Text(l.name, style: T.body(14, weight: FontWeight.w800))), Text('${l.pct}% progress', style: T.body(13, color: C.muted))])),
            const SizedBox(height: 8),
            Btn('Copy learners (CSV)', icon: Icons.copy, kind: BtnKind.white, onPressed: () async {
              await Clipboard.setData(ClipboardData(text: csv()));
              if (context.mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Copied. Paste it into a spreadsheet.')));
            }),
          ])),
        ]);
      case 4:
        body = WhiteCard(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Account', style: T.head(20)),
          const SizedBox(height: 10),
          for (final (k, v) in [('Name', [app.user?.firstName, app.user?.lastName].where((e) => e != null && e.isNotEmpty).join(' ')), ('Email', app.user?.email ?? ''), ('Role', 'Admin (from the backend)'), ('Server', 'Not connected yet (demo data)')])
            Padding(padding: const EdgeInsets.symmetric(vertical: 8), child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Text(k, style: T.body(14, weight: FontWeight.w800)), Flexible(child: Text(v, textAlign: TextAlign.right, style: T.body(14, color: C.muted)))])),
          const SizedBox(height: 8),
          Btn('Log out', icon: Icons.logout, kind: BtnKind.white, onPressed: () => confirmLogout(context)),
        ]));
      default:
        body = Column(children: [
          _stats(all, books),
          const SizedBox(height: 6),
          Align(alignment: Alignment.centerLeft, child: Padding(padding: const EdgeInsets.symmetric(vertical: 8), child: Text('Recent activity', style: T.head(18)))),
          for (final (dot, who, what, at) in [(C.green, 'Mia Santos', 'finished The Magic Kite · 3 stars', '10 minutes ago'), (C.blueBtn, 'Sam Dela Cruz', 'started Lesson 6: Bb', '25 minutes ago'), (C.orange, 'Paolo Reyes', 'needs help with letter sounds', 'Yesterday')])
            Padding(padding: const EdgeInsets.only(bottom: 8), child: WhiteCard(padding: const EdgeInsets.all(12), child: Row(children: [Container(width: 10, height: 10, decoration: BoxDecoration(color: dot, shape: BoxShape.circle)), const SizedBox(width: 12), Expanded(child: Text.rich(TextSpan(children: [TextSpan(text: who, style: T.body(14, weight: FontWeight.w800)), TextSpan(text: ' $what\n', style: T.body(14)), TextSpan(text: at, style: T.body(12, color: C.muted))])))]))),
        ]);
    }

    return AppPage(
      title: view == 0 ? 'Welcome, Teacher ${app.displayName}' : labels[view],
      subtitle: 'Kindergarten class overview · [SECTION NAME]',
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        ScrollSeg<int>(options: const [0, 1, 2, 3, 4], value: view, label: (i) => labels[i], onChanged: (i) => setState(() {
              view = i;
              q = '';
            })),
        const SizedBox(height: 10),
        Wrap(spacing: 10, children: [Btn('Add learner', icon: Icons.add, kind: BtnKind.white, onPressed: _addLearner), Btn('Add storybook', icon: Icons.add, onPressed: _addBook)]),
        const SizedBox(height: 10),
        body,
      ]),
    );
  }
}
