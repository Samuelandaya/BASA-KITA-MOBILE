using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using UnityEngine;
using Vuforia;

/// <summary>
/// The 3D storybook placed in the room, without a printed marker (see BasaKitaBridge.ShowBook).
///
/// Placing: a preview of the book floats in front of the camera, lying flat. On phones with ARCore,
/// Vuforia Ground Plane finds a table or the floor and the preview sits on it; "Place" anchors the
/// book there and it stays put as you walk around. On phones without ARCore the book is placed in
/// front of you and stays in that direction when you turn the phone (the gyroscope keeps it there),
/// but it cannot stay fixed if you walk.
///
/// The book model (StreamingAssets/models/book/*.glb, made by tools/make-book-glb.py) has the nodes
/// CoverPivot (hinge), Page3..Page10 (right-hand pages) and SceneAnchor.
/// </summary>
public class BookAR : MonoBehaviour
{
    public enum State { Loading, Placing, Placed, Open }
    public State Current { get; private set; } = State.Loading;

    // Where the book sits in front of the camera while placing, and how far away.
    static readonly Vector3 PreviewOffset = new Vector3(0f, -0.14f, 0.42f);

    Transform book, coverPivot, sceneAnchor;
    readonly Dictionary<int, GameObject> pages = new Dictionary<int, GameObject>();
    int shownPage;
    StoryScene scene;
    Transform sceneHolder;
    float scale = 1f;

    // Ground Plane (ARCore phones)
    PlaneFinderBehaviour planeFinder;
    AnchorBehaviour stage;
    HitTestResult lastHit;
    float lastHitTime = -10f;
    bool anchored;

    // Gyroscope placement (phones without ARCore): the book's pose in a gravity-aligned frame.
    bool gyroPlaced;
    Vector3 gyroPos;
    Quaternion gyroRot;

    Action<string> say;

    public static async Task<BookAR> Create(string modelName, PlaneFinderBehaviour planeFinder, AnchorBehaviour stage,
        Func<string, Task<GameObject>> loadRaw, Action<string> say)
    {
        var go = new GameObject("BookAR");
        var ar = go.AddComponent<BookAR>();
        ar.planeFinder = planeFinder;
        ar.stage = stage;
        ar.say = say;
        Input.gyro.enabled = true;
        var model = await loadRaw(modelName);
        if (model == null) { Destroy(go); return null; }
        ar.book = model.transform;
        ar.book.SetParent(go.transform, false);
        ar.coverPivot = Find(ar.book, "CoverPivot");
        ar.sceneAnchor = Find(ar.book, "SceneAnchor");
        for (int p = 3; p <= 10; p++)
        {
            var t = Find(ar.book, "Page" + p);
            if (t != null) { ar.pages[p] = t.gameObject; t.gameObject.SetActive(false); }
        }
        model.SetActive(true);
        ar.StartPlacing();
        return ar;
    }

    static Transform Find(Transform root, string name)
    {
        foreach (var t in root.GetComponentsInChildren<Transform>(true)) if (t.name == name) return t;
        return null;
    }

    // ---- placing ----

    public void StartPlacing()
    {
        Current = State.Placing;
        anchored = false;
        gyroPlaced = false;
        transform.SetParent(null, true);
        if (planeFinder != null) planeFinder.gameObject.SetActive(true);
        say("placing");
    }

    /// <summary>Called by the bridge for each automatic Ground Plane hit (only on phones with ARCore).</summary>
    public void OnPlaneHit(HitTestResult hit)
    {
        if (Current != State.Placing || hit == null) return;
        lastHit = hit;
        lastHitTime = Time.time;
    }

    bool SurfaceFound => Time.time - lastHitTime < 0.6f;

    public void Place()
    {
        if (Current != State.Placing) return;
        if (SurfaceFound && planeFinder != null && stage != null)
        {
            var positioning = planeFinder.GetComponent<ContentPositioningBehaviour>();
            positioning.DuplicateStage = false;
            positioning.AnchorStage = stage;
            positioning.PositionContentAtPlaneAnchor(lastHit);
            transform.SetParent(stage.transform, false);
            transform.localPosition = Vector3.zero;
            transform.localRotation = Quaternion.Euler(0f, FacingYaw(), 0f);
            anchored = true;
        }
        else
        {
            var g = DeviceRotation();
            gyroPos = g * PreviewOffset;
            gyroRot = Quaternion.Euler(0f, g.eulerAngles.y, 0f);
            gyroPlaced = true;
        }
        if (planeFinder != null) planeFinder.gameObject.SetActive(false);
        Current = State.Placed;
        say(anchored ? "placed-surface" : "placed");
    }

    float FacingYaw()
    {
        var cam = Camera.main;
        if (cam == null) return 0f;
        var f = cam.transform.forward;
        return Mathf.Atan2(f.x, f.z) * Mathf.Rad2Deg;
    }

    // Phone orientation in a gravity-aligned frame (y up), for portrait screens.
    static Quaternion DeviceRotation()
    {
        var q = Input.gyro.attitude;
        return Quaternion.Euler(90f, 0f, 0f) * new Quaternion(q.x, q.y, -q.z, -q.w);
    }

