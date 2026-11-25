(function() {
    "use strict";

    var ROOT_ID = "security-policies";
    // Core Button markup varies by version, so match the anchor and skip any inside a card
    var PILL_SELECTOR = ".cmp-button";
    // Supports both the custom policy card and Core Component teasers (pretitle = category)
    var CARD_SELECTOR = ".cmp-policy-card, .cmp-teaser";
    var CATEGORY_SELECTOR = ".cmp-policy-card__category, .cmp-teaser__pretitle";
    var WRAPPER_SELECTOR = ".aem-GridColumn, .teaser, .policycard";
    var ACTIVE_CLASS = "is-active";
    var HIDDEN_CLASS = "is-filtered-out";
    var ALL_FILTER = "all";
    var INIT_ATTR = "data-security-policies-init";

    function normalize(text) {
        return (text || "").replace(/\s+/g, " ").trim().toLowerCase();
    }

    // An optional data-filter attribute overrides the pill label as the filter key
    function getFilter(pill) {
        return normalize(pill.getAttribute("data-filter") || pill.textContent);
    }

    // Hide the wrapper only when it holds this card alone, so siblings are never hidden with it
    function getTarget(card, root) {
        var wrapper = card.closest(WRAPPER_SELECTOR);
        var isOwnWrapper = wrapper && wrapper !== root && root.contains(wrapper)
            && wrapper.querySelectorAll(CARD_SELECTOR).length === 1;
        return isOwnWrapper ? wrapper : card;
    }

    function SecurityPolicies(root) {
        var pills = Array.prototype.slice.call(root.querySelectorAll(PILL_SELECTOR))
            .filter(function(pill) {
                return !pill.closest(CARD_SELECTOR);
            });
        var cards = Array.prototype.slice.call(root.querySelectorAll(CARD_SELECTOR))
            .filter(function(card) {
                return card.querySelector(CATEGORY_SELECTOR);
            })
            .map(function(card) {
                return {
                    category: normalize(card.querySelector(CATEGORY_SELECTOR).textContent),
                    target: getTarget(card, root)
                };
            });

        function selectPill(selected) {
            var filter = getFilter(selected);

            pills.forEach(function(pill) {
                var isActive = pill === selected;
                pill.classList.toggle(ACTIVE_CLASS, isActive);
                pill.setAttribute("aria-pressed", isActive ? "true" : "false");
            });

            cards.forEach(function(card) {
                var show = filter === ALL_FILTER || card.category === filter;
                card.target.classList.toggle(HIDDEN_CLASS, !show);
                card.target.hidden = !show;
            });
        }

        function onRootClick(event) {
            var target = event.target;
            var pill = target && target.closest ? target.closest(PILL_SELECTOR) : null;

            if (!pill || pills.indexOf(pill) === -1) {
                return;
            }

            event.preventDefault();
            selectPill(pill);
        }

        function init() {
            if (root.getAttribute(INIT_ATTR)) {
                return;
            }
            root.setAttribute(INIT_ATTR, "true");

            // Delegated listener: one handler on the root, so clicks on pill children still register
            root.addEventListener("click", onRootClick);

            var initial = pills.filter(function(pill) {
                return getFilter(pill) === ALL_FILTER;
            })[0] || pills[0];

            if (initial) {
                selectPill(initial);
            }
        }

        init();
    }

    function onDocumentReady() {
        var root = document.getElementById(ROOT_ID)
            || document.querySelector("[id^='" + ROOT_ID + "']")
            || document.querySelector(".cmp-container.security-policies");

        if (root) {
            new SecurityPolicies(root);
        }
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

}());
