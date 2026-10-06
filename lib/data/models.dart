// Plain data types shared by the stories, lessons, vocabulary and flashcards.

class QuizOption {
  final String label;
  final int color;
  final bool correct;
  const QuizOption(this.label, this.color, this.correct);
}

class Quiz {
  final String question;
  final List<QuizOption> options;
  const Quiz(this.question, this.options);
}

class StoryPage {
  final String text;
  final String word; // the word to highlight in Read and Learn
  const StoryPage(this.text, this.word);
}

class Focus {
  final String letter;
  final String sound;
  final String example;
  final String exampleFil;
  final List<String> syllables;
  final List<String> parts;
  final List<String> partsFil;
  const Focus({
    required this.letter,
    required this.sound,
    required this.example,
    required this.exampleFil,
    required this.syllables,
    required this.parts,
    required this.partsFil,
  });
}

class Story {
  final String id;
  final String title;
  final String language; // Filipino | English | Bilingual
  final String lang; // speech locale, e.g. en-US
  final String level;
  final int minutes;
  final String ages;
  final String theme;
  final int color;
  final String cover; // asset path
  final String summary;
  final String blurb;
  final Focus focus;
  final List<StoryPage> pages;
  final List<Quiz> questions; // three comprehension questions
  const Story({
    required this.id,
    required this.title,
    required this.language,
    required this.lang,
    required this.level,
    required this.minutes,
    required this.ages,
    required this.theme,
    required this.color,
    required this.cover,
    required this.summary,
    required this.blurb,
    required this.focus,
    required this.pages,
    required this.questions,
  });

  bool get isFilipino => lang.startsWith('fil');
  String get shownLevel => level == language ? 'Beginner' : level;
}

class Lesson {
  final int id;
  final String letter;
  final String fil;
  final String eng;
  const Lesson(this.id, this.letter, this.fil, this.eng);
}

class VocabEntry {
  final String pair;
  final String meaning;
  const VocabEntry(this.pair, this.meaning);
}

class FlashCard {
  final String fil;
  final String shape;
  final String eng;
  final List<String> syl;
  final int color;
  const FlashCard({required this.fil, required this.shape, required this.eng, required this.syl, required this.color});
}

class Deck {
  final String letter;
  final List<FlashCard> cards;
  const Deck(this.letter, this.cards);
}
