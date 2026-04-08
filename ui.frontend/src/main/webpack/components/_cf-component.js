// Example of how cf-component should be initialized via JavaScript
// This script fetches content fragment data from the servlet and renders cards

(function () {
    "use strict";

    var SERVLET_URL = "/bin/wknd/cf-data.json";
    var LEARN_MORE_URL = "https://www.google.com";

    // Best practice:
    // For a good separation of concerns, don't rely on CSS-only selectors,
    // and use dedicated data attributes to identify component elements.
    var selectors = {
        self: '[data-cmp-is="cfcomponent"]',
        list: '[data-cmp-hook-cf="list"]',
        loader: '[data-cmp-hook-cf="loader"]',
        error: '[data-cmp-hook-cf="error"]'
    };

    var NO_IMG = "data:image/svg+xml," + encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="220">' +
        '<rect width="400" height="220" fill="#e0e0e0"/>' +
        '<text x="200" y="115" font-size="15" fill="#9e9e9e" ' +
        'text-anchor="middle" dominant-baseline="middle" font-family="Arial">' +
        "No Image</text></svg>"
    );

    function setVisible(element, isVisible) {
        if (element) {
            element.style.display = isVisible ? "block" : "none";
        }
    }

    //renderCards is responsible for building HTML strings and inserting them into the DOM
    function renderCards(listElement, cards) {
        if (!listElement) {
            return;
        }

        if (!Array.isArray(cards) || cards.length === 0) {
            listElement.innerHTML = "";
            return;
        }

        let cardHtml = cards.map(card => `
<li class="cardFeature">
    <div class="cf-card">
        <div class="cf-card__img-wrap">
            <img class="cf-card__img" src="${card.imageUrl || NO_IMG}" alt="" />
        </div>
        <div class="cf-card__body">
            <h2 class="cf-card__title">${card.title || ""}</h2>
            <p class="cf-card__desc">${card.description || ""}</p>
            <a class="cf-card__btn" href="${LEARN_MORE_URL}">Learn More</a>
        </div>
    </div>
</li>
`).join("");

        listElement.innerHTML = cardHtml;
    }

    function loadCards(config) {
        setVisible(config.loader, true);
        setVisible(config.error, false);

        fetch(SERVLET_URL, {
            method: "GET"
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Network response was not ok");
                }
                return response.json(); // converts response → JSON
            })
            .then(function (data) {
              //  console.log("datatatattatata", data.data1);
                renderCards(config.list, data.data1); //accesses data.data1
            })
            .catch(function (error) {
                if (console && console.error) {
                    console.error("CF component data load failed", error);
                }
                if (config.error) {
                    config.error.textContent = "Failed to load data";
                    setVisible(config.error, true);
                }
            })
            .finally(function () {
                setVisible(config.loader, false);
            });
    }

    function ContentFragment(config) {

        function init(config) {
            // Best practice:
            // To prevent multiple initialization, remove the main data attribute that
            // identified the component.
            config.element.removeAttribute("data-cmp-is");

            var list = config.element.querySelectorAll(selectors.list);
            list = list.length == 1 ? list[0] : null;

            var loader = config.element.querySelectorAll(selectors.loader);
            loader = loader.length == 1 ? loader[0] : null;

            var error = config.element.querySelectorAll(selectors.error);
            error = error.length == 1 ? error[0] : null;

            loadCards({
                list: list,
                loader: loader,
                error: error
            });
        }

        if (config && config.element) {
            init(config);
        }
    }

    // Best practice:
    // Use a method like this mutation observer to properly initialize the component
    // when an author drops it onto the page or modifies it with the dialog.
    function onDocumentReady() {
        var elements = document.querySelectorAll(selectors.self);
        //this handles initial rendering on page load
        for (var i = 0; i < elements.length; i++) {
            new ContentFragment({ element: elements[i] }); //Create new instance of CF & Passes the DOM element to it 
        }

        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                // needed for IE
                var nodesArray = [].slice.call(mutation.addedNodes);
                if (nodesArray.length > 0) {
                    nodesArray.forEach(function (addedNode) {
                        if (addedNode.querySelectorAll) {
                            var elementsArray = [].slice.call(addedNode.querySelectorAll(selectors.self));
                            elementsArray.forEach(function (element) {
                                new ContentFragment({ element: element });
                            });
                        }
                    });
                }
            });
        });

        observer.observe(body, {
            subtree: true,
            childList: true,
            characterData: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }

}());