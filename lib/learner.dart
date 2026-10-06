import 'data/content.dart';
import 'data/models.dart';
import 'state/models.dart';

class Skills {
  final int letterSounds;
  final int? english;
  final int? filipino;
  final int? reading;
  final int? understanding;
  final int finished;
  const Skills(this.letterSounds, this.english, this.filipino, this.reading, this.understanding, this.finished);
}

int? _avg(Iterable<int> xs) => xs.isEmpty ? null : (xs.reduce((a, b) => a + b) / xs.length).round();

/// Skill percentages from what the learner has actually done.
Skills learnerSkills(Account a) {
  final all = a.scores.values.toList();
  final quiz = a.quizzes.values.toList();
  final finished = stories.where((s) => a.progress[s.id]?.finished == true).length;
  final correct = quiz.fold<int>(0, (n, q) => n + q.correct);
  final total = quiz.fold<int>(0, (n, q) => n + q.total);
  return Skills(
    (a.lessonsDone / lessons.length * 100).round(),
    _avg(all.where((x) => x.english).map((x) => x.score)),
    _avg(all.where((x) => !x.english).map((x) => x.score)),
    _avg(all.map((x) => x.score)),
    total == 0 ? null : (correct / total * 100).round(),
    finished,
  );
}

class LevelInfo {
  final String name;
  final int index;
  final int toNext;
  final String? next;
  final String goal;
  final Skills skills;
  const LevelInfo(this.name, this.index, this.toNext, this.next, this.goal, this.skills);
}

const levelNames = ['Beginner', 'Growing', 'Confident'];
const _goals = [
  'Finish 2 stories and learn 4 letter sounds',
  'Finish 5 stories, learn 10 sounds and read aloud at 80% or more',
  'Keep reading new stories!',
];

/// Learner level from finished stories, letter sounds learned and read-aloud scores.
LevelInfo learnerLevel(Account a) {
  final sk = learnerSkills(a);
  final goodReading = sk.reading == null || sk.reading! >= 80;
  var i = 0;
  if (sk.finished >= 2 && a.lessonsDone >= 4) i = 1;
  if (sk.finished >= 5 && a.lessonsDone >= 10 && goodReading) i = 2;
  int toNext;
  if (i == 2) {
    toNext = 100;
  } else if (i == 0) {
    toNext = (((sk.finished.clamp(0, 2) / 2) + (a.lessonsDone.clamp(0, 4) / 4)) * 50).round();
  } else {
    toNext = (((sk.finished.clamp(0, 5) / 5) + (a.lessonsDone.clamp(0, 10) / 10) + (goodReading ? 1 : 0)) / 3 * 100).round();
  }
  return LevelInfo(levelNames[i], i, toNext, i < 2 ? levelNames[i + 1] : null, _goals[i], sk);
}

class Recommendation {
  final Story story;
  final String reason;
  const Recommendation(this.story, this.reason);
}

/// Unfinished stories in the chosen language, practicing known sounds first.
List<Recommendation> recommendStories(Account a, LevelInfo level, String language, {int count = 4}) {
  final known = lessons.take(a.lessonsDone < 1 ? 1 : a.lessonsDone).map((l) => l.letter).toSet();
  bool fits(Story s) => language == 'Bilingual' || s.language == language || s.language == 'Bilingual';
  int score(Story s) =>
      (a.progress[s.id] != null ? 0 : 2) +
      (fits(s) ? 3 : 0) +
      (known.contains(s.focus.letter) ? 2 : 0) +
      (level.index == 0 && (s.level == 'Beginner' || s.level == s.language) ? 1 : 0);
  String reason(Story s) => known.contains(s.focus.letter)
      ? 'Practice the ${s.focus.letter} sound'
      : a.progress[s.id] == null
          ? 'New for you'
          : 'Keep going';
  final list = stories.where((s) => a.progress[s.id]?.finished != true).toList()
    ..sort((x, y) {
      final d = score(y) - score(x);
      return d != 0 ? d : x.minutes - y.minutes;
    });
  return list.take(count).map((s) => Recommendation(s, reason(s))).toList();
}
