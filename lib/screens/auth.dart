import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../api/auth.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';

/// Yellow sign-in backdrop with the logo on top and a white card.
class AuthShell extends StatelessWidget {
  final Widget child;
  final bool wordmark;
  final bool back;
  const AuthShell({super.key, required this.child, this.wordmark = false, this.back = true});
  @override
  Widget build(BuildContext context) => Scaffold(
        backgroundColor: C.yellow,
        body: Column(children: [
          Container(
            width: double.infinity,
            color: C.cream,
            child: SafeArea(
              bottom: false,
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 10),
                child: Stack(alignment: Alignment.center, children: [
                  Image.asset(wordmark ? 'assets/images/wordmark.png' : 'assets/images/logo.png', height: 64, semanticLabel: 'BASA-KITA'),
                  if (back && context.canPop()) Positioned(left: 8, child: IconButton(onPressed: () => context.pop(), icon: const Icon(Icons.arrow_back, color: C.navy), tooltip: 'Back')),
                ]),
              ),
            ),
          ),
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(20, 24, 28, 24),
              child: child,
            ),
          ),
          Container(
            width: double.infinity,
            color: C.blue,
            padding: const EdgeInsets.symmetric(vertical: 10),
            child: SafeArea(top: false, child: Text('© 2026 BASA-KITA · Read • Speak • Learn', textAlign: TextAlign.center, style: T.body(12, color: Colors.white))),
          ),
        ]),
      );
}

/// White rounded card with the offset shadow from the design.
class AuthCard extends StatelessWidget {
  final List<Widget> children;
  const AuthCard({super.key, required this.children});
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.fromLTRB(22, 26, 22, 22),
        decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(34), boxShadow: const [BoxShadow(color: Color(0xFFDCC56E), offset: Offset(8, 8))]),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: children),
      );
}

class LabeledField extends StatelessWidget {
  final String label;
  final TextEditingController controller;
  final String? hint;
  final bool password;
  final TextInputType? keyboard;
  final Iterable<String>? autofill;
  final ValueChanged<String>? onSubmitted;
  final ValueChanged<String>? onChanged;
  final TextInputAction? action;
  const LabeledField(this.label, this.controller, {super.key, this.hint, this.password = false, this.keyboard, this.autofill, this.onSubmitted, this.onChanged, this.action});
  @override
  Widget build(BuildContext context) => _FieldBody(this);
}

class _FieldBody extends StatefulWidget {
  final LabeledField f;
  const _FieldBody(this.f);
  @override
  State<_FieldBody> createState() => _FieldBodyState();
}

class _FieldBodyState extends State<_FieldBody> {
  bool hide = true;
  @override
  Widget build(BuildContext context) {
    final f = widget.f;
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(f.label, style: T.body(14, weight: FontWeight.w800)),
        const SizedBox(height: 6),
        TextField(
          controller: f.controller,
          obscureText: f.password && hide,
          keyboardType: f.keyboard,
          autofillHints: f.autofill,
          textInputAction: f.action,
          onSubmitted: f.onSubmitted,
          onChanged: f.onChanged,
          style: T.body(16),
          decoration: InputDecoration(
            hintText: f.hint,
            suffixIcon: f.password
                ? IconButton(icon: Icon(hide ? Icons.visibility_outlined : Icons.visibility_off_outlined, color: C.muted), tooltip: hide ? 'Show password' : 'Hide password', onPressed: () => setState(() => hide = !hide))
                : null,
          ),
        ),
      ]),
    );
  }
}

final _emailRe = RegExp(r'^\S+@\S+\.\S+$');

