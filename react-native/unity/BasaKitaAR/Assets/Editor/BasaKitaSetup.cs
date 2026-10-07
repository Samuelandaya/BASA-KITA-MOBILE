using System.IO;
using System.Text.RegularExpressions;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.SceneManagement;
using UnityEngine;
using Vuforia;

/// <summary>
/// One-click setup for the AR scene and the Android export, so the steps in unity/README.md
/// don't have to be done by hand. Menu: BASA-KITA.
/// </summary>
public static class BasaKitaSetup
{
    const string ScenePath = "Assets/Scenes/AR.unity";
    // The React Native app looks for the export here (see react-native/app.config.ts).
    static string ExportPath => Path.GetFullPath(Path.Combine(Application.dataPath, "../../builds/android"));

    [MenuItem("BASA-KITA/1. Set up AR scene")]
    public static void SetUpScene()
    {
        // The app starts Vuforia itself with the license key (BasaKitaBridge.Configure).
        var config = VuforiaConfiguration.Instance;
        config.Vuforia.DelayedInitialization = true;
        EditorUtility.SetDirty(config);
        AssetDatabase.SaveAssetIfDirty(config);

        var scene = EditorSceneManager.NewScene(NewSceneSetup.DefaultGameObjects, NewSceneMode.Single);
        var mainCamera = GameObject.Find("Main Camera");
        if (mainCamera != null) Object.DestroyImmediate(mainCamera);

        Create("GameObject/Vuforia Engine/AR Camera");
        Create("GameObject/Vuforia Engine/Ground Plane/Plane Finder");
        Create("GameObject/Vuforia Engine/Ground Plane/Ground Plane Stage");
        var planeFinder = Object.FindFirstObjectByType<PlaneFinderBehaviour>().gameObject;
        var stage = Object.FindFirstObjectByType<AnchorBehaviour>().gameObject;
        // Vuforia's menu puts new objects under the selected one. Keep them at the top level:
        // the bridge turns the Plane Finder off until AR starts, which would also hide a stage inside it.
        planeFinder.transform.SetParent(null, true);
        stage.transform.SetParent(null, true);

        var positioning = planeFinder.GetComponent<ContentPositioningBehaviour>();
        positioning.AnchorStage = stage.GetComponent<AnchorBehaviour>();
        positioning.DuplicateStage = false;

        // Image Targets for "On the card" go under this, each named after its word (bola, aso, ...).
        var cardTargets = new GameObject("CardTargets");

        var bridgeObject = new GameObject("BasaKitaBridge"); // the app sends messages to this exact name
        var bridge = bridgeObject.AddComponent<BasaKitaBridge>();
        bridge.planeFinder = planeFinder.GetComponent<PlaneFinderBehaviour>();
        bridge.groundStage = stage.GetComponent<AnchorBehaviour>();
        bridge.cardTargets = cardTargets.transform;

        Directory.CreateDirectory(Path.GetDirectoryName(ScenePath));
        EditorSceneManager.SaveScene(scene, ScenePath);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(ScenePath, true) };

        SetAndroidSettings();
        AssetDatabase.SaveAssets();
        Debug.Log("[BASA-KITA] AR scene saved to " + ScenePath + " and Android settings applied.");
    }

    [MenuItem("BASA-KITA/2. Export for the app (Android)")]
    public static void ExportAndroid()
    {
        if (!File.Exists(ScenePath))
        {
            EditorUtility.DisplayDialog("BASA-KITA", "Run \"1. Set up AR scene\" first.", "OK");
            return;
        }
        SetAndroidSettings();
        EditorUserBuildSettings.exportAsGoogleAndroidProject = true;

        if (Directory.Exists(ExportPath)) Directory.Delete(ExportPath, true);
        var report = BuildPipeline.BuildPlayer(new BuildPlayerOptions
        {
            scenes = new[] { ScenePath },
            locationPathName = ExportPath,
            target = BuildTarget.Android,
            options = BuildOptions.None,
        });
        if (report.summary.result != UnityEditor.Build.Reporting.BuildResult.Succeeded)
        {
            Debug.LogError("[BASA-KITA] Export failed: " + report.summary.result);
            return;
        }
        RemoveLauncherIntent();
        Debug.Log("[BASA-KITA] Exported to " + ExportPath);
    }

    static void Create(string menuItem)
    {
        Selection.activeGameObject = null;
        if (!EditorApplication.ExecuteMenuItem(menuItem))
            throw new System.Exception("Vuforia menu item not found: " + menuItem);
    }

    static void SetAndroidSettings()
    {
        var android = NamedBuildTarget.Android;
        PlayerSettings.SetApplicationIdentifier(android, "ph.basakita.rn");
        PlayerSettings.SetScriptingBackend(android, ScriptingImplementation.IL2CPP);
        PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
        // Vuforia Engine 11.4 refuses to build below Android 10 (API 29); app.config.ts uses the same minimum.
        PlayerSettings.Android.minSdkVersion = AndroidSdkVersions.AndroidApiLevel29;
        PlayerSettings.defaultInterfaceOrientation = UIOrientation.Portrait;
    }

    // The app has its own launcher; Unity's would add a second app icon.
    static void RemoveLauncherIntent()
    {
        var manifest = Path.Combine(ExportPath, "unityLibrary/src/main/AndroidManifest.xml");
        if (!File.Exists(manifest)) return;
        var xml = File.ReadAllText(manifest);
        File.WriteAllText(manifest, Regex.Replace(xml, @"\s*<intent-filter>.*?</intent-filter>", "", RegexOptions.Singleline));
    }
}
