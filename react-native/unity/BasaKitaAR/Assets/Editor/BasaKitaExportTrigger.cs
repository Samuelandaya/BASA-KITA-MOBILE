using System.IO;
using UnityEditor;
using UnityEngine;

/// <summary>
/// Lets a script start the export while the Editor is open: create the file
/// Temp/basakita-export.request in the project (for example `touch Temp/basakita-export.request`)
/// and the Editor runs BASA-KITA → 2. Export for the app (Android), the same as clicking the menu.
/// The file is removed when the export starts.
/// </summary>
[InitializeOnLoad]
public static class BasaKitaExportTrigger
{
    const string RequestFile = "Temp/basakita-export.request";
    static double nextCheck;

    static BasaKitaExportTrigger()
    {
        EditorApplication.update += Check;
    }

    static void Check()
    {
        if (EditorApplication.timeSinceStartup < nextCheck) return;
        nextCheck = EditorApplication.timeSinceStartup + 2;
        if (!File.Exists(RequestFile) || EditorApplication.isCompiling || EditorApplication.isUpdating) return;
        File.Delete(RequestFile);
        Debug.Log("[BASA-KITA] Export requested by Temp/basakita-export.request");
        EditorApplication.delayCall += BasaKitaSetup.ExportAndroid;
    }
}
