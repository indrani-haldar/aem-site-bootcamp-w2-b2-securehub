// Policy listing: category tabs filter the policy cards (data-filter on tabs, data-category on cards).
(function() {
    "use strict";

    var SELECTOR = '[data-cmp-is="policy-listing"]';

    function init(root) {
        var tabs = [].slice.call(root.querySelectorAll('[role="tab"][data-filter]'));
        var panel = root.querySelector('[role="tabpanel"]');
        var count = root.querySelector(".policy-listing__count");
        var noMatch = root.querySelector(".policy-listing__no-match");
        if (!tabs.length || !panel) {
            console.warn("Policy listing requires category tabs and a results panel.", root);
            return;
        }
        // Prevents double initialisation when the mutation observer finds the component again.
        root.removeAttribute("data-cmp-is");

        // Hide the list item so no empty grid cell is left behind.
        var cards = [].slice.call(root.querySelectorAll(".policy-listing__card")).map(function(card) {
            return { element: card.closest("li") || card, category: card.getAttribute("data-category") };
        });

        function labelOf(tab) {
            return tab.querySelector(".policy-listing__tab-label").textContent.trim();
        }

        function applyFilter(tab) {
            var filter = tab.getAttribute("data-filter");
            var label = labelOf(tab);
            var visible = 0;
            cards.forEach(function(card) {
                var matches = filter === "all" || card.category === filter;
                card.element.hidden = !matches;
                visible += matches ? 1 : 0;
            });
            if (count) {
                count.textContent = "Showing " + visible + " of " + cards.length + " policies" +
                    (filter === "all" ? " across all categories" : " in " + label);
            }
            if (noMatch) {
                noMatch.hidden = visible > 0 || cards.length === 0;
                noMatch.textContent = "No policies in " + label + ". Select All to view every policy.";
            }
        }

        function activate(tab, moveFocus) {
            tabs.forEach(function(other) {
                var selected = other === tab;
                other.setAttribute("aria-selected", selected ? "true" : "false");
                other.setAttribute("tabindex", selected ? "0" : "-1");
                other.classList.toggle("is-active", selected);
            });
            panel.setAttribute("aria-labelledby", tab.id);
            applyFilter(tab);
            if (moveFocus) {
                tab.focus();
            }
        }

        tabs.forEach(function(tab, index) {
            var filter = tab.getAttribute("data-filter");
            var total = cards.filter(function(card) {
                return filter === "all" || card.category === filter;
            }).length;
            var badge = tab.querySelector(".policy-listing__tab-count");
            if (badge) {
                badge.textContent = total;
                badge.hidden = false;
            }
            tab.setAttribute("aria-label", labelOf(tab) + ", " + total + (total === 1 ? " policy" : " policies"));
            tab.addEventListener("click", function() {
                activate(tab, false);
            });
            tab.addEventListener("keydown", function(event) {
                var target = null;
                if (event.key === "ArrowRight") {
                    target = tabs[(index + 1) % tabs.length];
                } else if (event.key === "ArrowLeft") {
                    target = tabs[(index - 1 + tabs.length) % tabs.length];
                } else if (event.key === "Home") {
                    target = tabs[0];
                } else if (event.key === "End") {
                    target = tabs[tabs.length - 1];
                }
                if (target) {
                    event.preventDefault();
                    activate(target, true);
                }
            });
        });

        var initial = tabs.filter(function(tab) {
            return tab.getAttribute("aria-selected") === "true";
        })[0] || tabs[0];
        activate(initial, false);
    }

    function onDocumentReady() {
        [].slice.call(document.querySelectorAll(SELECTOR)).forEach(init);

        // The page editor re-renders components after dialog edits; initialise the new markup too.
        new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                [].slice.call(mutation.addedNodes).forEach(function(node) {
                    if (node.nodeType === 1) {
                        if (node.matches(SELECTOR)) {
                            init(node);
                        }
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
