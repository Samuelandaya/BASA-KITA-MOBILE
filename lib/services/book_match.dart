import '../data/content.dart';
import '../data/models.dart';
import '../data/vocab.dart';

class BookMatch {
  final Story story;
  final int page; // zero based
  final double score;
  const BookMatch(this.story, this.page, this.score);
}

List<String> _words(String t) => t.split(RegExp(r'\s+')).map(normalizeWord).where((w) => w.isNotEmpty).toList();

/// Book recognition: finds the storybook page whose words best match the text read from a picture.
BookMatch? recognizeBook(String text) {
  final found = _words(text).toSet();
  if (found.isEmpty) return null;
  BookMatch? best;
  for (final story in stories) {
    final titleWords = _words(story.title);
    final titleHit = titleWords.where(found.contains).length / titleWords.length;
    for (var page = 0; page < story.pages.length; page++) {
      final pw = _words(story.pages[page].text);
      final score = pw.where(found.contains).length / pw.length;
      final total = score > titleHit * 0.9 ? score : titleHit * 0.9;
      if (best == null || total > best.score) best = BookMatch(story, page, total);
    }
  }
  return best != null && best.score >= 0.5 ? best : null;
}

/// Flash card scanning: finds the flashcard whose word appears in the text read from a picture.
({int deck, int card})? recognizeFlashCard(String text) {
  final found = _words(text).toSet();
  for (var d = 0; d < decks.length; d++) {
    for (var c = 0; c < decks[d].cards.length; c++) {
      if (found.contains(normalizeWord(decks[d].cards[c].fil))) return (deck: d, card: c);
    }
  }
  return null;
}

/// The word on the flashcard that [recognizeFlashCard] found.
String decksCardFil(({int deck, int card}) m) => decks[m.deck].cards[m.card].fil;
