(function () {
    const tagInput = document.getElementById("tag-input");
    const suggestions = document.getElementById("tag-suggestions");
    const selectedTags = document.getElementById("selected-tags");
    const hiddenInputs = document.getElementById("tag-hidden-inputs");

    if (!tagInput || !suggestions || !selectedTags || !hiddenInputs) {
        return;
    }

    const suggestionsEndpoint = suggestions.dataset.suggestionsUrl;
    const confirmEndpoint = suggestions.dataset.confirmUrl;
    const confirmedTags = new Set();
    const pendingTags = new Set();
    let selectedTagForDelete = null;

    function normalizeTagName(value) {
        let normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.slice(1).trim();
        }
        return normalized;
    }

    function hideSuggestions() {
        suggestions.innerHTML = "";
        suggestions.hidden = true;
    }

    function renderConfirmedTags() {
        selectedTags.innerHTML = "";
        hiddenInputs.innerHTML = "";

        confirmedTags.forEach((tagName) => {
            const entry = document.createElement("span");
            entry.className = "tag-selected-tags__entry";

            const item = document.createElement("button");
            item.type = "button";
            item.className = "tag-selected-tags__item";
            if (selectedTagForDelete === tagName) {
                item.classList.add("tag-selected-tags__item--selected");
            }
            item.textContent = "#" + tagName;
            item.addEventListener("click", (event) => {
                event.stopPropagation();
                selectedTagForDelete = tagName;
                renderConfirmedTags();
            });
            entry.appendChild(item);

            if (selectedTagForDelete === tagName) {
                const popover = document.createElement("span");
                popover.className = "tag-selected-tags__popover";

                const deleteButton = document.createElement("button");
                deleteButton.type = "button";
                deleteButton.className = "tag-selected-tags__delete";
                deleteButton.textContent = "削除";
                deleteButton.setAttribute("aria-label", "#" + tagName + " を削除");
                deleteButton.addEventListener("click", (event) => {
                    event.stopPropagation();
                    confirmedTags.delete(tagName);
                    selectedTagForDelete = null;
                    renderConfirmedTags();
                });
                popover.appendChild(deleteButton);
                entry.appendChild(popover);
            }

            selectedTags.appendChild(entry);

            const hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = "tagNames";
            hiddenInput.value = tagName;
            hiddenInputs.appendChild(hiddenInput);
        });
    }

    function csrfParams() {
        const csrfInput = document.querySelector("input[name='_csrf']");
        if (!csrfInput) {
            return {};
        }
        return {
            name: csrfInput.name,
            value: csrfInput.value
        };
    }

    async function confirmTag(value) {
        const tagName = normalizeTagName(value);
        if (!tagName || confirmedTags.has(tagName) || pendingTags.has(tagName)) {
            tagInput.value = "";
            hideSuggestions();
            return;
        }

        pendingTags.add(tagName);
        const params = new URLSearchParams();
        params.append("name", tagName);
        const csrf = csrfParams();
        if (csrf.name) {
            params.append(csrf.name, csrf.value);
        }

        let response;
        try {
            response = await fetch(confirmEndpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params
            });
        } finally {
            pendingTags.delete(tagName);
        }
        if (!response.ok) {
            return;
        }

        const confirmed = await response.json();
        confirmedTags.add(confirmed.name);
        selectedTagForDelete = null;
        tagInput.value = "";
        renderConfirmedTags();
        hideSuggestions();
    }

    function renderSuggestions(tagNames) {
        suggestions.innerHTML = "";
        if (tagNames.length === 0) {
            hideSuggestions();
            return;
        }

        tagNames.forEach((tagName) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "tag-suggestions__item";
            button.textContent = "#" + tagName;
            button.addEventListener("pointerdown", (event) => {
                event.preventDefault();
                confirmTag(tagName);
            });
            button.addEventListener("click", (event) => {
                event.preventDefault();
                confirmTag(tagName);
            });
            suggestions.appendChild(button);
        });
        suggestions.hidden = false;
    }

    async function updateSuggestions() {
        const query = normalizeTagName(tagInput.value);
        if (!query) {
            hideSuggestions();
            return;
        }

        const response = await fetch(suggestionsEndpoint + "?q=" + encodeURIComponent(query));
        if (!response.ok) {
            hideSuggestions();
            return;
        }
        renderSuggestions(await response.json());
    }

    hideSuggestions();
    tagInput.addEventListener("input", updateSuggestions);
    tagInput.addEventListener("keydown", (event) => {
        if (event.key !== "Enter" && event.key !== " ") {
            return;
        }
        event.preventDefault();
        confirmTag(tagInput.value);
    });
    tagInput.addEventListener("blur", () => {
        window.setTimeout(hideSuggestions, 150);
    });
    document.addEventListener("click", (event) => {
        if (!selectedTagForDelete || selectedTags.contains(event.target)) {
            return;
        }
        selectedTagForDelete = null;
        renderConfirmedTags();
    });
}());
