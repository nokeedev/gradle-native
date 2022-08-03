assert leftNavigationContents
assert bodyContents

layout 'fragment-main-content.tpl', bodyContents: contents {
	leftNavigationContents()

	bodyContents()

	aside(class: 'secondary-navigation') {}
}
