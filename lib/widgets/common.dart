import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../data/models.dart';
import '../state/app_state.dart';
import '../state/models.dart';
import '../theme.dart';

/// Yellow and green waves at the top, green and blue at the bottom (like the website).
class WavesPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size s) {
    Path wave(double y, double amp, double phase, {bool top = true}) {
      final p = Path()..moveTo(0, top ? 0 : s.height);
      p.lineTo(0, y);
      for (double x = 0; x <= s.width; x += 6) {
        p.lineTo(x, y + math.sin(x / s.width * math.pi * 2 + phase) * amp);
      }
      p.lineTo(s.width, top ? 0 : s.height);
      p.close();
      return p;
    }

    canvas.drawPath(wave(34, 10, 0.3), Paint()..color = C.yellow);
    canvas.drawPath(wave(18, 8, 2.2), Paint()..color = C.green);
    canvas.drawPath(wave(s.height - 26, 9, 1.0, top: false), Paint()..color = C.green);
    canvas.drawPath(wave(s.height - 12, 7, 3.0, top: false), Paint()..color = C.blue);
  }

  @override
  bool shouldRepaint(covariant CustomPainter old) => false;
}

/// The page frame: cream background, waves, an optional banner or title, and the body.
class AppPage extends StatelessWidget {
  final String? title;
  final String? subtitle;
  final String? banner;
  final bool back;
  final Widget child;
  final bool scroll;
  final Widget? trailing;
  final EdgeInsets padding;
  const AppPage({super.key, this.title, this.subtitle, this.banner, this.back = false, required this.child, this.scroll = true, this.trailing, this.padding = const EdgeInsets.fromLTRB(16, 4, 16, 28)});

  @override
  Widget build(BuildContext context) {
    final header = <Widget>[
      if (banner != null) TitleBanner(banner!, top: back ? 4 : 52),
      if (title != null)
        Padding(
          padding: const EdgeInsets.only(top: 4),
          child: Row(children: [
            Expanded(child: Text(title!, style: T.head(26, weight: FontWeight.w700, height: 1.1))),
            if (trailing != null) trailing!,
          ]),
        ),
      if (subtitle != null) Padding(padding: const EdgeInsets.only(top: 4), child: Text(subtitle!, style: T.body(14, color: C.muted))),
    ];
    final content = Padding(
      padding: padding,
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        if (!back && banner == null && header.isNotEmpty) const SizedBox(height: 38),
        if (back) BackLink(),
        ...header,
        if (header.isNotEmpty) const SizedBox(height: 14),
        scroll ? child : Expanded(child: child),
      ]),
    );
    return Scaffold(
      backgroundColor: C.cream,
      body: Stack(children: [
        Positioned.fill(child: CustomPaint(painter: WavesPainter())),
        SafeArea(child: scroll ? SingleChildScrollView(child: content) : content),
      ]),
    );
  }
}

class BackLink extends StatelessWidget {
  final String label;
  BackLink({super.key, this.label = 'Back'});
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(top: 40, bottom: 4),
        child: InkWell(
          onTap: () => context.canPop() ? context.pop() : context.go('/home'),
          borderRadius: BorderRadius.circular(20),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
            child: Row(mainAxisSize: MainAxisSize.min, children: [
              const Icon(Icons.arrow_back, size: 18, color: C.blueBtn),
              const SizedBox(width: 6),
              Text(label, style: T.body(14, color: C.blueBtn, weight: FontWeight.w800)),
            ]),
          ),
        ),
      );
}

/// Orange banner title with a blue shadow behind it.
class TitleBanner extends StatelessWidget {
  final String text;
  final double top;
  const TitleBanner(this.text, {super.key, this.top = 52});
  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.only(top: top, bottom: 6),
        child: SizedBox(
          height: 70,
          child: Stack(children: [
            Positioned(
              left: 8, right: 30, top: 10,
              child: Transform.rotate(angle: -0.03, child: Container(height: 56, decoration: BoxDecoration(color: C.blue, borderRadius: BorderRadius.circular(34)))),
            ),
            Positioned(
              left: 0, right: 40, top: 0,
              child: Transform.rotate(
                angle: -0.025,
                child: Container(
                  height: 58,
                  alignment: Alignment.center,
                  padding: const EdgeInsets.symmetric(horizontal: 18),
                  decoration: BoxDecoration(color: C.orange, borderRadius: BorderRadius.circular(32), border: Border.all(color: const Color(0xFFFFC9A0), width: 2)),
                  child: FittedBox(fit: BoxFit.scaleDown, child: Text(text, style: T.head(28, color: Colors.white, weight: FontWeight.w700))),
                ),
              ),
            ),
          ]),
        ),
      );
}

