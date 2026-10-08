// Copies Unity's export settings (unity.androidNdkPath, unity.minSdkVersion, ...) from
// unity/builds/android/gradle.properties into the app's android/gradle.properties.
// unityLibrary/build.gradle reads them, and the app build doesn't load the export's own file.
// Also gives Gradle enough memory for the IL2CPP build.
const { readFileSync } = require('node:fs');
const { join } = require('node:path');
const { withGradleProperties } = require('expo/config-plugins');

const EXPORT_PROPERTIES = join(__dirname, '../unity/builds/android/gradle.properties');

function readUnityProperties() {
  return readFileSync(EXPORT_PROPERTIES, 'utf8')
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('unity') && line.includes('='))
    .map((line) => {
      const i = line.indexOf('=');
      return { key: line.slice(0, i), value: line.slice(i + 1) };
    });
}

function setProperty(props, key, value) {
  const existing = props.find((p) => p.type === 'property' && p.key === key);
  if (existing) existing.value = value;
  else props.push({ type: 'property', key, value });
}

module.exports = (config) =>
  withGradleProperties(config, (mod) => {
    for (const { key, value } of readUnityProperties()) setProperty(mod.modResults, key, value);
    setProperty(mod.modResults, 'org.gradle.jvmargs', '-Xmx4096m -XX:MaxMetaspaceSize=1024m');
    return mod;
  });
