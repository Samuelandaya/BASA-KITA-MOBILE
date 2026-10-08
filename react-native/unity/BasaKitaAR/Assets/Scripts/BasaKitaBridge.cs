using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using GLTFast;
using UnityEngine;
using Vuforia;

/// <summary>
/// Receives messages from the React Native app and shows a flashcard's 3D picture with Vuforia.
/// Put this on a GameObject named exactly "BasaKitaBridge" (the app sends messages to that name).
/// The message format is in react-native/src/ar/bridge.ts; keep the two files in step.
///
/// App -> Unity:  Configure {"licenseKey":"..."}   ShowCard {"word":"bola","mode":"ground"|"card"}   Stop {}
///                ShowPage {"target":"butit-kulit-page-3","models":["aso"],"widthMeters":0.21,"scene":{...}}
///                Cue {"actor":"butit","action":"walkTo","x":0,"z":0.2}   ResetScene {}
/// Unity -> App:  ready, searching, tracking, lost, error   (see Say* methods below)
/// </summary>
public class BasaKitaBridge : MonoBehaviour
{
    [Header("Ground Plane: 3D picture on a table or the floor")]
    [Tooltip("The Plane Finder object (it has a ContentPositioningBehaviour).")]
    public PlaneFinderBehaviour planeFinder;
    [Tooltip("The Ground Plane Stage. The 3D picture is placed under it.")]
    public AnchorBehaviour groundStage;

    [Header("Image Targets: 3D picture on a printed flashcard")]
    [Tooltip("Parent of the Image Targets. Name each Image Target after its word, e.g. bola.")]
    public Transform cardTargets;

    [Tooltip("How big the 3D picture is, in meters.")]
    public float modelSize = 0.15f;

    [Header("Trying it in the Unity Editor (Play mode)")]
    public string editorLicenseKey = "";
    public string editorWord = "bola";

    [Serializable] private class ConfigureMessage { public string licenseKey; }
    [Serializable] private class ShowCardMessage { public string word; public string mode; }
    [Serializable] private class ShowPageMessage { public string target; public string[] models; public float widthMeters; public StoryScene.Spec scene; }
    [Serializable] private class CueMessage { public string actor; public string action; public float x; public float z; }
    [Serializable] private class OutMessage { public string type; public string word; public string mode; public string code; public string message; }

    private string word;
    private string mode = "ground";
    private GameObject model;
    private ObserverBehaviour watched;
    private bool started;
    private bool placed;
    private bool vuforiaRunning;
    private ShowPageMessage pendingPage;
    private int pageRequest; // newest ShowPage wins when several load at once
    private StoryScene storyScene; // the page's 3D scene, when the page has one
    // Story pages made into Image Targets at runtime, by target name (StreamingAssets/pages/<name>.jpg).
    private readonly Dictionary<string, ImageTargetBehaviour> pageTargets = new Dictionary<string, ImageTargetBehaviour>();

    private void Start()
    {
        if (planeFinder != null)
        {
            planeFinder.OnAutomaticHitTest.AddListener(OnAutomaticHitTest);
            planeFinder.gameObject.SetActive(false);
        }
        Say(new OutMessage { type = "ready" });
#if UNITY_EDITOR
        Configure(JsonUtility.ToJson(new ConfigureMessage { licenseKey = editorLicenseKey }));
        ShowCard(JsonUtility.ToJson(new ShowCardMessage { word = editorWord, mode = "ground" }));
#endif
    }

    // ---- messages from the app (called by name through UnitySendMessage) ----

    /// <summary>Starts Vuforia with the license key. Needs "Delayed Initialization" ticked in the Vuforia Configuration.</summary>
    public void Configure(string json)
    {
        var m = JsonUtility.FromJson<ConfigureMessage>(json);
        if (m == null || string.IsNullOrWhiteSpace(m.licenseKey) || m.licenseKey.Trim() == "YOUR_VUFORIA_LICENSE_KEY")
        {
            SayError("LICENSE", "No Vuforia license key was given.");
            return;
        }
        if (started) return;
        started = true;
        VuforiaConfiguration.Instance.Vuforia.LicenseKey = m.licenseKey.Trim();
        VuforiaApplication.Instance.OnVuforiaInitialized += OnVuforiaInitialized;
        VuforiaApplication.Instance.OnVuforiaStarted += OnVuforiaStarted;
        VuforiaApplication.Instance.Initialize();
    }

