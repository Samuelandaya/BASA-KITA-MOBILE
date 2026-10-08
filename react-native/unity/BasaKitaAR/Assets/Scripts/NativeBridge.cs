using System.Runtime.InteropServices;
using UnityEngine;

/// <summary>Sends a message (a JSON string) from Unity to the React Native app.</summary>
public static class NativeBridge
{
#if UNITY_IOS && !UNITY_EDITOR
    [DllImport("__Internal")]
    private static extern void sendMessageToMobileApp(string message);
#endif

    public static void Send(string json)
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        using (var view = new AndroidJavaClass("com.azesmwayreactnativeunity.ReactNativeUnityViewManager"))
        {
            view.CallStatic("sendMessageToMobileApp", json);
        }
#elif UNITY_IOS && !UNITY_EDITOR
        sendMessageToMobileApp(json);
#else
        Debug.Log("[to app] " + json); // in the Unity Editor there is no app, so just log it
#endif
    }
}