// ---------------------------------------------------------------- Welcome
class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
        backgroundColor: C.yellow,
        body: SafeArea(
          child: Column(children: [
            const SizedBox(height: 24),
            Image.asset('assets/images/wordmark.png', height: 74, semanticLabel: 'BASA-KITA'),
            const SizedBox(height: 6),
            Text('Read • Speak • Learn', style: T.fun(24, color: C.navy)),
            Expanded(child: Padding(padding: const EdgeInsets.all(18), child: Image.asset('assets/images/login-illustration.png', fit: BoxFit.contain))),
            Padding(
              padding: const EdgeInsets.fromLTRB(28, 0, 28, 8),
              child: Column(children: [
                Btn('Login', big: true, expand: true, onPressed: () => context.push('/login')),
                const SizedBox(height: 10),
                Btn('Sign Up', big: true, expand: true, kind: BtnKind.white, onPressed: () => context.push('/signup')),
              ]),
            ),
            Container(
              width: double.infinity,
              margin: const EdgeInsets.only(top: 14),
              color: C.blue,
              padding: const EdgeInsets.symmetric(vertical: 10),
              child: Text('Filipino-English reading for kindergarten learners', textAlign: TextAlign.center, style: T.body(12, color: Colors.white)),
            ),
          ]),
        ),
      );
}

