# -*- coding: utf-8 -*-
"""Fix mojibake encoding and remove ry-navbar from page vue files."""
import re
from pathlib import Path

PAGES_DIR = Path(__file__).resolve().parents[1] / 'pages'

RY_NAVBAR_BLOCK = re.compile(
    r'\t?<ry-navbar[\s\S]*?</ry-navbar>\s*\n?',
    re.MULTILINE,
)
RY_NAVBAR_SELF = re.compile(
    r'\t?<ry-navbar[^>]*?/>\s*\n?',
    re.MULTILINE,
)


def fix_mojibake(text: str) -> str:
    try:
        return text.encode('latin-1').decode('utf-8')
    except (UnicodeDecodeError, UnicodeEncodeError):
        return text


def strip_navbar(text: str) -> str:
    text = RY_NAVBAR_BLOCK.sub('', text)
    text = RY_NAVBAR_SELF.sub('', text)
    return text


def main():
    count = 0
    for path in PAGES_DIR.rglob('*.vue'):
        raw = path.read_text(encoding='utf-8')
        fixed = fix_mojibake(raw)
        fixed = strip_navbar(fixed)
        if fixed != raw:
            path.write_text(fixed, encoding='utf-8', newline='\n')
            count += 1
            print(f'updated: {path.relative_to(PAGES_DIR.parent)}')
    print(f'done, {count} files updated')


if __name__ == '__main__':
    main()
