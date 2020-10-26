package dev.nokee.docs

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

trait DocumentationPluginFixture {
	abstract TestFile file(Object... path)

	String getPluginId() {
		return 'dev.nokeebuild.documentation'
	}

	String getJbakePropertiesContent() {
		return '''
render.tags=false
render.sitemap=false
render.index=true
render.archive=false
render.post=false
render.page=true
render.feed=false
'''
	}

	void writeBasicJbakeProject() {
		file('src/jbake/jbake.properties') << jbakePropertiesContent
		file('src/jbake/templates/index.gsp') << '''
<!DOCTYPE html>
<html lang="en">
  <head>
  	<title>Foo</title>
  </head>
  <body>
	<h1>Foo</h1>
  </body>
</html>
'''
		file('src/jbake/templates/page.gsp') << '''
<!DOCTYPE html>
<html lang="en">
  <head>
  	<title>${content.title}</title>
  </head>
  <body>
	<h1>${content.title}</h1>
	${content.body}
  </body>
</html>
'''
		file('src/jbake/assets/js/foo.js') << '''
console.log('Foo');
'''
		file('src/jbake/content/page.adoc') << '''= Some Page
:jbake-status: published
:jbake-type: page
Some content
'''
	}

	void writeBasicSample() {
		file('src/docs/samples/foo-bar/README.adoc') << """= Foo Bar
			|:jbake-type: page
			|
			|[listing.terminal]
			|----
			|\$ ls
			|
			|BUILD SUCCESSFUL
			|4 actionable tasks: 4 executed
			|----
			|
			|""".stripMargin()
		file('src/docs/samples/foo-bar/groovy-dsl/build.gradle') << """
			println 'Hello, World!'
		"""
		file('src/docs/samples/foo-bar/groovy-dsl/settings.gradle') << """
			rootProject.name = 'foo-bar'
		"""
		file('src/docs/samples/foo-bar/kotlin-dsl/build.gradle.kts') << """
			println("Hello, World!")
		"""
		file('src/docs/samples/foo-bar/kotlin-dsl/settings.gradle.kts') << """
			rootProject.name = "foo-bar"
		"""
	}

	/*
class TemplateFoo extends SourceElement {
@Override
List<SourceFile> getFiles() {
	return [sourceFile('java/com/example', 'Foo.java', '''package com.example;
public class Foo {}
''')]
}
}
*/
	String configureTemplate(String name) {
		return """
			import ${SourceElement.canonicalName}
			import ${SourceFile.canonicalName}

			class Template${name.capitalize()} extends SourceElement {
				@Override
				List<SourceFile> getFiles() {
					return [sourceFile('java/com/example', '${name.capitalize()}.java', '''package com.example;
		public class ${name.capitalize()} {}
		''')]
				}
			}
		"""
	}
}
