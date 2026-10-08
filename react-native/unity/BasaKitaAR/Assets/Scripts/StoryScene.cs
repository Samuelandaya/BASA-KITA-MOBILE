using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using UnityEngine;

/// <summary>
/// A small 3D scene that stands on a printed story page (a diorama): scenery props plus
/// characters ("actors") that act out the page while it is read aloud. The app describes the
/// scene (ShowPage "scene") and sends cues as the narration reaches certain words (Cue).
///
/// Positions are in page widths, measured from the page's centre: x to the right, z towards the
/// top of the page. Sizes are the model's biggest side, also in page widths.
/// </summary>
public class StoryScene : MonoBehaviour
{
    [Serializable] public class Piece { public string id; public string m; public float x; public float z; public float s = 0.1f; public float r; public string anim; }
    [Serializable] public class Spec { public Piece[] props; public Piece[] actors; }

    private readonly Dictionary<string, StoryActor> actors = new Dictionary<string, StoryActor>();
    private float pageWidth;
    private bool risen;

    /// <summary>Builds the scene under `parent` (the page's Image Target). Models are loaded with `load(name, sizeMeters)`.</summary>
    public static async Task<StoryScene> Build(Transform parent, Spec spec, float pageWidth, Func<string, float, Task<GameObject>> load)
    {
        var go = new GameObject("story scene");
        go.transform.SetParent(parent, false);
        var scene = go.AddComponent<StoryScene>();
        scene.pageWidth = pageWidth;
        foreach (var p in spec.props ?? new Piece[0])
        {
            var model = await load("scene/" + p.m, p.s * pageWidth);
            if (model == null) continue;
            scene.Place(model.transform, p);
            model.SetActive(true);
        }
        foreach (var a in spec.actors ?? new Piece[0])
        {
            var model = await load(a.m, a.s * pageWidth);
            if (model == null) continue;
            scene.Place(model.transform, a);
            model.SetActive(true);
            var actor = model.AddComponent<StoryActor>();
            actor.Init(a, pageWidth);
            scene.actors[string.IsNullOrEmpty(a.id) ? a.m : a.id] = actor;
        }
        go.transform.localScale = Vector3.zero; // grows out of the page the first time it is seen
        return scene;
    }

    private void Place(Transform t, Piece p)
    {
        t.SetParent(transform, false);
        t.localPosition = new Vector3(p.x * pageWidth, 0f, p.z * pageWidth);
        t.localRotation = Quaternion.Euler(0f, p.r, 0f);
    }

    /// <summary>Called when the page is found: the scene rises out of the page once.</summary>
    public void OnShown()
    {
        if (risen) return;
        risen = true;
        StartCoroutine(Rise());
    }

    private IEnumerator Rise()
    {
        const float duration = 0.9f;
        for (float t = 0f; t < duration; t += Time.deltaTime)
        {
            float k = t / duration;
            float ease = 1f + 2.7f * Mathf.Pow(k - 1f, 3f) + 1.7f * Mathf.Pow(k - 1f, 2f); // ease-out with a small overshoot
            transform.localScale = new Vector3(1f, Mathf.Max(0.01f, ease), 1f) * Mathf.Clamp01(k * 3f);
            yield return null;
        }
        transform.localScale = Vector3.one;
    }

    public void Cue(string actor, string action, float x, float z)
    {
        if (actors.TryGetValue(actor, out var a)) a.Do(action, x, z);
    }

    /// <summary>Back to how the scene started, for "Read again".</summary>
    public void ResetScene()
    {
        foreach (var a in actors.Values) a.ResetToStart();
    }
}

/// <summary>One character in a story scene: plays its glTF animations and moves around the page.</summary>
public class StoryActor : MonoBehaviour
{
    private StoryScene.Piece start;
    private float pageWidth;
    private Animation anim;
    private Coroutine running;
    private Vector3 baseScale;

    public void Init(StoryScene.Piece piece, float width)
    {
        start = piece;
        pageWidth = width;
        anim = GetComponentInChildren<Animation>();
        baseScale = transform.localScale;
        Play(string.IsNullOrEmpty(piece.anim) ? "Idle" : piece.anim);
    }

    public void ResetToStart()
    {
        if (running != null) StopCoroutine(running);
        running = null;
        transform.localPosition = new Vector3(start.x * pageWidth, 0f, start.z * pageWidth);
        transform.localRotation = Quaternion.Euler(0f, start.r, 0f);
        transform.localScale = baseScale;
        Play(string.IsNullOrEmpty(start.anim) ? "Idle" : start.anim);
    }