    public async void ShowCard(string json)
    {
        var m = JsonUtility.FromJson<ShowCardMessage>(json);
        if (m == null || string.IsNullOrEmpty(m.word)) return;
        bool sameWord = m.word == word && model != null;
        word = m.word;
        mode = m.mode == "card" ? "card" : "ground";
        if (!sameWord)
        {
            if (model != null) Destroy(model);
            model = await LoadModel(word);
            if (model == null)
            {
                SayError("MODEL", "No 3D picture for " + word + ".");
                return;
            }
        }
        Place();
    }

    /// <summary>
    /// Puts a story page's 3D models on that printed page. The page picture becomes a Vuforia
    /// Image Target at runtime, so no target database is needed and it works without ARCore.
    /// </summary>
    public async void ShowPage(string json)
    {
        var m = JsonUtility.FromJson<ShowPageMessage>(json);
        if (m == null || string.IsNullOrEmpty(m.target)) return;
        pendingPage = m;
        mode = "page";
        word = m.target;
        if (vuforiaRunning) await PlacePage(m);
    }

    /// <summary>A character in the page's scene acts, as the narration reaches a word.</summary>
    public void Cue(string json)
    {
        var m = JsonUtility.FromJson<CueMessage>(json);
        if (m != null && storyScene != null) storyScene.Cue(m.actor, m.action, m.x, m.z);
    }

    public void ResetScene(string _)
    {
        if (storyScene != null) storyScene.ResetScene();
    }

    public void Stop(string _)
    {
        pendingPage = null;
        pageRequest++;
        storyScene = null;
        Unwatch();
        if (model != null) Destroy(model);
        model = null;
        word = null;
        if (planeFinder != null) planeFinder.gameObject.SetActive(false);
    }

    // ---- Vuforia ----

    private async void OnVuforiaStarted()
    {
        vuforiaRunning = true;
        if (pendingPage != null) await PlacePage(pendingPage);
    }

    private async Task PlacePage(ShowPageMessage m)
    {
        int request = ++pageRequest;
        Unwatch();
        if (model != null) Destroy(model);
        model = null;
        if (planeFinder != null) planeFinder.gameObject.SetActive(false);

        float width = m.widthMeters > 0f ? m.widthMeters : 0.21f;
        ImageTargetBehaviour target;
        try
        {
            if (!pageTargets.TryGetValue(m.target, out target) || target == null)
            {
                target = await VuforiaBehaviour.Instance.ObserverFactory.CreateImageTargetAsync("pages/" + m.target + ".jpg", width, m.target);
                pageTargets[m.target] = target;
            }
        }
        catch (Exception e)
        {
            SayError("NO_PAGE_TARGET", "Could not make an Image Target from pages/" + m.target + ".jpg: " + e.Message);
            return;
        }
        if (request != pageRequest) return;
        foreach (var t in pageTargets.Values) if (t != null) t.enabled = t == target;

        bool hasScene = m.scene != null && ((m.scene.props?.Length ?? 0) + (m.scene.actors?.Length ?? 0)) > 0;
        if (hasScene)
        {
            var built = await StoryScene.Build(target.transform, m.scene, width, LoadModel);
            if (request != pageRequest) { Destroy(built.gameObject); return; }
            storyScene = built;
            model = built.gameObject;
            model.SetActive(false);
            Say(new OutMessage { type = "searching", mode = "page" });
            Watch(target);
            return;
        }
        storyScene = null;

        // The page's models stand in a row across the page, each about a third of the page wide.
        var scene = new GameObject(m.target + " scene");
        var names = m.models ?? new string[0];
        float size = width * 0.33f;
        for (int i = 0; i < names.Length; i++)
        {
            var piece = await LoadModel(names[i], size);
            if (request != pageRequest) { Destroy(scene); if (piece != null) Destroy(piece); return; }
            if (piece == null) continue;
            piece.transform.SetParent(scene.transform, false);
            piece.transform.localPosition = new Vector3((i - (names.Length - 1) / 2f) * size * 1.1f, 0f, 0f);
            piece.SetActive(true);
        }
        model = scene;
        Attach(target.transform);
        model.SetActive(false);
        Say(new OutMessage { type = "searching", mode = "page" });
        Watch(target);
    }

    private void OnVuforiaInitialized(VuforiaInitError error)
    {
        if (error == VuforiaInitError.NONE)
        {
            // Story pages are placed once Vuforia has started (OnVuforiaStarted).
            if (model != null && mode != "page") Place();
            return;
        }
        started = false;
        var name = error.ToString();
        SayError(name.Contains("LICENSE") ? "LICENSE" : "INIT", name);
    }

