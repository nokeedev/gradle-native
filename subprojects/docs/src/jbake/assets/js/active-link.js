// Polyfill Element.matches()
if (!Element.prototype.matches) {
	Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
}
// Polyfill Element.closest()
if (!Element.prototype.closest) {
	Element.prototype.closest = function(s) {
		var el = this;
		if (!document.documentElement.contains(el)) return null;
		do {
			if (typeof el.matches === "function" && el.matches(s)) return el;
			el = el.parentElement || el.parentNode;
		} while (el !== null);
		return null;
	};
}

// Add "active" class to TOC link corresponding to subsection at top of page
function setActiveSubsection(activeHref) {
	var tocLinkToActivate = document.querySelector(".toc a[href$='"+activeHref+"']");
	var currentActiveTOCLink = document.querySelector(".toc a.active");
	if (tocLinkToActivate != null) {
		if (currentActiveTOCLink != null && currentActiveTOCLink !== tocLinkToActivate) {
			currentActiveTOCLink.classList.remove("active");
		}
		tocLinkToActivate.classList.add("active");
	}
}

function calculateActiveSubsectionFromLink(event) {
	var closestLink = event.target.closest("a[href]");
	if (closestLink) {
		setActiveSubsection(closestLink.getAttribute("href"));
	}
}

function calculateActiveSubsectionFromScrollPosition() {
	var subsections = document.querySelectorAll("h2[id] > a.anchor,h2.title > a[name],h3.title > a[name]");

	// Assign active section: take advantage of fact that querySelectorAll returns elements in source order
	var activeSection = subsections[0];
	Array.prototype.forEach.call(subsections, function(section) {
		if (Math.floor(section.offsetTop) <= (window.scrollY + 50)) {
			activeSection = section;
		}
	});

	if (activeSection != null && activeSection.hasAttribute("href")) {
		setActiveSubsection(activeSection.getAttribute("href"));
	}
}

function postProcessUserguideNavigation() {
	function throttle(fn, periodMs) {
		var time = Date.now();
		var context = this;
		var args = Array.prototype.slice.call(arguments);
		return function() {
			if ((time + periodMs - Date.now()) < 0) {
				fn.apply(context, args);
				time = Date.now();
			}
		}
	}

	window.addEventListener("click", calculateActiveSubsectionFromLink);
	window.addEventListener("scroll", throttle(calculateActiveSubsectionFromScrollPosition, 50));
	calculateActiveSubsectionFromScrollPosition();
}


document.addEventListener("DOMContentLoaded", function(event) {
	postProcessUserguideNavigation();
});
