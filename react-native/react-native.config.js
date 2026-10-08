// Leaves the Unity view out of the native build until the Unity export exists
// (unity/builds/android/unityLibrary), so the app still builds and runs without it.
const { existsSync } = require('node:fs');
const { join } = require('node:path');

const unityAvailable = existsSync(join(__dirname, 'unity/builds/android/unityLibrary'));

module.exports = {
  dependencies: unityAvailable ? {} : { '@azesmway/react-native-unity': { platforms: { android: null, ios: null } } },
};
