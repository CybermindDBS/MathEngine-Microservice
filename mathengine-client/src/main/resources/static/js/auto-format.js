document.addEventListener('DOMContentLoaded', () => {
    const javaKeyword = /^(public|private|protected|class|static|void|int|String|boolean|float|double|if|else|while|for|return|new)\b/;
    const javaBlockTriggers = new Set(['{', '}', ';']);
    const exprTriggers = new Set(['+', '-', '*', '/', '^', '=', ',']); // Added comma
    const globalTriggers = new Set([...javaBlockTriggers, ...exprTriggers]);

    const isJavaLine = (line, depth) => {
        const trimmed = line.trim();
        return depth > 0 ||
            javaKeyword.test(trimmed) ||
            trimmed.endsWith('{') ||
            trimmed.startsWith('}') ||
            trimmed.startsWith('//') ||
            trimmed.startsWith('/*') ||
            trimmed.startsWith('*');
    };

    const getDepthAtLine = (lines, idx) => {
        let depth = 0;
        for (let i = 0; i < idx; i++) {
            const t = lines[i].trim();
            if (t.endsWith('{')) depth++;
            if (t.startsWith('}')) depth = Math.max(depth - 1, 0);
        }
        if (lines[idx].trim().startsWith('}')) depth = Math.max(depth - 1, 0);
        return depth;
    };

    document.querySelectorAll('.code-area').forEach(textarea => {
        // — Smart Backspace on expression lines —
        textarea.addEventListener('keydown', function (e) {
            if (e.key === 'Backspace' && this.selectionStart === this.selectionEnd) {
                const v = this.value;
                const pos = this.selectionStart;
                const before = v.slice(0, pos);
                const lines = v.split('\n');
                const lineIdx = before.split('\n').length - 1;
                const rawLine = lines[lineIdx];
                const depth = getDepthAtLine(lines, lineIdx);

                if (!isJavaLine(rawLine, depth)) {
                    const match = before.match(/ *([\+\-\*\/\^=]) *$|, $/); // comma only matches if there's space after it
                    if (match) {
                        e.preventDefault();
                        const opWithSpaces = match[0];
                        const newBefore = before.slice(0, -opWithSpaces.length);
                        const after = v.slice(pos);
                        this.value = newBefore + after;
                        const newPos = newBefore.length;
                        this.selectionStart = this.selectionEnd = newPos;
                    }
                }
            }

            // Smart indent on Enter
            if (e.key === 'Enter') {
                e.preventDefault();
                const v = this.value;
                const pos = this.selectionStart;
                const before = v.slice(0, pos);
                const after = v.slice(pos);

                const lastNL = before.lastIndexOf('\n');
                const currentLine = before.slice(lastNL + 1);
                const indentMatch = currentLine.match(/^\s*/);
                const indent = indentMatch ? indentMatch[0] : '';

                const insert = '\n' + indent;
                this.value = before + insert + after;
                const newPos = pos + insert.length;
                this.selectionStart = this.selectionEnd = newPos;
            }
        });

        // — Input formatting —
        textarea.addEventListener('input', function (e) {
            if (e.inputType && e.inputType.startsWith('delete')) return;

            const val = this.value;
            const pos = this.selectionStart;
            const lastChar = val[pos - 1];
            if (!globalTriggers.has(lastChar)) return;

            const before = val.slice(0, pos);
            const lines = val.split('\n');
            const lineIdx = before.split('\n').length - 1;
            const rawLine = lines[lineIdx];
            const depth = getDepthAtLine(lines, lineIdx);
            const inJava = isJavaLine(rawLine, depth);

            const lineStart = before.lastIndexOf('\n') + 1;
            const offsetInLine = pos - lineStart;
            const oldLineBeforeCursor = rawLine.slice(0, offsetInLine);

            let formattedLine, newLineBeforeCursor;
            if (inJava) {
                // Java-mode: indent + preserve comment lines
                if (/^\s*\/\*/.test(rawLine.trim()) || /^\s*\*/.test(rawLine.trim()) || /^\s*\/\//.test(rawLine.trim())) return;

                formattedLine = '    '.repeat(depth) + rawLine.trim();
                newLineBeforeCursor = '    '.repeat(depth) + oldLineBeforeCursor.trimStart();
            } else {
                // Expression-mode: space out operators
                formattedLine = rawLine
                    .replace(/ *([\+\-\*\/\^=]) */g, ' $1 ') // normal ops
                    .replace(/ *, */g, ', '); // commas: only space after, never before
                newLineBeforeCursor = oldLineBeforeCursor
                    .replace(/ *([\+\-\*\/\^=]) */g, ' $1 ')
                    .replace(/ *, */g, ', ');
            }

            lines[lineIdx] = formattedLine;
            const newVal = lines.join('\n');
            const newLineStart = lines.slice(0, lineIdx).reduce((sum, l) => sum + l.length + 1, 0);
            const newCursor = newLineStart + newLineBeforeCursor.length;

            this.value = newVal;
            this.selectionStart = this.selectionEnd = newCursor;
        });
    });
});