/// The rounded gradient panel that holds a page's content.
class Panel extends StatelessWidget {
  final Widget child;
  final EdgeInsets padding;
  const Panel({super.key, required this.child, this.padding = const EdgeInsets.all(16)});
  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        padding: padding,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          border: Border.all(color: const Color(0xFFF6D9B3)),
          gradient: const LinearGradient(begin: Alignment.topCenter, end: Alignment.bottomCenter, colors: [Color(0xFFFFF1BE), Color(0xFFE2F5CF), Color(0xFFE2F2FD)]),
        ),
        child: child,
      );
}

class WhiteCard extends StatelessWidget {
  final Widget child;
  final EdgeInsets padding;
  final VoidCallback? onTap;
  final Color color;
  final Color? border;
  const WhiteCard({super.key, required this.child, this.padding = const EdgeInsets.all(16), this.onTap, this.color = Colors.white, this.border});
  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(22), border: Border.all(color: border ?? C.cardBorder), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 10, offset: Offset(0, 4))]),
        child: Material(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(22),
          child: InkWell(onTap: onTap, borderRadius: BorderRadius.circular(22), child: Padding(padding: padding, child: child)),
        ),
      );
}

enum BtnKind { orange, blue, white, danger }

/// Pill buttons with the website's bottom edge.
class Btn extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final BtnKind kind;
  final bool big;
  final bool expand;
  const Btn(this.label, {super.key, this.onPressed, this.icon, this.kind = BtnKind.orange, this.big = false, this.expand = false});

  @override
  Widget build(BuildContext context) {
    final (bg, fg, edge) = switch (kind) {
      BtnKind.orange => (C.orange, C.ink, C.orangeDark),
      BtnKind.blue => (C.blueBtn, Colors.white, C.blueDark),
      BtnKind.danger => (C.red, Colors.white, const Color(0xFF8E211C)),
      BtnKind.white => (Colors.white, C.navy, C.cardBorder),
    };
    final enabled = onPressed != null;
    final btn = Opacity(
      opacity: enabled ? 1 : 0.5,
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onPressed,
          borderRadius: BorderRadius.circular(30),
          child: Container(
            constraints: BoxConstraints(minHeight: big ? 58 : 46),
            padding: EdgeInsets.symmetric(horizontal: big ? 28 : 20, vertical: 10),
            decoration: BoxDecoration(
              color: bg,
              borderRadius: BorderRadius.circular(30),
              border: kind == BtnKind.white ? Border.all(color: C.cardBorder, width: 2) : null,
              boxShadow: [BoxShadow(color: edge, offset: const Offset(0, 4))],
            ),
            child: Row(mainAxisSize: expand ? MainAxisSize.max : MainAxisSize.min, mainAxisAlignment: MainAxisAlignment.center, children: [
              if (icon != null) ...[Icon(icon, size: big ? 24 : 20, color: fg), if (label.isNotEmpty) const SizedBox(width: 8)],
              if (label.isNotEmpty) Flexible(child: Text(label, textAlign: TextAlign.center, style: T.body(big ? 18 : 15, color: fg, weight: FontWeight.w800))),
            ]),
          ),
        ),
      ),
    );
    return Padding(padding: const EdgeInsets.only(bottom: 4), child: expand ? SizedBox(width: double.infinity, child: btn) : btn);
  }
}

/// Small rounded label.
class Tag extends StatelessWidget {
  final String text;
  final Color bg;
  final Color fg;
  const Tag(this.text, {super.key, this.bg = C.yellow, this.fg = C.ink});
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(20)),
        child: Text(text.toUpperCase(), maxLines: 1, softWrap: false, style: T.body(11, color: fg, weight: FontWeight.w800).copyWith(letterSpacing: .6)),
      );
}

