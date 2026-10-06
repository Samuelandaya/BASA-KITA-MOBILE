import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'router.dart';
import 'services/sounds.dart';
import 'services/speech.dart';
import 'state/app_state.dart';
import 'theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final state = await AppState.load();
  runApp(ChangeNotifierProvider<AppState>.value(value: state, child: BasaKitaApp(state)));
}

class BasaKitaApp extends StatefulWidget {
  final AppState state;
  const BasaKitaApp(this.state, {super.key});
  @override
  State<BasaKitaApp> createState() => _BasaKitaAppState();
}

class _BasaKitaAppState extends State<BasaKitaApp> {
  late final _router = buildRouter(widget.state);

  // Keeps the voice, volume, sound effects and music in step with Settings.
  void _syncSettings() {
    final s = widget.state.settings;
    Speech.i.voiceId = s.voice;
    Speech.i.volume = s.volume / 100;
    Sounds.i.configure(volume: s.volume, effects: s.effects, music: s.music);
  }

  @override
  void initState() {
    super.initState();
    Speech.i.init();
    _syncSettings();
    widget.state.addListener(_syncSettings);
  }

  @override
  void dispose() {
    widget.state.removeListener(_syncSettings);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => MaterialApp.router(
        title: 'BASA-KITA',
        debugShowCheckedModeBanner: false,
        theme: buildTheme(),
        routerConfig: _router,
      );
}