    void LateUpdate()
    {
        var cam = Camera.main;
        if (cam == null || book == null) return;
        HandleTouches(cam);
        if (anchored) return; // Ground Plane keeps it in place

        var g = DeviceRotation();
        var toCamera = Quaternion.Inverse(g);
        Vector3 localPos;
        Quaternion localRot;
        if (Current == State.Placing)
        {
            if (SurfaceFound && lastHit != null)
            {
                transform.position = lastHit.Position;
                transform.rotation = Quaternion.Euler(0f, FacingYaw(), 0f);
                transform.localScale = Vector3.one * scale;
                return;
            }
            // the preview stays in front of the camera, lying flat whatever the phone's tilt
            localPos = PreviewOffset;
            localRot = toCamera * Quaternion.Euler(0f, g.eulerAngles.y, 0f);
        }
        else if (gyroPlaced)
        {
            localPos = toCamera * gyroPos;
            localRot = toCamera * gyroRot;
        }
        else return;
        transform.position = cam.transform.TransformPoint(localPos);
        transform.rotation = cam.transform.rotation * localRot;
        transform.localScale = Vector3.one * scale;
    }

    // One finger moves the placed book across its table; two fingers pinch to resize it.
    Vector2 lastTouch;
    float lastPinch;
    void HandleTouches(Camera cam)
    {
        if (Current == State.Loading) return;
        if (Input.touchCount == 2)
        {
            float d = Vector2.Distance(Input.GetTouch(0).position, Input.GetTouch(1).position);
            if (Input.GetTouch(1).phase == TouchPhase.Began) lastPinch = d;
            else if (lastPinch > 0f) scale = Mathf.Clamp(scale * d / lastPinch, 0.5f, 2.5f);
            lastPinch = d;
            if (anchored) transform.localScale = Vector3.one * scale;
            return;
        }
        lastPinch = 0f;
        if (Input.touchCount != 1 || Current == State.Placing) return;
        var t = Input.GetTouch(0);
        if (t.phase == TouchPhase.Began) { lastTouch = t.position; return; }
        if (t.phase != TouchPhase.Moved) return;
        var delta = (t.position - lastTouch) / Screen.height;
        lastTouch = t.position;
        // move along the table: screen right/up become right/away on the book's plane
        var right = cam.transform.right; right.y = 0f; right.Normalize();
        var away = cam.transform.forward; away.y = 0f; away.Normalize();
        var move = (right * delta.x + away * delta.y) * 0.5f;
        if (anchored) transform.position += move;
        else if (gyroPlaced) gyroPos += DeviceRotation() * (Quaternion.Inverse(cam.transform.rotation) * move);
    }

    // ---- the book ----

    public void Open(StoryScene.Spec spec, int page, Func<string, float, Task<GameObject>> load)
    {
        if (Current == State.Placing || Current == State.Loading) return;
        StartCoroutine(OpenThenShow(spec, page, load));
    }

    IEnumerator OpenThenShow(StoryScene.Spec spec, int page, Func<string, float, Task<GameObject>> load)
    {
        ShowPagePicture(page);
        if (Current != State.Open && coverPivot != null)
        {
            const float duration = 1.2f;
            for (float t = 0f; t < duration; t += Time.deltaTime)
            {
                float k = Mathf.SmoothStep(0f, 1f, t / duration);
                coverPivot.localRotation = Quaternion.Euler(0f, 0f, 180f * k);
                yield return null;
            }
            coverPivot.localRotation = Quaternion.Euler(0f, 0f, 180f);
        }
        Current = State.Open;
        say("open");
        var task = BuildScene(spec, load);
        while (!task.IsCompleted) yield return null;
    }

    public void ShowPage(StoryScene.Spec spec, int page, Func<string, float, Task<GameObject>> load)
    {
        if (Current != State.Open) return;
        StartCoroutine(TurnTo(spec, page, load));
    }

    IEnumerator TurnTo(StoryScene.Spec spec, int page, Func<string, float, Task<GameObject>> load)
    {
        ClearScene();
        // a quick page turn: the old page folds over to the left, then the new one shows
        if (pages.TryGetValue(shownPage, out var old) && old != null)
        {
            const float duration = 0.45f;
            var t0 = old.transform.localRotation;
            for (float t = 0f; t < duration; t += Time.deltaTime)
            {
                old.transform.localRotation = t0 * Quaternion.Euler(0f, 0f, 180f * t / duration);
                yield return null;
            }
            old.transform.localRotation = t0;
        }
        ShowPagePicture(page);
        var task = BuildScene(spec, load);
        while (!task.IsCompleted) yield return null;
    }

    void ShowPagePicture(int page)
    {
        foreach (var kv in pages) kv.Value.SetActive(kv.Key == page);
        shownPage = page;
    }

    void ClearScene()
    {
        if (sceneHolder != null) Destroy(sceneHolder.gameObject);
        sceneHolder = null;
        scene = null;
    }

    async Task BuildScene(StoryScene.Spec spec, Func<string, float, Task<GameObject>> load)
    {
        ClearScene();
        if (spec == null || ((spec.props?.Length ?? 0) + (spec.actors?.Length ?? 0)) == 0)
        {
            say("scene-ready");
            return;
        }
        // Scene positions are in "page widths" measured on the printed page, where the drawing is in
        // the top half; centre the scene on the open book instead.
        const float width = 0.22f;
        var holder = new GameObject("scene holder").transform;
        holder.SetParent(sceneAnchor != null ? sceneAnchor : book, false);
        holder.localPosition = new Vector3(0f, 0f, -0.2f * width);
        sceneHolder = holder;
        scene = await StoryScene.Build(holder, spec, width, load);
        if (sceneHolder != holder) { if (scene != null) Destroy(holder.gameObject); return; } // a newer page won
        if (scene == null) return;
        scene.gameObject.SetActive(true);
        scene.OnShown();
        say("scene-ready");
    }

    public StoryScene Scene => scene;
}
