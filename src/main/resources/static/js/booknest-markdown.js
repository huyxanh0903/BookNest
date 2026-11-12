(() => {
    const isSafeLink = (value) => /^(https?:|mailto:)/i.test(value.trim());

    const renderInline = (text, parent) => {
        const token = /(`[^`]+`|\*\*[^*]+\*\*|__[^_]+__|\*[^*\n]+\*|_[^_\n]+_|\[[^\]]+\]\([^)]+\))/g;
        let cursor = 0;
        for (const match of text.matchAll(token)) {
            parent.append(document.createTextNode(text.slice(cursor, match.index)));
            const value = match[0];
            let node;
            if (value.startsWith('`')) {
                node = document.createElement('code');
                node.textContent = value.slice(1, -1);
            } else if (value.startsWith('**') || value.startsWith('__')) {
                node = document.createElement('strong');
                node.textContent = value.slice(2, -2);
            } else if (value.startsWith('[')) {
                const parts = value.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
                if (parts && isSafeLink(parts[2])) {
                    node = document.createElement('a');
                    node.textContent = parts[1];
                    node.href = parts[2];
                    node.target = '_blank';
                    node.rel = 'noopener noreferrer';
                } else {
                    parent.append(document.createTextNode(value));
                    cursor = match.index + value.length;
                    continue;
                }
            } else {
                node = document.createElement('em');
                node.textContent = value.slice(1, -1);
            }
            parent.append(node);
            cursor = match.index + value.length;
        }
        parent.append(document.createTextNode(text.slice(cursor)));
    };

    const cells = (line) => line.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map(cell => cell.trim());
    const isTableSeparator = (line) => /^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(line);

    const render = (source, root) => {
        const lines = source.replace(/\r\n?/g, '\n').split('\n');
        let i = 0;
        while (i < lines.length) {
            const line = lines[i];
            if (!line.trim()) { i++; continue; }
            if (/^```/.test(line.trim())) {
                const code = [];
                i++;
                while (i < lines.length && !/^```/.test(lines[i].trim())) code.push(lines[i++]);
                if (i < lines.length) i++;
                const pre = document.createElement('pre');
                const block = document.createElement('code');
                block.textContent = code.join('\n');
                pre.append(block);
                root.append(pre);
                continue;
            }
            const heading = line.match(/^\s{0,3}(#{1,6})\s+(.+?)\s*#*\s*$/);
            if (heading) {
                const element = document.createElement(`h${heading[1].length}`);
                renderInline(heading[2], element);
                root.append(element);
                i++;
                continue;
            }
            if (i + 1 < lines.length && line.includes('|') && isTableSeparator(lines[i + 1])) {
                const headers = cells(line);
                const alignments = cells(lines[i + 1]).map(value => value.startsWith(':') && value.endsWith(':') ? 'center' : value.endsWith(':') ? 'right' : 'left');
                const table = document.createElement('table');
                const headerRow = table.createTHead().insertRow();
                headers.forEach((text, index) => {
                    const cell = document.createElement('th');
                    renderInline(text, cell);
                    cell.style.textAlign = alignments[index] || 'left';
                    headerRow.append(cell);
                });
                const body = table.createTBody();
                i += 2;
                while (i < lines.length && lines[i].includes('|') && lines[i].trim()) {
                    const row = body.insertRow();
                    cells(lines[i++]).forEach((text, index) => {
                        const cell = row.insertCell();
                        cell.style.textAlign = alignments[index] || 'left';
                        if (index < headers.length) renderInline(text, cell);
                    });
                }
                const wrapper = document.createElement('div');
                wrapper.className = 'ai-table-wrap';
                wrapper.setAttribute('role', 'region');
                wrapper.setAttribute('aria-label', 'Scrollable response table');
                wrapper.tabIndex = 0;
                wrapper.append(table);
                root.append(wrapper);
                continue;
            }
            if (/^\s{0,3}>\s?/.test(line)) {
                const quote = document.createElement('blockquote');
                while (i < lines.length && /^\s{0,3}>\s?/.test(lines[i])) {
                    if (quote.childNodes.length) quote.append(document.createElement('br'));
                    renderInline(lines[i++].replace(/^\s{0,3}>\s?/, ''), quote);
                }
                root.append(quote);
                continue;
            }
            const listMatch = line.match(/^\s{0,3}([-*+]\s+|\d+[.)]\s+)/);
            if (listMatch) {
                const ordered = /^\d/.test(listMatch[1]);
                const list = document.createElement(ordered ? 'ol' : 'ul');
                while (i < lines.length) {
                    const item = lines[i].match(/^\s{0,3}([-*+]\s+|\d+[.)]\s+)(.+)$/);
                    if (!item || /^\d/.test(item[1]) !== ordered) break;
                    const entry = document.createElement('li');
                    renderInline(item[2], entry);
                    list.append(entry);
                    i++;
                }
                root.append(list);
                continue;
            }
            const paragraph = document.createElement('p');
            while (i < lines.length && lines[i].trim() && !/^\s{0,3}(#{1,6}\s|```|>\s?|[-*+]\s+|\d+[.)]\s+)/.test(lines[i]) && !(i + 1 < lines.length && lines[i].includes('|') && isTableSeparator(lines[i + 1]))) {
                if (paragraph.childNodes.length) paragraph.append(document.createElement('br'));
                renderInline(lines[i++], paragraph);
            }
            root.append(paragraph);
        }
    };

    window.BookNestMarkdown = { render };
    document.querySelectorAll('.ai-message-content[data-markdown="true"]').forEach(node => {
        const source = node.textContent;
        node.replaceChildren();
        render(source, node);
    });
})();
