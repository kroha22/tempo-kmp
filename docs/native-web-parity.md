# Native update from the public web app

Source: `kroha22/tempo-react`, commit `de007e53fca4b94f1ac098c7468ccd1955dcffc2`.

Android and iOS continue to use shared Kotlin/Compose screens. No WebView, speech engine, audio controls, account or cloud service was added.

The update adds the Words area with 14 themes, selectable collections, Portuguese-first glass cards, side chevrons, pairs and the five school phrase exercises. Phrase state is retained while opening the two related lessons and present-tense forms. The related material and verb forms reuse the web catalog. The existing seven-step native learning route is preserved.

Cards include the original saved lesson cards and 1,000-verb deck, the 351-word collection and eight classroom instructions. IDs, articles, reviewed plural displays, examples and accepted option IDs are copied from the public content. Cards are 175 dp tall with 44 dp navigation targets; ratings use centered 13 sp labels. Both presentations share the same data and review reducer.

Ratings persist through the existing Android preferences and iOS UserDefaults hosts. The pure review calculation mirrors the web intervals and ease bounds. Basic verbs and the first 40 ranks receive longer successful intervals and lower priority when choosing the next review. Manual arrows browse without submitting a rating. Existing v1 progress records remain readable; review records are appended to the existing format.

The import is a content snapshot, not automatic synchronization. Run `node --experimental-strip-types scripts/export-web-content.mjs <tempo-react-checkout> <output.json>` with Node 22+, then `python3 scripts/import-web-content.py <output.json>` to emit an apply_patch patch. Content keys are `words`, `groups`, `general`, `school`, `phrases`, `lessons`, and `forms`; the general collection uses the reviewed display from `vocabularyLessons` where available. The generated source contains no device settings or repository credentials.

This transfer covers the requested cards, school phrases and related lessons. Existing web-only picture matching and word-search mechanics are not introduced by this update.

Validation without mobile launches: `:shared:jsNodeTest` runs the common reducer/content tests in Node; `:androidApp:assembleDebug` builds the Android APK; Xcode `build` for a generic iOS Simulator destination verifies the iOS host. These checks do not establish device UI parity or release-signing readiness.
