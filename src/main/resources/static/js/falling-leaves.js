(() => {
    const MAX_DECORATIONS = 14;
    const SPAWN_INTERVAL_MS = 1800;
    const FLOWER_CHANCE = 0.08;
    const INITIAL_DECORATIONS = 6;
    const LEAF_COLORS = ["#86c85a", "#4f9d45", "#2f7d46"];
    const motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");

    if (motionQuery.matches) {
        return;
    }

    const layer = document.createElement("div");
    layer.className = "falling-decoration";
    layer.setAttribute("aria-hidden", "true");
    document.body.append(layer);

    const randomBetween = (min, max) => min + Math.random() * (max - min);
    const cssValue = (value, unit) => `${value.toFixed(2)}${unit}`;
    const randomItem = (items) => items[Math.floor(Math.random() * items.length)];

    const spawnDecoration = () => {
        if (document.hidden || motionQuery.matches || layer.childElementCount >= MAX_DECORATIONS) {
            return;
        }

        const item = document.createElement("span");
        const isFlower = Math.random() < FLOWER_CHANCE;
        item.className = isFlower
            ? "falling-decoration__item falling-decoration__flower"
            : "falling-decoration__item falling-decoration__leaf";

        if (!isFlower) {
            item.style.setProperty("--leaf-size", cssValue(randomBetween(1.15, 2.1), "rem"));
            item.style.setProperty("--leaf-color", randomItem(LEAF_COLORS));
        }

        item.style.setProperty("--fall-start-x", cssValue(randomBetween(58, 98), "vw"));
        item.style.setProperty("--fall-start-y", cssValue(randomBetween(-18, -4), "vh"));
        item.style.setProperty("--fall-end-x", cssValue(randomBetween(-18, 24), "vw"));
        item.style.setProperty("--fall-end-y", cssValue(randomBetween(98, 114), "vh"));
        item.style.setProperty("--fall-duration", cssValue(randomBetween(18, 30), "s"));
        item.style.setProperty("--fall-rotate-start", cssValue(randomBetween(-40, 80), "deg"));
        item.style.setProperty("--fall-rotate-end", cssValue(randomBetween(220, 540), "deg"));
        item.addEventListener("animationend", () => item.remove(), { once: true });

        layer.append(item);
    };

    for (let index = 0; index < INITIAL_DECORATIONS; index++) {
        window.setTimeout(spawnDecoration, index * 320);
    }

    window.setInterval(spawnDecoration, SPAWN_INTERVAL_MS);
    document.addEventListener("visibilitychange", spawnDecoration);
})();
