import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../lock.dart';
import '../services/notifications.dart';
import '../services/sounds.dart';
import '../services/speech.dart';
import '../state/app_state.dart';
import '../theme.dart';
import '../widgets/common.dart';
import 'me.dart' show confirmLogout;

/// Settings are behind the parent PIN when the parent lock is on.
class SettingsGate extends StatefulWidget {
  const SettingsGate({super.key});
  @override
  State<SettingsGate> createState() => _SettingsGateState();
}

class _SettingsGateState extends State<SettingsGate> {
  final pin = TextEditingController();
  String error = '';

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final s = app.settings;
    if (!(s.parentLock && s.pin.isNotEmpty) || isUnlocked(app.user?.email)) return const SettingsScreen();
    return AppPage(
      back: true,
      title: 'Settings',
      subtitle: 'Grown-ups only. Enter the parent PIN to continue.',
      child: WhiteCard(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Parent PIN', style: T.body(14, weight: FontWeight.w800)),
          const SizedBox(height: 6),
          TextField(controller: pin, obscureText: true, keyboardType: TextInputType.number, maxLength: 4, inputFormatters: [FilteringTextInputFormatter.digitsOnly], style: T.body(20), decoration: const InputDecoration(counterText: '')),
          if (error.isNotEmpty) Padding(padding: const EdgeInsets.only(top: 8), child: Text(error, style: T.body(14, color: C.red, weight: FontWeight.w800))),
          const SizedBox(height: 12),
          Btn('Unlock', onPressed: () {
            if (pin.text == s.pin) {
              unlock(app.user?.email);
              setState(() => error = '');
            } else {
              setState(() {
                error = 'That PIN isn’t right. Try again.';
                pin.clear();
              });
            }
          }),
        ]),
      ),
    );
  }
}

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});
  @override
  State<SettingsScreen> createState() => _SettingsState();
}

class _SettingsState extends State<SettingsScreen> {
  String reminderNote = '';

