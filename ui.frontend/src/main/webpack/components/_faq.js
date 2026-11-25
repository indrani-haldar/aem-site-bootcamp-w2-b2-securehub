// FAQ accordion: buttons toggle their panel through aria-expanded and aria-controls.
// Arrow keys, Home and End move focus between questions. Enter and Space come from the native button.
(function() {
    "use strict";

    var selectors = {
        self:    '[data-cmp-is="faq"]',
        trigger: '[data-cmp-hook-faq="trigger"]'
    };

    function Faq(config) {

        function init(element) {
            // Prevents double initialisation when the mutation observer finds the component again.
            element.removeAttribute("data-cmp-is");

            var triggers = [].slice.call(element.querySelectorAll(selectors.trigger));

            function panelFor(trigger) {
                return document.getElementById(trigger.getAttribute("aria-controls"));
            }

            function setExpanded(trigger, expanded) {
                var panel = panelFor(trigger);
                trigger.setAttribute("aria-expanded", expanded ? "true" : "false");
                if (panel) {
                    panel.hidden = !expanded;
                }
            }

            function focusTrigger(index) {
                var last = triggers.length - 1;
                if (index < 0) {
                    index = last;
                } else if (index > last) {
                    index = 0;
                }
                triggers[index].focus();
            }

            function onKeydown(event, index) {
                switch (event.key) {
                    case "ArrowDown":
                        focusTrigger(index + 1);
                        break;
                    case "ArrowUp":
                        focusTrigger(index - 1);
                        break;
                    case "Home":
                        focusTrigger(0);
                        break;
                    case "End":
                        focusTrigger(triggers.length - 1);
                        break;
                    default:
                        return;
                }
                event.preventDefault();
            }

            triggers.forEach(function(trigger, index) {
                setExpanded(trigger, false);
                trigger.addEventListener("click", function() {
                    setExpanded(trigger, trigger.getAttribute("aria-expanded") !== "true");
                });
                trigger.addEventListener("keydown", function(event) {
                    onKeydown(event, index);
                });
            });
        }

        if (config && config.element) {
            init(config.element);
        }
    }

    function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        for (var i = 0; i < elements.length; i++) {
            new Faq({ element: elements[i] });
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                [].slice.call(mutation.addedNodes).forEach(function(addedNode) {
                    if (addedNode.querySelectorAll) {
                        [].slice.call(addedNode.querySelectorAll(selectors.self)).forEach(function(element) {
                            new Faq({ element: element });
                        });
                    }
                });
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

}());
