using System;
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
    [Serializable] private class OutMessage { public string type; public string word; public string mode; public string code; public string message; }

    private string word;
    private string mode = "ground";
    private GameObject model;
    private ObserverBehaviour watched;
    private bool started;
    private bool placed;

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

    public void Stop(string _)
    {
        Unwatch();
        if (model != null) Destroy(model);
        model = null;
        word = null;
        if (planeFinder != null) planeFinder.gameObject.SetActive(false);
    }

    // ---- Vuforia ----

    private void OnVuforiaInitialized(VuforiaInitError error)
    {
        if (error == VuforiaInitError.NONE)
        {
            if (model != null) Place();
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
            Watch(groundStage);
        }
        Say(new OutMessage { type = "searching", mode = mode });
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

    private void Watch(ObserverBehaviour observer)
    {
        watched = observer;
        if (watched != null) watched.OnTargetStatusChanged += OnTargetStatusChanged;
    }

    private void Unwatch()
    {
        if (watched != null) watched.OnTargetStatusChanged -= OnTargetStatusChanged;
        watched = null;
    }

    private void OnTargetStatusChanged(ObserverBehaviour observer, TargetStatus status)
    {
        bool visible = status.Status == Status.TRACKED || status.Status == Status.EXTENDED_TRACKED;
        if (model != null) model.SetActive(visible);
        Say(new OutMessage { type = visible ? "tracking" : "lost", word = word });
    }

    // ---- 3D pictures: the same .glb files as the Kotlin app, in StreamingAssets/models ----

    private async Task<GameObject> LoadModel(string w)
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
        FitToSize(go, holder);
        go.SetActive(false);
        return go;
    }

    private void Attach(Transform parent)
    {
        model.transform.SetParent(parent, false);
        model.transform.localPosition = Vector3.zero;
        model.transform.localRotation = Quaternion.identity;
    }

    // Scales the model so its biggest side is modelSize and it stands on the surface.
    private void FitToSize(GameObject go, Transform holder)
    {
        var renderers = go.GetComponentsInChildren<Renderer>();
        if (renderers.Length == 0) return;
        var bounds = renderers[0].bounds;
        foreach (var r in renderers) bounds.Encapsulate(r.bounds);
        float biggest = Mathf.Max(bounds.size.x, bounds.size.y, bounds.size.z);
        if (biggest <= 0f) return;
        float scale = modelSize / biggest;
        holder.localScale = Vector3.one * scale;
        holder.localPosition = new Vector3(-bounds.center.x, -bounds.min.y, -bounds.center.z) * scale;
    }

    // ---- messages to the app ----

    private static void Say(OutMessage m) => NativeBridge.Send(JsonUtility.ToJson(m));

    private static void SayError(string code, string message) => Say(new OutMessage { type = "error", code = code, message = message });
}
