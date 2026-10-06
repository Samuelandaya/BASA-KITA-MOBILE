// Remembers which account has entered the parent PIN since the app opened.
// Logging out locks Settings again, and so does closing the app.
String? _unlockedFor;
bool isUnlocked(String? email) => email != null && email.isNotEmpty && _unlockedFor == email;
void unlock(String? email) => _unlockedFor = email;
void relock() => _unlockedFor = null;
