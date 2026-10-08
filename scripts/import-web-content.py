#!/usr/bin/env python3
"""Emit an apply_patch patch from the public web catalog JSON export.

The input contains words, groups, general, school, phrases, lessons and forms.
IDs and Portuguese text are preserved. No private settings or media are read.
"""
import json
import sys

data = json.load(open(sys.argv[1], encoding="utf-8"))
def s(value):
    return json.dumps(value, ensure_ascii=False).replace("$", "\\$")
def ls(values):
    return "listOf(" + ", ".join(values) + ")"
def strings(values):
    return ls([s(value) for value in values])
def word(w, pt="label", ru="translation", example=""):
    return f"NativeWord({s(w['id'])}, {s(w[pt])}, {s(w[ru])}, {s(example)})"
def example(e, pt="portuguese", ru="translation"):
    return f"Example({s(e[pt])}, {s(e[ru])})"
def activity(a):
    return f"NativeActivity({s(a['id'])}, {s(a['prompt'])}, {s(a.get('before',''))}, {s(a.get('after',''))}, " + ls([f"AnswerOption({s(o['id'])}, {s(o.get('text',o.get('label','')))})" for o in a['options']]) + f", {s(a['correctOptionId'])}, {s(a.get('feedback',''))})"
lines = ['package io.github.kroha22.tempo.model', '', '// Generated from kroha22/tempo-react de007e53fca4b94f1ac098c7468ccd1955dcffc2.',
 'data class NativeWord(val id: String, val portuguese: String, val russian: String, val example: String = "")',
 'data class NativeSet(val id: String, val title: String, val wordIds: List<String>)',
 'data class NativeTopic(val id: String, val title: String, val sets: List<NativeSet>)',
 'data class NativeActivity(val id: String, val prompt: String, val before: String, val after: String, val options: List<AnswerOption>, val correctOptionId: String, val feedback: String)',
 'data class NativeUsage(val id: String, val title: String, val situation: String, val collectionIds: List<String>, val examples: List<Example>, val exercises: List<NativeActivity>)',
 'data class NativeRelatedLesson(val id: String, val title: String, val cue: String, val examples: List<Example>, val vocabulary: List<Example>, val activity: NativeActivity)',
 'data class NativeVerbForms(val key: String, val title: String, val translation: String, val forms: List<String>)', '']
for name, values in [('nativeWords',[word(w) for w in data['words']]), ('nativeGeneralCards',[word(w,'portuguese') for w in data['general']]), ('nativeSchoolCards',[word(w,example=w['examplePt']) for w in data['school']])]:
    lines.append('val '+name+' = listOf(\n    '+',\n    '.join(values)+'\n)')
lines.append('val nativeTopics = listOf(\n    '+',\n    '.join(f"NativeTopic({s(g['id'])}, {s(g['title'])}, " + ls([f"NativeSet({s(t['id'])}, {s(t['title'])}, {strings(t['words'])})" for subgroup in g['subgroups'] for t in subgroup['sets']]) + ')' for g in data['groups'])+'\n)')
lines.append('val nativeUsages = listOf(\n    '+',\n    '.join(f"NativeUsage({s(u['id'])}, {s(u['title'])}, {s(u['situation'])}, {strings(u['collectionIds'])}, {ls([example(e) for e in u['examples']])}, {ls([activity(a) for a in u['exercises']])})" for u in data['phrases'])+'\n)')
related = [l for l in data['lessons'] if l['id'] in ['lesson:a1-1:locate-object','lesson:a1-1:possession-and-presence']]
lines.append('val nativeRelatedLessons = listOf(\n    '+',\n    '.join(f"NativeRelatedLesson({s(l['id'])}, {s(l['title'])}, {s(l['cue'])}, {ls([example(e,'pt','ru') for e in l['examples']])}, {ls([example(e,'pt','ru') for e in l.get('vocabulary',[])])}, {activity(l['activity'])})" for l in related)+'\n)')
lines.append('val nativeVerbForms = listOf(\n    '+',\n    '.join(f"NativeVerbForms({s(k)}, {s(v['infinitive'])}, {s(v['translation'])}, {strings([v['forms']['present'][p] for p in ['eu','tu','ele','nos','voces','eles']])})" for k,v in data['forms'].items())+'\n)')
print('*** Begin Patch\n*** Add File: shared/src/commonMain/kotlin/io/github/kroha22/tempo/model/ImportedWebContent.kt')
print('\n'.join('+'+line for line in '\n\n'.join(lines).splitlines()))
print('*** End Patch')
