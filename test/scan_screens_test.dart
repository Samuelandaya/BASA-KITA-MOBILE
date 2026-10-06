import 'package:basa_kita/screens/scan.dart';
import 'package:basa_kita/state/app_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

Future<void> show(WidgetTester tester, Widget screen) async {
  SharedPreferences.setMockInitialValues({});
  final state = await AppState.load();
  tester.view.physicalSize = const Size(800, 1600);
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(ChangeNotifierProvider<AppState>.value(value: state, child: MaterialApp(home: screen)));
  await tester.pump();
}

void main() {
  testWidgets('Scan a Book scans books only, with no flash card switch', (tester) async {
    await show(tester, const ScanScreen());
    expect(find.text('Scan a Book'), findsOneWidget);
    expect(find.text('Take a picture of the page'), findsOneWidget);
    expect(find.text('Flash card'), findsNothing);
  });

  testWidgets('Scan a Flash Card is its own page', (tester) async {
    await show(tester, const ScanCardScreen());
    expect(find.text('Scan a Flash Card'), findsOneWidget);
    expect(find.text('Point the camera at a flashcard'), findsOneWidget);
    expect(find.text('Book page'), findsNothing);
  });
}
