# Play Store assets

Everything the Console asks for. See `RELEASE.md` for the order to do it in.

| File | What it is |
|---|---|
| `RELEASE.md` | The full publishing procedure, including the data safety and content rating answers |
| `listing-en.md` | App name, short and full description, English |
| `listing-my.md` | The same, Burmese |
| `icon-512.png` | 512×512 app icon, PNG, no transparency |
| `feature-graphic-1024x500.png` | 1024×500 feature graphic |
| `privacy-policy.html` | The source of the published policy; Play requires one because the app shows ads. Published copy lives in `docs/` |
| `screenshots/` | Seven 1080-wide phone screenshots, taken from the running app |

The icon and feature graphic are **generated**, not hand-drawn — `scripts/store-graphics.py`
redraws them from the same vector artwork and palette the app uses, so they stay in step
with it. The screenshots are real captures and have to be retaken when the UI changes.
