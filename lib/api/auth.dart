import '../state/models.dart';

/// Account service. The backend decides each account's role ('learner' or 'admin');
/// the app only reads `user.role` and opens the matching home screen.
///
/// Demo stand-ins until a backend exists:
///  - admin@basakita.ph signs in as an admin
///  - sam@basakita.ph is a returning learner with sample reading history
///  - any other email is a brand-new learner with no history
///
/// To connect a server, replace the bodies below with HTTP calls that return the same [User].
class AuthApi {
  static const _adminEmails = ['admin@basakita.ph'];
  static const _returningEmails = ['sam@basakita.ph'];
  static final _email = RegExp(r'^\S+@\S+\.\S+$');

  static Future<void> _pause() => Future<void>.delayed(const Duration(milliseconds: 300));

  static String _niceName(String handle) {
    final first = handle.split(RegExp(r'[._-]')).first;
    if (first.isEmpty) return 'Reader';
    return first[0].toUpperCase() + first.substring(1);
  }

  static Future<User> signIn(String email, String password) async {
    final clean = email.trim().toLowerCase();
    if (clean.isEmpty || password.isEmpty) throw 'Please enter your email and password.';
    if (!_email.hasMatch(clean)) throw 'Please enter a valid email address.';
    await _pause();
    final handle = clean.split('@').first;
    if (_adminEmails.contains(clean)) {
      return User(firstName: 'Ana', lastName: 'Reyes', username: handle, email: clean, role: 'admin');
    }
    if (_returningEmails.contains(clean)) {
      return User(firstName: 'Sam', lastName: 'Dela Cruz', username: handle, email: clean, role: 'learner', returning: true);
    }
    return User(firstName: _niceName(handle), username: handle, email: clean, role: 'learner');
  }

  static Future<User> signUp({required String name, required String username, required String email, required String password}) async {
    await _pause();
    final parts = name.trim().split(RegExp(r'\s+'));
    return User(
      firstName: parts.first,
      lastName: parts.skip(1).join(' '),
      username: username,
      email: email.trim().toLowerCase(),
      role: 'pending',
    );
  }

  /// Demo: any 6-digit code works.
  static Future<User> verifyEmail(User user, String code) async {
    await _pause();
    user.role = 'learner';
    return user;
  }

  static Future<void> resendCode(String email) => _pause();
  static Future<void> requestPasswordReset(String email) => _pause();
  static Future<void> resetPassword(String? token, String password) => _pause();
}