class Bar extends StatelessWidget {
  final int pct;
  final Color color;
  final double height;
  const Bar(this.pct, {super.key, this.color = C.blueBtn, this.height = 10});
  @override
  Widget build(BuildContext context) => ClipRRect(
        borderRadius: BorderRadius.circular(height),
        child: LinearProgressIndicator(value: (pct.clamp(0, 100)) / 100, minHeight: height, color: color, backgroundColor: const Color(0xFFD9E2EC)),
      );
}

Color levelBg(String l) => switch (l) { 'Growing' => const Color(0xFFDDF3E6), 'Confident' => const Color(0xFFE8F1FA), _ => const Color(0xFFFFF3D6) };
Color levelFg(String l) => switch (l) { 'Growing' => const Color(0xFF1D6B42), 'Confident' => const Color(0xFF1F4F7A), _ => C.navy };

/// The story's cover picture (or a color with "Aa").
class StoryCover extends StatelessWidget {
  final Story story;
  final double? height;
  final double radius;
  const StoryCover(this.story, {super.key, this.height, this.radius = 22});
  @override
  Widget build(BuildContext context) => ClipRRect(
        borderRadius: BorderRadius.circular(radius),
        child: Container(
          height: height,
          width: double.infinity,
          color: Color(story.color),
          child: story.cover.endsWith('.svg')
              ? SvgPicture.asset(story.cover, fit: BoxFit.cover)
              : Image.asset(story.cover, fit: BoxFit.cover),
        ),
      );
}

/// Heart that saves a storybook.
class SaveHeart extends StatelessWidget {
  final Story story;
  const SaveHeart(this.story, {super.key});
  @override
  Widget build(BuildContext context) {
    final on = context.select<AppState, bool>((s) => s.saved.contains(story.id));
    return Semantics(
      button: true,
      label: on ? 'Remove ${story.title} from saved stories' : 'Save ${story.title}',
      child: GestureDetector(
        onTap: () => context.read<AppState>().toggleSaved(story.id),
        child: Container(
          width: 44, height: 44,
          decoration: BoxDecoration(color: on ? const Color(0xFFFFE3EA) : Colors.white, shape: BoxShape.circle, border: Border.all(color: on ? const Color(0xFFE86A8A) : C.cardBorder, width: 2), boxShadow: const [BoxShadow(color: Color(0x1F142938), blurRadius: 6, offset: Offset(0, 3))]),
          child: Icon(on ? Icons.favorite : Icons.favorite_border, size: 22, color: on ? const Color(0xFFE86A8A) : const Color(0xFFC9506B)),
        ),
      ),
    );
  }
}

/// A pill-shaped storybook row (cover, title, tags, progress, heart).
class BookRow extends StatelessWidget {
  final Story story;
  const BookRow(this.story, {super.key});
  @override
  Widget build(BuildContext context) {
    final entry = context.select<AppState, Progress?>((s) => s.progress[story.id]);
    final pct = percentRead(story, entry);
    return Stack(children: [
      Container(
        decoration: BoxDecoration(color: const Color(0xFFFFF8E3), borderRadius: BorderRadius.circular(34), border: Border.all(color: const Color(0xFFF6E3B8)), boxShadow: const [BoxShadow(color: Color(0x14142938), blurRadius: 12, offset: Offset(0, 6))]),
        child: Material(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(34),
          child: InkWell(
            onTap: () => context.push('/books/${story.id}'),
            borderRadius: BorderRadius.circular(34),
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(children: [
              SizedBox(width: 104, height: 104, child: StoryCover(story, radius: 26)),
              const SizedBox(width: 14),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(right: 40),
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text(story.title, maxLines: 2, overflow: TextOverflow.ellipsis, style: T.head(19, height: 1.15)),
                    const SizedBox(height: 8),
                    Wrap(spacing: 6, runSpacing: 6, children: [Tag(story.language), if (story.shownLevel != story.language) Tag(story.shownLevel, bg: C.orange)]),
                    if (entry != null) ...[
                      const SizedBox(height: 8),
                      Row(children: [Expanded(child: Bar(pct, height: 7)), const SizedBox(width: 8), Text(entry.finished ? 'Done' : '$pct%', style: T.body(11, color: C.muted, weight: FontWeight.w800))]),
                    ],
                  ]),
                ),
              ),
              ]),
            ),
          ),
        ),
      ),
      Positioned(right: 10, top: 10, child: SaveHeart(story)),
    ]);
  }
}

