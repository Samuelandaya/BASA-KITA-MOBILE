import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'screens/admin.dart';
import 'screens/auth.dart';
import 'screens/cards.dart';
import 'screens/create.dart';
import 'screens/home.dart';
import 'screens/learn.dart';
import 'screens/library.dart';
import 'screens/me.dart';
import 'screens/read_modes.dart';
import 'screens/scan.dart';
import 'screens/settings.dart';
import 'state/app_state.dart';
import 'theme.dart';

const _publicPaths = {'/welcome', '/login', '/signup', '/verify', '/forgot', '/reset'};

GoRouter buildRouter(AppState state) => GoRouter(
      initialLocation: '/home',
      refreshListenable: state,
      redirect: (context, route) {
        final path = route.uri.path;
        final user = state.user;
        final isPublic = _publicPaths.contains(path);
        if (user == null) return isPublic ? null : '/welcome';
        if (user.role == 'pending') return path == '/verify' ? null : '/verify';
        if (user.role == 'admin') return path == '/admin' ? null : '/admin';
        if (isPublic || path == '/admin') return '/home';
        return null;
      },
      routes: [
        GoRoute(path: '/welcome', builder: (_, _) => const WelcomeScreen()),
        GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
        GoRoute(path: '/signup', builder: (_, _) => const SignUpScreen()),
        GoRoute(path: '/verify', builder: (_, _) => const VerifyScreen()),
        GoRoute(path: '/forgot', builder: (_, _) => const ForgotScreen()),
        GoRoute(path: '/reset', builder: (_, s) => ResetScreen(token: s.uri.queryParameters['token'])),
        ShellRoute(
          builder: (context, route, child) => TabShell(location: route.uri.path, child: child),
          routes: [
            GoRoute(path: '/home', pageBuilder: (_, _) => const NoTransitionPage(child: HomeScreen())),
            GoRoute(path: '/books', pageBuilder: (_, _) => const NoTransitionPage(child: StorybooksScreen())),
            GoRoute(path: '/learn', pageBuilder: (_, _) => const NoTransitionPage(child: LearnScreen())),
            GoRoute(path: '/cards', pageBuilder: (_, s) => NoTransitionPage(child: FlashcardsScreen(startCard: s.uri.queryParameters['card']))),
            GoRoute(path: '/me', pageBuilder: (_, _) => const NoTransitionPage(child: MeScreen())),
          ],
        ),
        GoRoute(path: '/books/:id', builder: (_, s) => StoryDetailsScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/books/:id/listen', builder: (_, s) => ListenScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/books/:id/read', builder: (_, s) => ReadAndLearnScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/books/:id/with-me', builder: (_, s) => ReadWithMeScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/books/:id/normal', builder: (_, s) => NormalReadingScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/books/:id/complete', builder: (_, s) => CompleteScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/scan', builder: (_, s) => ScanScreen(cardMode: s.uri.queryParameters['mode'] == 'card')),
        GoRoute(path: '/create', builder: (_, s) => CreateScreen(editId: s.uri.queryParameters['story'])),
        GoRoute(path: '/made/:id', builder: (_, s) => MadeStoryScreen(id: s.pathParameters['id']!)),
        GoRoute(path: '/progress', builder: (_, _) => const ProgressScreen()),
        GoRoute(path: '/my-stories', builder: (_, _) => const MyStoriesScreen()),
        GoRoute(path: '/settings', builder: (_, _) => const SettingsGate()),
        GoRoute(path: '/profile', builder: (_, _) => const ProfileScreen()),
        GoRoute(path: '/help', builder: (_, _) => const HelpScreen()),
        GoRoute(path: '/admin', builder: (_, _) => const AdminScreen()),
      ],
    );

/// The bottom navigation bar for the five main tabs.
class TabShell extends StatelessWidget {
  final String location;
  final Widget child;
  const TabShell({super.key, required this.location, required this.child});

  static const _tabs = [
    ('/home', 'Home', Icons.home_outlined, Icons.home),
    ('/books', 'Books', Icons.menu_book_outlined, Icons.menu_book),
    ('/learn', 'Learn', Icons.school_outlined, Icons.school),
    ('/cards', 'Cards', Icons.style_outlined, Icons.style),
    ('/me', 'Me', Icons.person_outline, Icons.person),
  ];

  @override
  Widget build(BuildContext context) {
    final index = _tabs.indexWhere((t) => location.startsWith(t.$1)).clamp(0, 4);
    return Scaffold(
      body: child,
      bottomNavigationBar: Container(
        decoration: const BoxDecoration(boxShadow: [BoxShadow(color: Color(0x1A142938), blurRadius: 14, offset: Offset(0, -4))]),
        child: NavigationBar(
          selectedIndex: index,
          height: 68,
          onDestinationSelected: (i) => context.go(_tabs[i].$1),
          destinations: [
            for (final t in _tabs)
              NavigationDestination(
                icon: Icon(t.$3, color: C.muted),
                selectedIcon: Icon(t.$4, color: C.ink),
                label: t.$2,
              ),
          ],
        ),
      ),
    );
  }
}
