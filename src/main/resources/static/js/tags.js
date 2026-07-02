(() => {
    const tagPattern = /#([\p{L}\p{N}_-]+)/gu;

    const uniqueTags = (text) => {
        const tags = new Set();
        for (const match of text.matchAll(tagPattern)) {
            tags.add(match[1]);
        }
        return [...tags];
    };

    const appendTag = (container, tagName) => {
        const tag = document.createElement("span");
        tag.className = "tag-chip";
        tag.textContent = `#${tagName}`;
        container.appendChild(tag);
    };

    document.querySelectorAll(".post").forEach((post) => {
        const body = post.querySelector(".post__body");
        const container = post.querySelector("[data-tag-container]");
        if (!body || !container) {
            return;
        }

        const tags = uniqueTags(body.textContent || "");
        container.hidden = tags.length === 0;
        tags.forEach((tagName) => appendTag(container, tagName));
    });
})();
