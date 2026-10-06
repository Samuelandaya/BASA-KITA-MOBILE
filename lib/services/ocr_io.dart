import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

/// Reads the words in a picture on the phone itself (Google ML Kit), so no internet is needed.
Future<String> recognizeText(String path) async {
  final recognizer = TextRecognizer(script: TextRecognitionScript.latin);
  try {
    final result = await recognizer.processImage(InputImage.fromFilePath(path));
    return result.text.replaceAll(RegExp(r'\s+\n'), '\n').trim();
  } finally {
    recognizer.close();
  }
}
