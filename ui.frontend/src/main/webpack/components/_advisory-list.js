// Advisory list: search, severity filter and sort run together on the rendered cards, without reloading the page.
(function() {
    "use strict";

    var SELECTOR = '[data-cmp-is="advisory-list"]';

    function init(root) {
        // Prevents double initialisation when the mutation observer finds the component again.
        root.removeAttribute("data-cmp-is");

        var list = root.querySelector("[data-advisory-items]");
        if (!list) {
            return;
        }
        var search = root.querySelector("[data-advisory-search]");
        var sort = root.querySelector("[data-advisory-sort]");
        var count = root.querySelector("[data-advisory-count]");
        var empty = root.querySelector("[data-advisory-empty]");
        var filters = [].slice.call(root.querySelectorAll("[data-advisory-filter]"));
        var state = { query: "", severity: "all", order: "newest" };

        function textOf(card, selector) {
            var element = card.querySelector(selector);
            return element ? element.textContent.toLowerCase() : "";
        }

        var items = [].slice.call(list.querySelectorAll(".advisory-card")).map(function(card) {
            return {
                card: card,
                text: textOf(card, "[data-advisory-title]") + " " + textOf(card, "[data-advisory-summary]"),
                severity: card.getAttribute("data-severity"),
                date: Number(card.getAttribute("data-date")) || 0
            };
        });

        function update() {
            var visible = 0;
            items.slice()
                .sort(function(a, b) {
                    return state.order === "oldest" ? a.date - b.date : b.date - a.date;
                })
                .forEach(function(item) {
                    var matches = (state.severity === "all" || item.severity === state.severity) &&
                        (!state.query || item.text.indexOf(state.query) !== -1);
                    item.card.hidden = !matches;
                    visible += matches ? 1 : 0;
                    list.appendChild(item.card);
                });
            if (count) {
                count.textContent = "Showing " + visible + " of " + items.length + " advisories";
            }
            if (empty) {
                empty.hidden = visible > 0;
            }
        }

        if (search) {
            search.addEventListener("input", function() {
                state.query = search.value.trim().toLowerCase();
                update();
            });
        }
        if (sort) {
            sort.addEventListener("change", function() {
                state.order = sort.value;
                update();
            });
        }
        filters.forEach(function(button) {
            button.addEventListener("click", function() {
                state.severity = button.getAttribute("data-advisory-filter");
                filters.forEach(function(other) {
                    var active = other === button;
                    other.classList.toggle("is-active", active);
                    other.setAttribute("aria-pressed", active ? "true" : "false");
                });
                update();
            });
        });

        update();
    }

    function onDocumentReady() {
        [].slice.call(document.querySelectorAll(SELECTOR)).forEach(init);

        // The page editor re-renders components after dialog edits; initialise the new markup too.
        new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                [].slice.call(mutation.addedNodes).forEach(function(node) {
                    if (node.querySelectorAll) {
                        [].slice.call(node.querySelectorAll(SELECTOR)).forEach(init);
                    }
                });
            });
        }).observe(document.body, { subtree: true, childList: true });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }
}());