/// A square-ish story card for horizontal lists.
class StoryCard extends StatelessWidget {
  final Story story;
  final String? note;
  final int? pct;
  final double width;
  const StoryCard(this.story, {super.key, this.note, this.pct, this.width = 170});
  @override
  Widget build(BuildContext context) => SizedBox(
        width: width,
        child: Stack(children: [
          WhiteCard(
            padding: const EdgeInsets.all(10),
            onTap: () => context.push('/books/${story.id}'),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              StoryCover(story, height: 96, radius: 16),
              const SizedBox(height: 8),
              Text(story.title, maxLines: 2, overflow: TextOverflow.ellipsis, style: T.head(15, height: 1.15)),
              const SizedBox(height: 6),
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Tag(story.language, bg: const Color(0xFFFFF3D6)), Text('${story.minutes} min', style: T.body(11, color: C.muted, weight: FontWeight.w800))]),
              if (pct != null) ...[const SizedBox(height: 8), Bar(pct!, height: 7)],
            ]),
          ),
          if (note != null) Positioned(left: 8, top: 8, child: Container(padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3), decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12), boxShadow: const [BoxShadow(color: Color(0x1F142938), blurRadius: 4)]), child: Text(note!, style: T.body(10, weight: FontWeight.w800, color: C.navy)))),
          Positioned(right: 8, top: 8, child: SaveHeart(story)),
        ]),
      );
}

class SectionTitle extends StatelessWidget {
  final String text;
  final String? action;
  final VoidCallback? onAction;
  const SectionTitle(this.text, {super.key, this.action, this.onAction});
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(top: 6, bottom: 10),
        child: Row(children: [
          Expanded(child: Text(text, style: T.head(20))),
          if (action != null) GestureDetector(onTap: onAction, child: Text(action!, style: T.body(14, color: C.blueBtn, weight: FontWeight.w800))),
        ]),
      );
}

/// Segmented pill control.
class Seg<V> extends StatelessWidget {
  final List<V> options;
  final V value;
  final ValueChanged<V> onChanged;
  final String Function(V) label;
  const Seg({super.key, required this.options, required this.value, required this.onChanged, required this.label});
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(4),
        decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(30), border: Border.all(color: C.cardBorder)),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          for (final o in options)
            GestureDetector(
              onTap: () => onChanged(o),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
                decoration: BoxDecoration(color: o == value ? C.blueBtn : Colors.transparent, borderRadius: BorderRadius.circular(24)),
                child: Text(label(o), style: T.body(13, color: o == value ? Colors.white : C.muted, weight: FontWeight.w800)),
              ),
            ),
        ]),
      );
}

/// Horizontally scrollable [Seg] for many options.
class ScrollSeg<V> extends StatelessWidget {
  final List<V> options;
  final V value;
  final ValueChanged<V> onChanged;
  final String Function(V) label;
  const ScrollSeg({super.key, required this.options, required this.value, required this.onChanged, required this.label});
  @override
  Widget build(BuildContext context) => SingleChildScrollView(scrollDirection: Axis.horizontal, child: Seg<V>(options: options, value: value, onChanged: onChanged, label: label));
}

class FormError extends StatelessWidget {
  final String text;
  const FormError(this.text, {super.key});
  @override
  Widget build(BuildContext context) => text.isEmpty
      ? const SizedBox.shrink()
      : Padding(padding: const EdgeInsets.only(bottom: 10), child: Text(text, textAlign: TextAlign.center, style: T.body(14, color: C.red, weight: FontWeight.w800)));
}

Future<void> showMessage(BuildContext context, String title, String message, {String button = 'OK'}) => showDialog<void>(
      context: context,
      builder: (c) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
        title: Text(title, style: T.head(22), textAlign: TextAlign.center),
        content: Text(message, style: T.body(15, color: C.muted), textAlign: TextAlign.center),
        actionsAlignment: MainAxisAlignment.center,
        actions: [Btn(button, onPressed: () => Navigator.pop(c))],
      ),
    );
