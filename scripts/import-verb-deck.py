#!/usr/bin/env python3
"""Convert Tempo's canonical TypeScript verb deck into Kotlin source."""

import json
import re
import sys
from pathlib import Path


def kotlin_string(value: str) -> str:
    return (
        value.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("$", "\\$")
        .replace("\n", "\\n")
    )


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: import-verb-deck.py SOURCE_TS OUTPUT_KT")

    source = Path(sys.argv[1]).read_text(encoding="utf-8")
    match = re.search(r"export const verbCards: VerbCard\[\] = (\[.*\]);\s*$", source, re.DOTALL)
    if not match:
        raise SystemExit("verbCards JSON array not found")

    cards = json.loads(match.group(1))
    ids = {card["id"] for card in cards}
    infinitives = {card["pt"] for card in cards}
    expected_ids = {f"v{rank:04d}" for rank in range(1, 1001)}
    if len(cards) != 1000 or ids != expected_ids or len(infinitives) != 1000:
        raise SystemExit("deck must contain v0001-v1000 and 1,000 unique infinitives")

    lines = [
        "// Generated from Tempo's canonical verb deck.",
        "// Frequency data: Corpus do Português. Translations: FreeDict rus-por (CC BY-SA 3.0).",
        "package io.github.kroha22.tempo.model",
        "",
        "data class VerbDeckCard(",
        "    val id: String,",
        "    val infinitive: String,",
        "    val translation: String,",
        "    val rank: Int,",
        "    val basic: Boolean,",
        ")",
        "",
        "val verbDeck: List<VerbDeckCard> = listOf(",
    ]
    for card in cards:
        lines.append(
            f'    VerbDeckCard("{card["id"]}", "{kotlin_string(card["pt"])}", '
            f'"{kotlin_string(card["ru"])}", {card["rank"]}, {str(card["basic"]).lower()}),'
        )
    lines.extend([")", ""])
    Path(sys.argv[2]).write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