  Widget _row(String label, Widget trailing, {String? hint}) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Row(children: [
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(label, style: T.body(15, weight: FontWeight.w800, color: C.navy)), if (hint != null) Text(hint, style: T.body(12, color: C.muted))])),
          trailing,
        ]),
      );

  Widget _switch(String label, bool value, ValueChanged<bool> onChanged, {String? hint}) => _row(label, Switch(value: value, onChanged: onChanged, activeTrackColor: C.green, thumbColor: const WidgetStatePropertyAll(Colors.white)), hint: hint);

  Widget _section(String title, List<Widget> children) => Padding(
        padding: const EdgeInsets.only(bottom: 14),
        child: WhiteCard(
          padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(title, style: T.head(20)), const Divider(color: C.border), ...children]),
        ),
      );

  Future<void> toggleReminder(AppState app, bool on) async {
    if (!on) {
      await setDailyReminder(false);
      app.updateSettings((s) => s.reminder = false);
      setState(() => reminderNote = '');
      return;
    }
    final ok = await setDailyReminder(true);
    app.updateSettings((s) => s.reminder = ok);
    setState(() => reminderNote = ok ? 'Reminder on! You’ll get a notification every day at 4:00 PM, even when the app is closed.' : 'Notifications are blocked. Allow them in your phone settings to get reminders.');
  }

  Future<void> setPin(AppState app) async {
    final pin = await showDialog<String>(context: context, builder: (_) => const _PinDialog());
    if (pin != null) {
      app.updateSettings((s) {
        s.parentLock = true;
        s.pin = pin;
      });
      unlock(app.user?.email);
    }
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final s = app.settings;
    final current = voiceProfiles.firstWhere((p) => p.id == s.voice, orElse: () => voiceProfiles.first);
    final real = Speech.i.pickVoice('en-US', current.id);
    return AppPage(
      back: true,
      title: 'Settings',
      subtitle: 'Make reading just right for you.',
      child: Column(children: [
        _section('Reading', [
          _row('Story language', ScrollSeg<String>(options: const ['Filipino', 'English', 'Bilingual'], value: s.language, label: (v) => v, onChanged: (v) => app.updateSettings((x) => x.language = v))),
          _row('Read-aloud speed', Seg<double>(options: const [0.75, 1.0, 1.25], value: s.speed, label: (v) => '$v×', onChanged: (v) => app.updateSettings((x) => x.speed = v))),
          _row('Text size', Seg<String>(options: const ['Normal', 'Big', 'Bigger'], value: s.textSize, label: (v) => v, onChanged: (v) => app.updateSettings((x) => x.textSize = v))),
          _switch('Highlight words while reading', s.highlight, (v) => app.updateSettings((x) => x.highlight = v)),
        ]),
        _section('Sounds', [
          _switch('Story narration', s.narration, (v) => app.updateSettings((x) => x.narration = v)),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 10),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Voice', style: T.body(15, weight: FontWeight.w800, color: C.navy)),
              Text('${current.blurb}${real != null ? ' · ${(real['name'] ?? '').replaceAll(RegExp(r'^Microsoft |^Google '), '')}' : ''}', style: T.body(12, color: C.muted)),
              const SizedBox(height: 8),
              Row(children: [
                Expanded(
                  child: DropdownButtonFormField<String>(
                    initialValue: current.id,
                    isExpanded: true,
                    decoration: const InputDecoration(contentPadding: EdgeInsets.symmetric(horizontal: 14, vertical: 8)),
                    style: T.body(15, weight: FontWeight.w700),
                    items: [for (final p in voiceProfiles) DropdownMenuItem(value: p.id, child: Text(p.name))],
                    onChanged: (v) {
                      if (v == null) return;
                      app.updateSettings((x) => x.voice = v);
                      final p = voiceProfiles.firstWhere((x) => x.id == v);
                      Speech.i.speak(p.sample, lang: 'en-US', rate: .95, voiceId: p.id);
                    },
                  ),
                ),
                const SizedBox(width: 10),
                Btn('Hear', icon: Icons.volume_up, kind: BtnKind.white, onPressed: () => Speech.i.speak(current.sample, lang: 'en-US', rate: .95, voiceId: current.id)),
              ]),
              if (real != null && !isNaturalVoice(real)) Padding(padding: const EdgeInsets.only(top: 6), child: Text('This phone only has a basic voice, so it may sound like a robot. Install a better voice in your phone’s text-to-speech settings.', style: T.body(12, color: C.muted))),
            ]),
          ),
          _switch('Sound effects', s.effects, (v) {
            app.updateSettings((x) => x.effects = v);
            if (v) Sounds.i.play('good');
          }),
          _switch('Background music', s.music, (v) => app.updateSettings((x) => x.music = v)),
          _row('Volume', SizedBox(width: 180, child: Slider(value: s.volume.toDouble(), min: 0, max: 100, activeColor: C.blueBtn, onChanged: (v) => app.updateSettings((x) => x.volume = v.round())))),
        ]),
        _section('For grown-ups', [
          _switch('Parent / teacher lock', s.parentLock, (v) => v && s.pin.isEmpty ? setPin(app) : app.updateSettings((x) => x.parentLock = v), hint: 'Ask for a 4-digit PIN before opening Settings'),
          if (s.parentLock && s.pin.isNotEmpty) Align(alignment: Alignment.centerRight, child: Btn('Change PIN', kind: BtnKind.white, onPressed: () => setPin(app))),
          _switch('Daily reading reminder', s.reminder, (v) => toggleReminder(app, v), hint: 'Every day at 4:00 PM'),
          if (reminderNote.isNotEmpty) Padding(padding: const EdgeInsets.only(bottom: 8), child: Text(reminderNote, style: T.body(13, color: C.muted, weight: FontWeight.w700))),
        ]),
        _section('Account', [
          _row('Signed in as', Flexible(child: Text(app.user?.email ?? '', overflow: TextOverflow.ellipsis, style: T.body(13, color: C.muted)))),
          Wrap(spacing: 10, children: [
            Btn('My Profile', kind: BtnKind.white, onPressed: () => context.push('/profile')),
            Btn('Log out', icon: Icons.logout, kind: BtnKind.white, onPressed: () => confirmLogout(context)),
          ]),
          const SizedBox(height: 8),
        ]),
      ]),
    );
  }
}

class _PinDialog extends StatefulWidget {
  const _PinDialog();
  @override
  State<_PinDialog> createState() => _PinDialogState();
}

class _PinDialogState extends State<_PinDialog> {
  final a = TextEditingController();
  final b = TextEditingController();
  String error = '';

  Widget _f(String label, TextEditingController c) => Padding(
        padding: const EdgeInsets.only(bottom: 10),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label, style: T.body(14, weight: FontWeight.w800)),
          const SizedBox(height: 4),
          TextField(controller: c, obscureText: true, keyboardType: TextInputType.number, maxLength: 4, inputFormatters: [FilteringTextInputFormatter.digitsOnly], style: T.body(18), decoration: const InputDecoration(counterText: '')),
        ]),
      );

  @override
  Widget build(BuildContext context) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        title: Text('Set a parent PIN', style: T.head(22)),
        content: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Grown-ups enter this PIN to open Settings.', style: T.body(14, color: C.muted)),
          const SizedBox(height: 12),
          _f('New PIN', a),
          _f('Type it again', b),
          if (error.isNotEmpty) Text(error, style: T.body(13, color: C.red, weight: FontWeight.w800)),
        ]),
        actions: [
          Btn('Cancel', kind: BtnKind.white, onPressed: () => Navigator.pop(context)),
          Btn('Save PIN', onPressed: () {
            if (!RegExp(r'^\d{4}$').hasMatch(a.text)) return setState(() => error = 'The PIN needs exactly 4 numbers.');
            if (a.text != b.text) return setState(() => error = 'The two PINs don’t match.');
            Navigator.pop(context, a.text);
          }),
        ],
      );
}