// ---------------------------------------------------------------- Login
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});
  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final email = TextEditingController();
  final password = TextEditingController();
  String error = '';
  bool busy = false;

  Future<void> submit() async {
    setState(() {
      error = '';
      busy = true;
    });
    try {
      final user = await AuthApi.signIn(email.text, password.text);
      if (!mounted) return;
      context.read<AppState>().login(user); // the router opens the learner or admin home from the role
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => AuthShell(
        wordmark: true,
        child: AuthCard(children: [
          Text('LOGIN', textAlign: TextAlign.center, style: T.head(34, weight: FontWeight.w600)),
          const SizedBox(height: 4),
          Text('Sign in to access your learning activities.', textAlign: TextAlign.center, style: T.body(14, color: C.muted)),
          const SizedBox(height: 20),
          LabeledField('Email Address', email, hint: 'name@email.com', keyboard: TextInputType.emailAddress, autofill: const [AutofillHints.email], action: TextInputAction.next),
          LabeledField('Password', password, password: true, autofill: const [AutofillHints.password], action: TextInputAction.done, onSubmitted: (_) => submit()),
          Align(alignment: Alignment.centerRight, child: TextButton(onPressed: () => context.push('/forgot'), child: Text('Forgot Password?', style: T.body(14, color: C.blueBtn, weight: FontWeight.w800)))),
          FormError(error),
          Btn(busy ? 'Logging in…' : 'Login', kind: BtnKind.blue, big: true, expand: true, onPressed: busy ? null : submit),
          const SizedBox(height: 10),
          Center(
            child: TextButton(
              onPressed: () => context.pushReplacement('/signup'),
              child: Text.rich(TextSpan(style: T.body(14, color: C.muted), children: [const TextSpan(text: 'Don’t have an account? '), TextSpan(text: 'Sign Up', style: T.body(14, color: C.orangeDark, weight: FontWeight.w800))])),
            ),
          ),
        ]),
      );
}

// ---------------------------------------------------------------- Sign up
class SignUpScreen extends StatefulWidget {
  const SignUpScreen({super.key});
  @override
  State<SignUpScreen> createState() => _SignUpScreenState();
}

class _SignUpScreenState extends State<SignUpScreen> {
  final name = TextEditingController();
  final username = TextEditingController();
  final email = TextEditingController();
  final password = TextEditingController();
  final confirm = TextEditingController();
  String error = '';
  bool busy = false;

  Future<void> submit() async {
    if (name.text.trim().isEmpty || username.text.trim().isEmpty || email.text.trim().isEmpty) return setState(() => error = 'Please fill in every field.');
    if (!_emailRe.hasMatch(email.text)) return setState(() => error = 'Please enter a valid email address.');
    final p = password.text;
    if (p.length < 8 || !RegExp(r'[A-Z]').hasMatch(p) || !RegExp(r'\d').hasMatch(p)) {
      return setState(() => error = 'Password needs at least 8 characters, 1 uppercase letter and 1 number.');
    }
    if (p != confirm.text) return setState(() => error = 'The two passwords don’t match.');
    setState(() {
      error = '';
      busy = true;
    });
    try {
      final user = await AuthApi.signUp(name: name.text, username: username.text, email: email.text, password: p);
      if (!mounted) return;
      context.read<AppState>().login(user); // stays "pending" until the email code is verified
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => AuthShell(
        child: AuthCard(children: [
          Text('Sign Up', textAlign: TextAlign.center, style: T.head(34, weight: FontWeight.w600)),
          const SizedBox(height: 16),
          LabeledField('Name', name, hint: 'First Name, Last Name...', autofill: const [AutofillHints.name], action: TextInputAction.next),
          LabeledField('Username', username, hint: 'Username...', autofill: const [AutofillHints.newUsername], action: TextInputAction.next),
          LabeledField('Email Address', email, hint: 'name@email.com', keyboard: TextInputType.emailAddress, autofill: const [AutofillHints.email], action: TextInputAction.next),
          LabeledField('Password', password, password: true, autofill: const [AutofillHints.newPassword], action: TextInputAction.next),
          LabeledField('Confirm Password', confirm, password: true, autofill: const [AutofillHints.newPassword], action: TextInputAction.done, onSubmitted: (_) => submit()),
          FormError(error),
          Btn(busy ? 'Signing up…' : 'Sign Up', big: true, expand: true, onPressed: busy ? null : submit),
        ]),
      );
}

// ---------------------------------------------------------------- Verify
class VerifyScreen extends StatefulWidget {
  const VerifyScreen({super.key});
  @override
  State<VerifyScreen> createState() => _VerifyScreenState();
}

class _VerifyScreenState extends State<VerifyScreen> {
  final code = TextEditingController();
  final focus = FocusNode();
  String error = '';
  bool resent = false;
  bool busy = false;

  Future<void> submit() async {
    if (code.text.length != 6) return setState(() => error = 'Please enter all 6 numbers.');
    final app = context.read<AppState>();
    setState(() {
      error = '';
      busy = true;
    });
    try {
      final user = await AuthApi.verifyEmail(app.user!, code.text);
      app.updateUser((u) => u.role = user.role);
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    return AuthShell(
      back: false,
      child: AuthCard(children: [
        Text('Check your email', textAlign: TextAlign.center, style: T.head(30, weight: FontWeight.w600)),
        const SizedBox(height: 6),
        Text('We sent a 6-digit code to ${app.user?.email ?? 'your email'}.', textAlign: TextAlign.center, style: T.body(14, color: C.muted)),
        const SizedBox(height: 18),
        GestureDetector(
          onTap: focus.requestFocus,
          child: Stack(alignment: Alignment.center, children: [
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              for (var i = 0; i < 6; i++)
                Container(
                  width: 44, height: 54,
                  margin: const EdgeInsets.symmetric(horizontal: 3),
                  alignment: Alignment.center,
                  decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(14), border: Border.all(color: i == code.text.length ? C.blueBtn : const Color(0xFFE5E7EB), width: 2)),
                  child: Text(i < code.text.length ? code.text[i] : '', style: T.head(26, weight: FontWeight.w700)),
                ),
            ]),
            // The real input sits invisibly on top so paste and one-time-code autofill work.
            Positioned.fill(
              child: Opacity(
                opacity: 0,
                child: TextField(
                  controller: code,
                  focusNode: focus,
                  autofocus: true,
                  keyboardType: TextInputType.number,
                  autofillHints: const [AutofillHints.oneTimeCode],
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly, LengthLimitingTextInputFormatter(6)],
                  onChanged: (_) => setState(() {}),
                ),
              ),
            ),
          ]),
        ),
        const SizedBox(height: 16),
        FormError(error),
        Btn(busy ? 'Checking…' : 'Verify', big: true, expand: true, onPressed: busy ? null : submit),
        const SizedBox(height: 6),
        Center(
          child: TextButton(
            onPressed: () async {
              await AuthApi.resendCode(app.user?.email ?? '');
              if (mounted) setState(() => resent = true);
            },
            child: Text(resent ? 'A new code is on its way.' : 'Didn’t get it? Resend code', style: T.body(14, color: C.blueBtn, weight: FontWeight.w800)),
          ),
        ),
        Center(child: TextButton(onPressed: () => app.logout(), child: Text('Back to Login', style: T.body(14, color: C.muted, weight: FontWeight.w800)))),
      ]),
    );
  }
}

// ---------------------------------------------------------------- Forgot / reset
class ForgotScreen extends StatefulWidget {
  const ForgotScreen({super.key});
  @override
  State<ForgotScreen> createState() => _ForgotScreenState();
}

