// Run with Node 22+: node --experimental-strip-types scripts/export-web-content.mjs <tempo-react-checkout> <output.json>
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { writeFileSync } from 'node:fs';
const root = process.argv[2];
if (!root || !process.argv[3]) throw new Error('Supply the public tempo-react checkout and output JSON path');
const read = (path) => import(pathToFileURL(resolve(root, path)).href);
const { catalogWords, catalogGroups } = await read('features/content/learning-catalog-view.ts');
const { generalVocabulary } = await read('features/content/general-vocabulary.ts');
const { vocabularyLessons } = await read('features/content/vocabulary-lessons.ts');
const { schoolInstructionCards } = await read('features/content/school-content.ts');
const { learningCatalog } = await read('features/content/learning-catalog.ts');
const { routeLessonExperiences } = await read('features/content/first-vertical-slice/route-extension.ts');
const { verbs } = await read('features/conjugation/data.ts');
const reviewed = new Map(vocabularyLessons.flatMap(lesson => lesson.items.map(item => [item.sourceEntryId, item.display])));
writeFileSync(process.argv[3], JSON.stringify({ words: catalogWords, groups: catalogGroups, general: generalVocabulary.map(word => ({ ...word, portuguese: reviewed.get(word.id) ?? word.portuguese })), school: schoolInstructionCards, phrases: learningCatalog.usageModules, lessons: routeLessonExperiences, forms: verbs }));