    public void Do(string action, float x, float z)
    {
        if (running != null) StopCoroutine(running);
        transform.localScale = baseScale;
        switch (action)
        {
            case "walkTo": running = StartCoroutine(MoveTo(new Vector3(x * pageWidth, 0f, z * pageWidth), "Walk", 0.35f)); break;
            case "runTo": running = StartCoroutine(MoveTo(new Vector3(x * pageWidth, 0f, z * pageWidth), "Run", 0.8f)); break;
            case "runAround": running = StartCoroutine(RunAround(2.4f)); break;
            case "jump": running = StartCoroutine(Hop(2)); break;
            case "eat": running = StartCoroutine(PlayFor("Idle_Eating", 2.2f)); break;
            case "spin": running = StartCoroutine(Spin(1.4f)); break;
            case "look": running = StartCoroutine(TurnTo(CameraDirection(), 0.5f)); break;
            case "wiggle": running = StartCoroutine(Wiggle(1.0f)); break;
            default: Play(action); break; // an animation by name, e.g. "Idle"
        }
    }

    // ---- animations: glTF clip names look like "AnimalArmature|...|Walk", so match the last part ----

    private void Play(string name, float fade = 0.2f)
    {
        if (anim == null) return;
        foreach (AnimationState s in anim)
        {
            var clip = s.name;
            int bar = clip.LastIndexOf('|');
            if ((bar >= 0 ? clip.Substring(bar + 1) : clip) == name)
            {
                s.wrapMode = WrapMode.Loop;
                anim.CrossFade(clip, fade);
                return;
            }
        }
    }

    private Vector3 CameraDirection()
    {
        var cam = Camera.main;
        if (cam == null || transform.parent == null) return Vector3.back;
        var local = transform.parent.InverseTransformPoint(cam.transform.position) - transform.localPosition;
        local.y = 0f;
        return local.sqrMagnitude > 1e-6f ? local.normalized : Vector3.back;
    }

    private IEnumerator TurnTo(Vector3 dir, float seconds)
    {
        var from = transform.localRotation;
        var to = Quaternion.LookRotation(dir, Vector3.up);
        for (float t = 0f; t < seconds; t += Time.deltaTime)
        {
            transform.localRotation = Quaternion.Slerp(from, to, t / seconds);
            yield return null;
        }
        transform.localRotation = to;
    }

    private IEnumerator MoveTo(Vector3 target, string clip, float pageWidthsPerSecond)
    {
        var dir = target - transform.localPosition;
        dir.y = 0f;
        if (dir.sqrMagnitude > 1e-8f) yield return TurnTo(dir.normalized, 0.25f);
        Play(clip);
        float speed = pageWidthsPerSecond * pageWidth;
        while ((transform.localPosition - target).sqrMagnitude > 1e-8f)
        {
            transform.localPosition = Vector3.MoveTowards(transform.localPosition, target, speed * Time.deltaTime);
            yield return null;
        }
        Play("Idle");
        yield return TurnTo(CameraDirection(), 0.4f);
    }

    private IEnumerator RunAround(float seconds)
    {
        Play("Run");
        var centre = transform.localPosition;
        float radius = 0.12f * pageWidth;
        centre -= transform.localRotation * Vector3.right * radius;
        float a0 = Mathf.Atan2(transform.localPosition.z - centre.z, transform.localPosition.x - centre.x);
        for (float t = 0f; t < seconds; t += Time.deltaTime)
        {
            float a = a0 - t / seconds * Mathf.PI * 2f;
            var p = centre + new Vector3(Mathf.Cos(a), 0f, Mathf.Sin(a)) * radius;
            var forward = new Vector3(Mathf.Sin(a), 0f, -Mathf.Cos(a));
            transform.localPosition = p;
            transform.localRotation = Quaternion.LookRotation(forward, Vector3.up);
            yield return null;
        }
        Play("Idle");
        yield return TurnTo(CameraDirection(), 0.4f);
    }

    private IEnumerator Hop(int times)
    {
        var ground = transform.localPosition;
        float height = 0.08f * pageWidth;
        for (int i = 0; i < times; i++)
        {
            Play("Jump_Loop", 0.1f);
            for (float t = 0f; t < 0.45f; t += Time.deltaTime)
            {
                transform.localPosition = ground + Vector3.up * Mathf.Sin(t / 0.45f * Mathf.PI) * height;
                yield return null;
            }
        }
        transform.localPosition = ground;
        Play("Idle");
    }

    private IEnumerator PlayFor(string clip, float seconds)
    {
        Play(clip);
        yield return new WaitForSeconds(seconds);
        Play("Idle");
    }

    private IEnumerator Spin(float seconds)
    {
        var from = transform.localRotation;
        for (float t = 0f; t < seconds; t += Time.deltaTime)
        {
            transform.localRotation = from * Quaternion.Euler(0f, 360f * t / seconds, 0f);
            yield return null;
        }
        transform.localRotation = from;
    }

    // A fluffy shake: a quick squash and stretch.
    private IEnumerator Wiggle(float seconds)
    {
        for (float t = 0f; t < seconds; t += Time.deltaTime)
        {
            float w = Mathf.Sin(t * 30f) * 0.06f * (1f - t / seconds);
            transform.localScale = new Vector3(baseScale.x * (1f + w), baseScale.y * (1f - w), baseScale.z * (1f + w));
            yield return null;
        }
        transform.localScale = baseScale;
    }
}
