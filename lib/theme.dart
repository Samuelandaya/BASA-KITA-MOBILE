import 'package:flutter/material.dart';

/// Colors from the BASA-KITA design system (same tokens as the website).
class C {
  static const cream = Color(0xFFFFF9ED);
  static const cream2 = Color(0xFFFFF3D6);
  static const orange = Color(0xFFFF7A22);
  static const orangeDark = Color(0xFFD9600F);
  static const yellow = Color(0xFFFFE588);
  static const yellow2 = Color(0xFFF6C85F);
  static const green = Color(0xFF46B97B);
  static const greenText = Color(0xFF1D6B42);
  static const blue = Color(0xFF3E89C9);
  static const blueBtn = Color(0xFF2F6FA8);
  static const blueDark = Color(0xFF1F4F7A);
  static const navy = Color(0xFF183B5B);
  static const ink = Color(0xFF14293A);
  static const muted = Color(0xFF566879);
  static const border = Color(0xFFF3EEE3);
  static const cardBorder = Color(0xFFF2E4C4);
  static const red = Color(0xFFC7302A);
  static const white = Colors.white;
}

/// Fredoka for headings, Nunito for text, Chewy for fun labels, Luckiest Guy for pills.
/// The fonts are bundled in the app (assets/fonts), so they also work offline.
class T {
  static TextStyle head(double size, {Color color = C.navy, FontWeight weight = FontWeight.w600, double? height}) =>
      TextStyle(fontFamily: 'Fredoka', fontSize: size, color: color, fontWeight: weight, height: height);
  static TextStyle body(double size, {Color color = C.ink, FontWeight weight = FontWeight.w600, double? height}) =>
      TextStyle(fontFamily: 'Nunito', fontSize: size, color: color, fontWeight: weight, height: height);
  static TextStyle fun(double size, {Color color = C.orange}) => TextStyle(fontFamily: 'Chewy', fontSize: size, color: color);
  static TextStyle pill(double size, {Color color = C.ink}) =>
      TextStyle(fontFamily: 'LuckiestGuy', fontSize: size, color: color, letterSpacing: 0.5);
}

ThemeData buildTheme() {
  final base = ThemeData(
    useMaterial3: true,
    colorScheme: ColorScheme.fromSeed(seedColor: C.orange, primary: C.orange, secondary: C.blueBtn, surface: C.cream),
    scaffoldBackgroundColor: C.cream,
  );
  return base.copyWith(
    textTheme: base.textTheme.apply(fontFamily: 'Nunito', bodyColor: C.ink, displayColor: C.navy),
    appBarTheme: const AppBarTheme(backgroundColor: Colors.transparent, elevation: 0, scrolledUnderElevation: 0),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: Colors.white,
      contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
      hintStyle: T.body(16, color: C.muted),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: const BorderSide(color: Color(0xFFD9E2EC))),
      enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: const BorderSide(color: Color(0xFFD9E2EC))),
      focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: const BorderSide(color: C.blueBtn, width: 2)),
    ),
    navigationBarTheme: NavigationBarThemeData(
      backgroundColor: Colors.white,
      indicatorColor: C.orange,
      labelTextStyle: WidgetStatePropertyAll(T.body(12, weight: FontWeight.w800)),
    ),
  );
}
