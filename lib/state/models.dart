// Per-account data, with the same shape as the website's saved state.

class Progress {
  int page;
  bool finished;
  int? stars;
  int? lastRead; // milliseconds since epoch
  Progress({this.page = 1, this.finished = false, this.stars, this.lastRead});

  Map<String, dynamic> toJson() => {'page': page, 'finished': finished, 'stars': stars, 'lastRead': lastRead};
  factory Progress.fromJson(Map<String, dynamic> j) => Progress(
        page: (j['page'] as num?)?.toInt() ?? 1,
        finished: j['finished'] == true,
        stars: (j['stars'] as num?)?.toInt(),
        lastRead: (j['lastRead'] as num?)?.toInt(),
      );
}

class ScoreEntry {
  final int score;
  final bool english;
  const ScoreEntry(this.score, this.english);
  Map<String, dynamic> toJson() => {'score': score, 'english': english};
  factory ScoreEntry.fromJson(Map<String, dynamic> j) => ScoreEntry((j['score'] as num).toInt(), j['english'] == true);
}

class QuizResult {
  final int correct;
  final int total;
  const QuizResult(this.correct, this.total);
  Map<String, dynamic> toJson() => {'correct': correct, 'total': total};
  factory QuizResult.fromJson(Map<String, dynamic> j) => QuizResult((j['correct'] as num).toInt(), (j['total'] as num).toInt());
}

/// A story the learner wrote. Pictures are small base64 JPEGs, one per page.
class Draft {
  final String id;
  String title;
  String language;
  String character;
  String place;
  List<String> texts;
  List<String> images;
  Draft({
    required this.id,
    required this.title,
    required this.language,
    this.character = 'Cat',
    this.place = 'Home',
    required this.texts,
    List<String>? images,
  }) : images = images ?? [];

  int get pages => texts.length;

  Map<String, dynamic> toJson() => {
        'id': id, 'title': title, 'language': language, 'character': character, 'place': place,
        'texts': texts, 'images': images,
      };
  factory Draft.fromJson(Map<String, dynamic> j) => Draft(
        id: j['id'] as String,
        title: j['title'] as String? ?? 'My Story',
        language: j['language'] as String? ?? 'Filipino',
        character: j['character'] as String? ?? 'Cat',
        place: j['place'] as String? ?? 'Home',
        texts: (j['texts'] as List?)?.map((e) => e.toString()).toList() ?? [''],
        images: (j['images'] as List?)?.map((e) => e.toString()).toList() ?? [],
      );
}

class Account {
  Map<String, Progress> progress;
  int lessonsDone;
  List<Draft> drafts;
  List<String> saved;
  Map<String, ScoreEntry> scores; // "storyId:page"
  Map<String, QuizResult> quizzes;
  bool sample;
  Account({
    Map<String, Progress>? progress,
    this.lessonsDone = 0,
    List<Draft>? drafts,
    List<String>? saved,
    Map<String, ScoreEntry>? scores,
    Map<String, QuizResult>? quizzes,
    this.sample = false,
  })  : progress = progress ?? {},
        drafts = drafts ?? [],
        saved = saved ?? [],
        scores = scores ?? {},
        quizzes = quizzes ?? {};

  Map<String, dynamic> toJson() => {
        'progress': progress.map((k, v) => MapEntry(k, v.toJson())),
        'lessonsDone': lessonsDone,
        'drafts': drafts.map((d) => d.toJson()).toList(),
        'saved': saved,
        'scores': scores.map((k, v) => MapEntry(k, v.toJson())),
        'quizzes': quizzes.map((k, v) => MapEntry(k, v.toJson())),
        'sample': sample,
      };

  factory Account.fromJson(Map<String, dynamic> j) => Account(
        progress: (j['progress'] as Map? ?? {}).map((k, v) => MapEntry(k as String, Progress.fromJson(Map<String, dynamic>.from(v as Map)))),
        lessonsDone: (j['lessonsDone'] as num?)?.toInt() ?? 0,
        drafts: (j['drafts'] as List? ?? []).map((d) => Draft.fromJson(Map<String, dynamic>.from(d as Map))).toList(),
        saved: (j['saved'] as List? ?? []).map((e) => e.toString()).toList(),
        scores: (j['scores'] as Map? ?? {}).map((k, v) => MapEntry(k as String, ScoreEntry.fromJson(Map<String, dynamic>.from(v as Map)))),
        quizzes: (j['quizzes'] as Map? ?? {}).map((k, v) => MapEntry(k as String, QuizResult.fromJson(Map<String, dynamic>.from(v as Map)))),
        sample: j['sample'] == true,
      );
}

class User {
  String firstName;
  String lastName;
  String username;
  String email;
  String role; // learner | admin | pending
  bool returning;
  String school;
  String teacher;
  User({
    required this.firstName,
    this.lastName = '',
    required this.username,
    required this.email,
    required this.role,
    this.returning = false,
    this.school = '',
    this.teacher = '',
  });

  Map<String, dynamic> toJson() => {
        'firstName': firstName, 'lastName': lastName, 'username': username, 'email': email, 'role': role,
        'returning': returning, 'school': school, 'teacher': teacher,
      };
  factory User.fromJson(Map<String, dynamic> j) => User(
        firstName: j['firstName'] as String? ?? 'Reader',
        lastName: j['lastName'] as String? ?? '',
        username: j['username'] as String? ?? '',
        email: j['email'] as String? ?? '',
        role: j['role'] as String? ?? 'learner',
        returning: j['returning'] == true,
        school: j['school'] as String? ?? '',
        teacher: j['teacher'] as String? ?? '',
      );
}

class Settings {
  String language; // Filipino | English | Bilingual
  bool languageChosen;
  double speed;
  String textSize; // Normal | Big | Bigger
  bool highlight;
  bool narration;
  bool effects;
  bool music;
  int volume; // 0-100
  bool parentLock;
  String pin;
  bool reminder;
  String voice; // ana | ben | maya | jay | mia | lola
  Settings({
    this.language = 'Bilingual',
    this.languageChosen = false,
    this.speed = 1,
    this.textSize = 'Big',
    this.highlight = true,
    this.narration = true,
    this.effects = true,
    this.music = false,
    this.volume = 70,
    this.parentLock = false,
    this.pin = '',
    this.reminder = false,
    this.voice = 'ana',
  });

  Map<String, dynamic> toJson() => {
        'language': language, 'languageChosen': languageChosen, 'speed': speed, 'textSize': textSize,
        'highlight': highlight, 'narration': narration, 'effects': effects, 'music': music, 'volume': volume,
        'parentLock': parentLock, 'pin': pin, 'reminder': reminder, 'voice': voice,
      };
  factory Settings.fromJson(Map<String, dynamic> j) {
    final d = Settings();
    return Settings(
      language: j['language'] as String? ?? d.language,
      languageChosen: j['languageChosen'] == true,
      speed: (j['speed'] as num?)?.toDouble() ?? d.speed,
      textSize: j['textSize'] as String? ?? d.textSize,
      highlight: j['highlight'] as bool? ?? d.highlight,
      narration: j['narration'] as bool? ?? d.narration,
      effects: j['effects'] as bool? ?? d.effects,
      music: j['music'] as bool? ?? d.music,
      volume: (j['volume'] as num?)?.toInt() ?? d.volume,
      parentLock: j['parentLock'] as bool? ?? d.parentLock,
      pin: j['pin'] as String? ?? d.pin,
      reminder: j['reminder'] as bool? ?? d.reminder,
      voice: j['voice'] as String? ?? d.voice,
    );
  }
}
