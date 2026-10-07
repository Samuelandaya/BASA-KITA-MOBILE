// COPIED from the website (src/lock.js) by tools/sync-web.mjs. Edit it there, then run the script again.
// Remembers which account has entered the parent PIN since the page opened.
// Logging out locks Settings again; so does reloading the page.
let unlockedFor = null;
export const isUnlocked = (email) => Boolean(email) && unlockedFor === email;
export const unlock = (email) => { unlockedFor = email; };
export const relock = () => { unlockedFor = null; };
