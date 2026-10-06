import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/content.dart';
import '../data/models.dart';
import '../lock.dart';
import 'models.dart';

const _key = 'basa-kita:state:v2';
int _daysAgo(double n) => DateTime.now().millisecondsSinceEpoch - (n * 86400000).round();

/// Demo "returning student" so the regular home page can be shown.
Account sampleAccount() => Account(
      progress: {
        'mia-and-the-red-ball': Progress(page: 3, lastRead: _daysAgo(0.1)),
        'the-magic-kite': Progress(page: 1, lastRead: _daysAgo(1)),
        'the-lost-puppy': Progress(page: 5, finished: true, stars: 3, lastRead: _daysAgo(2)),
        'ang-munting-puno': Progress(page: 5, finished: true, stars: 3, lastRead: _daysAgo(4)),
        'my-first-school-day': Progress(page: 5, finished: true, stars: 2, lastRead: _daysAgo(6)),
      },
      lessonsDone: 5,
      saved: ['the-magic-kite', 'ang-munting-puno'],
      scores: {
        'the-lost-puppy:1': const ScoreEntry(78, true),
        'the-lost-puppy:2': const ScoreEntry(64, true),
        'ang-munting-puno:1': const ScoreEntry(100, false),
        'ang-munting-puno:3': const ScoreEntry(86, false),
        'mia-and-the-red-ball:1': const ScoreEntry(72, true),
      },
      quizzes: {
        'the-lost-puppy': const QuizResult(3, 3),
        'ang-munting-puno': const QuizResult(2, 3),
        'my-first-school-day': const QuizResult(2, 3),
      },
      drafts: [
        Draft(
          id: 'draft-1',
          title: 'Ang Pusa ni Sam',
          language: 'Filipino',
          texts: ['Si Sam ay may pusa.', 'Ang pusa ay puti at malambot.', 'Mahal ni Sam ang kanyang pusa.'],
        ),
      ],
      sample: true,
    );

/// The app's single source of truth: signed-in user, settings and each learner's progress,
/// saved on the phone. (The backend will keep this data later.)
class AppState extends ChangeNotifier {
  User? user;
  int avatarColor = 0xFFFFB27A;
  Settings settings = Settings();
  final Map<String, Account> accounts = {};
  SharedPreferences? _prefs;

  static Future<AppState> load() async {
    final s = AppState();
    s._prefs = await SharedPreferences.getInstance();
    final raw = s._prefs!.getString(_key);
    if (raw != null) {
      try {
        final j = jsonDecode(raw) as Map<String, dynamic>;
        if (j['user'] != null) s.user = User.fromJson(Map<String, dynamic>.from(j['user'] as Map));
        s.avatarColor = (j['avatarColor'] as num?)?.toInt() ?? s.avatarColor;
        if (j['settings'] != null) s.settings = Settings.fromJson(Map<String, dynamic>.from(j['settings'] as Map));
        (j['accounts'] as Map? ?? {}).forEach((k, v) => s.accounts[k as String] = Account.fromJson(Map<String, dynamic>.from(v as Map)));
      } catch (_) {
        // A corrupt save starts fresh instead of crashing the app.
      }
    }
    return s;
  }

  String get _accountKey => (user?.email.isNotEmpty == true ? user!.email : user?.username ?? 'guest').toLowerCase();
  Account get account => accounts[_accountKey] ?? Account();
  Account _mutable() => accounts.putIfAbsent(_accountKey, Account.new);

  Map<String, Progress> get progress => account.progress;
  int get lessonsDone => account.lessonsDone;
  List<Draft> get drafts => account.drafts;
  List<String> get saved => account.saved;
  Map<String, ScoreEntry> get scores => account.scores;
  Map<String, QuizResult> get quizzes => account.quizzes;
  bool get sampleData => account.sample;
  bool get isAdmin => user?.role == 'admin';
  String get displayName => (user?.firstName.isNotEmpty ?? false) ? user!.firstName : 'Reader';

  void _save() {
    notifyListeners();
    _prefs?.setString(
      _key,
      jsonEncode({
        'user': user?.toJson(),
        'avatarColor': avatarColor,
        'settings': settings.toJson(),
        'accounts': accounts.map((k, v) => MapEntry(k, v.toJson())),
      }),
    );
  }

  // --- account
  void login(User u) {
    user = u;
    final k = _accountKey;
    accounts.putIfAbsent(k, () => u.returning ? sampleAccount() : Account());
    _save();
  }

  void logout() {
    relock();
    user = null;
    _save();
  }

  void updateUser(void Function(User u) change) {
    if (user == null) return;
    change(user!);
    _save();
  }

  void setAvatarColor(int c) {
    avatarColor = c;
    _save();
  }

  void updateSettings(void Function(Settings s) change) {
    change(settings);
    _save();
  }

  // --- reading
  /// Opening a story adds it to Reading History (and keeps the saved page).
  void openStory(String id) {
    final a = _mutable();
    final p = a.progress.putIfAbsent(id, () => Progress());
    p.lastRead = DateTime.now().millisecondsSinceEpoch;
    _save();
  }

  void savePage(String id, int page) {
    final p = _mutable().progress.putIfAbsent(id, () => Progress());
    p.page = page;
    p.lastRead = DateTime.now().millisecondsSinceEpoch;
    _save();
  }

  void finishStory(String id, int stars) {
    final p = _mutable().progress.putIfAbsent(id, () => Progress());
    p.finished = true;
    p.stars = stars;
    p.lastRead = DateTime.now().millisecondsSinceEpoch;
    _save();
  }

  void toggleSaved(String id) {
    final a = _mutable();
    a.saved.contains(id) ? a.saved.remove(id) : a.saved.insert(0, id);
    _save();
  }

  void completeLesson() {
    _mutable().lessonsDone += 1;
    _save();
  }

  void recordScore(String storyId, int page, int score, bool english) {
    _mutable().scores['$storyId:$page'] = ScoreEntry(score, english);
    _save();
  }

  void recordQuiz(String storyId, int correct, int total) {
    _mutable().quizzes[storyId] = QuizResult(correct, total);
    _save();
  }

  void saveDraft(Draft d) {
    final a = _mutable();
    a.drafts.removeWhere((x) => x.id == d.id);
    a.drafts.insert(0, d);
    _save();
  }

  void deleteDraft(String id) {
    _mutable().drafts.removeWhere((d) => d.id == id);
    _save();
  }
}

// --- helpers shared by screens

Story? storyById(String id) {
  for (final s in stories) {
    if (s.id == id) return s;
  }
  return null;
}

/// How much of a story has been read, 0-100.
int percentRead(Story story, Progress? p) {
  if (p == null) return 0;
  if (p.finished) return 100;
  final page = p.page.clamp(1, story.pages.length);
  return ((page - 1) / story.pages.length * 100).round();
}

/// Story ids, most recently read first.
List<String> historyIds(Map<String, Progress> progress) {
  final e = progress.entries.where((x) => x.value.lastRead != null).toList()
    ..sort((a, b) => b.value.lastRead!.compareTo(a.value.lastRead!));
  return e.map((x) => x.key).toList();
}

/// The story to pick up again: the most recent one that is not finished.
String? continueId(Map<String, Progress> progress) {
  for (final id in historyIds(progress)) {
    if (!progress[id]!.finished) return id;
  }
  return null;
}

/// A learner is "new" until they finish their first story.
bool isNewReader(Map<String, Progress> progress) => !progress.values.any((p) => p.finished);
