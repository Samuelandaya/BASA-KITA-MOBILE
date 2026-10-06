import 'package:basa_kita/data/content.dart';
import 'package:basa_kita/data/vocab.dart';
import 'package:basa_kita/learner.dart';
import 'package:basa_kita/services/book_match.dart';
import 'package:basa_kita/state/app_state.dart';
import 'package:basa_kita/state/models.dart';
import 'package:basa_kita/widgets/reading.dart' show scoreReading;
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  group('content', () {
    test('has 6 stories with 3 questions each, 20 lessons and 6 flashcard decks', () {
      expect(stories.length, 6);
      expect(stories.every((s) => s.questions.length == 3), isTrue);
      expect(lessons.length, 20);
      expect(decks.length, 6);
    });
    test('every flashcard has a 3D model file name', () {
      final names = decks.expand((d) => d.cards).map((c) => c.fil).toSet();
      expect(names.length, 18);
    });
  });

  group('syllables', () {
    test('Filipino consonant-vowel parts keep "ng" together', () {
      expect(syllables('saranggola'), ['sa', 'rang', 'go', 'la']);
      expect(syllables('mama'), ['ma', 'ma']);
    });
    test('English words use the listed parts', () {
      expect(syllables('teacher', filipino: false), ['tea', 'cher']);
      expect(syllables('ball', filipino: false), ['ball']);
    });
    test('normalizeWord strips punctuation', () => expect(normalizeWord('Ball,'), 'ball'));
  });

  group('book recognition', () {
    test('finds the right story and page from text read off a picture', () {
      final m = recognizeBook('Mia saw a red ball under a big tree.');
      expect(m?.story.id, 'mia-and-the-red-ball');
      expect(m?.page, 0);
    });
    test('returns null when nothing matches', () => expect(recognizeBook('zzz qqq'), isNull));
    test('finds a flashcard by its word', () => expect(recognizeFlashCard('this is mama'), isNotNull));
  });

  group('scores', () {
    test('English score uses confidence, Filipino uses accuracy', () {
      final words = ['Mia', 'saw', 'a', 'ball'];
      final heard = words.map(normalizeWord).toSet();
      expect(scoreReading(words, heard, 0.7, 'en-US').score, 88);
      expect(scoreReading(words, heard, 0.7, 'fil-PH').score, 100);
      expect(scoreReading(words, {'mia', 'saw'}, null, 'fil-PH').score, 50);
    });
  });

  group('learner level', () {
    test('a new account is a Beginner', () => expect(learnerLevel(Account()).name, 'Beginner'));
    test('the sample account is Growing', () => expect(learnerLevel(sampleAccount()).name, 'Growing'));
    test('recommendations skip finished stories', () {
      final a = sampleAccount();
      final recs = recommendStories(a, learnerLevel(a), 'Bilingual');
      expect(recs.every((r) => a.progress[r.story.id]?.finished != true), isTrue);
    });
  });

  group('app state', () {
    setUp(() => SharedPreferences.setMockInitialValues({}));

    test('a new learner starts empty and progress is saved per account', () async {
      final s = await AppState.load();
      s.login(User(firstName: 'Kid', username: 'kid', email: 'kid@x.ph', role: 'learner'));
      expect(isNewReader(s.progress), isTrue);
      s.openStory('the-lost-puppy');
      s.savePage('the-lost-puppy', 3);
      expect(percentRead(storyById('the-lost-puppy')!, s.progress['the-lost-puppy']), 40);
      s.finishStory('the-lost-puppy', 3);
      expect(isNewReader(s.progress), isFalse);
      s.logout();
      s.login(User(firstName: 'Other', username: 'other', email: 'other@x.ph', role: 'learner'));
      expect(isNewReader(s.progress), isTrue);
    });

    test('hearts, history and own stories work and survive a restart', () async {
      final s = await AppState.load();
      s.login(User(firstName: 'Sam', username: 'sam', email: 'sam@basakita.ph', role: 'learner', returning: true));
      s.toggleSaved('the-lost-puppy');
      expect(s.saved.contains('the-lost-puppy'), isTrue);
      s.toggleSaved('the-lost-puppy');
      expect(s.saved.contains('the-lost-puppy'), isFalse);
      s.saveDraft(Draft(id: 'd2', title: 'Aso', language: 'Filipino', texts: ['Si Mia ay may aso.'], images: ['']));
      final again = await AppState.load();
      expect(again.user?.email, 'sam@basakita.ph');
      expect(again.drafts.map((d) => d.id), contains('d2'));
      expect(continueId(again.progress), 'mia-and-the-red-ball');
    });
  });
}