class _ForgotScreenState extends State<ForgotScreen> {
  final email = TextEditingController();
  String error = '';
  bool busy = false;

  Future<void> submit() async {
    if (!_emailRe.hasMatch(email.text)) return setState(() => error = 'Please enter a valid email address.');
    setState(() {
      error = '';
      busy = true;
    });
    try {
      await AuthApi.requestPasswordReset(email.text.trim());
      if (!mounted) return;
      await showMessage(context, 'Reset link sent!', 'Check your email for a secure link to choose a new password.', button: 'Back to Login');
      if (mounted) context.go('/login');
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => AuthShell(
        child: AuthCard(children: [
          Text('Forgot Password', textAlign: TextAlign.center, style: T.head(30, weight: FontWeight.w600)),
          const SizedBox(height: 6),
          Text('Enter your email and we’ll send a secure reset link.', textAlign: TextAlign.center, style: T.body(14, color: C.muted)),
          const SizedBox(height: 18),
          LabeledField('Email Address', email, hint: 'name@email.com', keyboard: TextInputType.emailAddress, autofill: const [AutofillHints.email], action: TextInputAction.done, onSubmitted: (_) => submit()),
          FormError(error),
          Btn(busy ? 'Sending…' : 'Send reset link', kind: BtnKind.blue, big: true, expand: true, onPressed: busy ? null : submit),
          Center(child: TextButton(onPressed: () => context.pop(), child: Text('Back to Login', style: T.body(14, color: C.blueBtn, weight: FontWeight.w800)))),
        ]),
      );
}

/// Opened from the link in the reset email (basakita://reset?token=...).
class ResetScreen extends StatefulWidget {
  final String? token;
  const ResetScreen({super.key, this.token});
  @override
  State<ResetScreen> createState() => _ResetScreenState();
}

class _ResetScreenState extends State<ResetScreen> {
  final pw = TextEditingController();
  final confirm = TextEditingController();
  String error = '';

  static final _rules = <(String, bool Function(String))>[
    ('At least 8 characters, with letters and numbers.', (p) => p.length >= 8 && RegExp(r'[a-z]', caseSensitive: false).hasMatch(p) && RegExp(r'\d').hasMatch(p)),
    ('At least 1 uppercase letter (A–Z)', (p) => RegExp(r'[A-Z]').hasMatch(p)),
    ('At least 1 number (0–9)', (p) => RegExp(r'\d').hasMatch(p)),
  ];

  Future<void> submit() async {
    if (!_rules.every((r) => r.$2(pw.text))) return setState(() => error = 'Your new password doesn’t meet all the rules yet.');
    if (pw.text != confirm.text) return setState(() => error = 'The two passwords don’t match.');
    setState(() => error = '');
    try {
      await AuthApi.resetPassword(widget.token, pw.text);
      if (!mounted) return;
      await showMessage(context, 'Password Updated!', 'You can now log in with your new password.', button: 'Back to Login');
      if (mounted) context.go('/login');
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) => AuthShell(
        back: false,
        child: AuthCard(children: [
          Text('Reset Your Password', textAlign: TextAlign.center, style: T.head(28, weight: FontWeight.w600)),
          const SizedBox(height: 16),
          LabeledField('New password', pw, password: true, action: TextInputAction.next, onChanged: (_) => setState(() {})),
          LabeledField('Confirm new password', confirm, password: true, action: TextInputAction.done, onSubmitted: (_) => submit()),
          StatefulBuilder(
            builder: (_, _) => Column(children: [
              for (final r in _rules)
                Padding(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Row(children: [
                    Icon(r.$2(pw.text) ? Icons.check_circle : Icons.radio_button_unchecked, size: 18, color: r.$2(pw.text) ? C.green : C.muted),
                    const SizedBox(width: 8),
                    Expanded(child: Text(r.$1, style: T.body(13, color: C.muted))),
                  ]),
                ),
            ]),
          ),
          const SizedBox(height: 8),
          FormError(error),
          Btn('Update Password', kind: BtnKind.blue, big: true, expand: true, onPressed: submit),
        ]),
      );
}