    /// <summary>Puts the 3D picture on the ground plane or on the printed card, depending on the mode.</summary>
    private void Place()
    {
        Unwatch();
        model.SetActive(false);
        placed = false;
        if (mode == "card")
        {
            if (planeFinder != null) planeFinder.gameObject.SetActive(false);
            var target = cardTargets != null ? cardTargets.Find(word) : null;
            if (target == null)
            {
                SayError("NO_CARD_TARGET", "There is no Image Target named " + word + " yet.");
                return;
            }
            Attach(target);
            Say(new OutMessage { type = "searching", mode = mode });
            Watch(target.GetComponent<ObserverBehaviour>());
        }
        else
        {
            if (planeFinder == null || groundStage == null)
            {
                SayError("NO_GROUND_PLANE", "The Ground Plane objects are not set on BasaKitaBridge.");
                return;
            }
            planeFinder.gameObject.SetActive(true);
            Attach(groundStage.transform);
            Say(new OutMessage { type = "searching", mode = mode });
            Watch(groundStage);
        }
    }

    // The first time the phone finds a flat surface, the picture is placed there on its own (no tap needed).
    private void OnAutomaticHitTest(HitTestResult result)
    {
        if (placed || mode != "ground" || model == null || result == null) return;
        var positioning = planeFinder.GetComponent<ContentPositioningBehaviour>();
        if (positioning == null) return;
        positioning.DuplicateStage = false;
        positioning.AnchorStage = groundStage;
        positioning.PositionContentAtPlaneAnchor(result);
        placed = true;
    }

    // Call after telling the app "searching": if the page or card is already in view (it can be
    // found while the 3D scene is still loading), Vuforia won't report a change, so apply it now.
    private void Watch(ObserverBehaviour observer)
    {
        watched = observer;
        if (watched == null) return;
        watched.OnTargetStatusChanged += OnTargetStatusChanged;
        var now = watched.TargetStatus;
        if (now.Status == Status.TRACKED || now.Status == Status.EXTENDED_TRACKED) OnTargetStatusChanged(watched, now);
    }

    private void Unwatch()
    {
        if (watched != null) watched.OnTargetStatusChanged -= OnTargetStatusChanged;
        watched = null;
    }

    private void OnTargetStatusChanged(ObserverBehaviour observer, TargetStatus status)
    {
        Debug.Log("[BASA-KITA] " + observer.TargetName + ": " + status.Status + " / " + status.StatusInfo);
        bool visible = status.Status == Status.TRACKED || status.Status == Status.EXTENDED_TRACKED;
        if (model != null) model.SetActive(visible);
        if (visible && storyScene != null) storyScene.OnShown();
        Say(new OutMessage { type = visible ? "tracking" : "lost", word = word });
    }

    // ---- 3D pictures: the same .glb files as the Kotlin app, in StreamingAssets/models ----

    private Task<GameObject> LoadModel(string w) => LoadModel(w, modelSize);

    private async Task<GameObject> LoadModel(string w, float size)
    {
        var url = Application.streamingAssetsPath + "/models/" + w + ".glb";
        if (!url.Contains("://")) url = "file://" + url; // Android's path is already a jar:file:// URL
        var gltf = new GltfImport();
        if (!await gltf.Load(url)) return null;
        var go = new GameObject(w);
        var holder = new GameObject("model").transform;
        holder.SetParent(go.transform, false);
        if (!await gltf.InstantiateMainSceneAsync(holder))
        {
            Destroy(go);
            return null;
        }
        FitToSize(go, holder, size);
        go.SetActive(false);
        return go;
    }

    private void Attach(Transform parent)
    {
        model.transform.SetParent(parent, false);
        model.transform.localPosition = Vector3.zero;
        model.transform.localRotation = Quaternion.identity;
    }

    // Scales the model so its biggest side is `size` (meters) and it stands on the surface.
    private void FitToSize(GameObject go, Transform holder, float size)
    {
        var renderers = go.GetComponentsInChildren<Renderer>();
        if (renderers.Length == 0) return;
        var bounds = renderers[0].bounds;
        foreach (var r in renderers) bounds.Encapsulate(r.bounds);
        float biggest = Mathf.Max(bounds.size.x, bounds.size.y, bounds.size.z);
        if (biggest <= 0f) return;
        float scale = size / biggest;
        holder.localScale = Vector3.one * scale;
        holder.localPosition = new Vector3(-bounds.center.x, -bounds.min.y, -bounds.center.z) * scale;
    }

    // ---- messages to the app ----

    private static void Say(OutMessage m) => NativeBridge.Send(JsonUtility.ToJson(m));

    private static void SayError(string code, string message) => Say(new OutMessage { type = "error", code = code, message = message });
}
