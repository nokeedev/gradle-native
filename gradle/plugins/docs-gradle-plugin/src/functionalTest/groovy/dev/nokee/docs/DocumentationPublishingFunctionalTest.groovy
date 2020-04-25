package dev.nokee.docs

import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture

class DocumentationPublishingFunctionalTest extends AbstractDocumentationFunctionalSpec implements ArchiveTestFixture {
	def "can publish documentation to repository"() {
		given:
		makeSingleProject()
		writeDocumentationUnderTest()

		when:
		succeeds('publish')

		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToPublish)
		zip('repo/com/example/baked/4.2/baked-4.2-baked.zip').hasDescendants('docs/4.2/samples/foo-bar/all-commands.embed.html', 'docs/4.2/samples/foo-bar/all-commands.gif', 'docs/4.2/samples/foo-bar/all-commands.mp4', 'docs/4.2/samples/foo-bar/all-commands.png', 'docs/4.2/samples/foo-bar/FooBar-4.2-groovy-dsl.zip', 'docs/4.2/samples/foo-bar/FooBar-4.2-kotlin-dsl.zip', 'docs/4.2/samples/foo-bar/index.html', 'index.html', 'page.html', 'js/foo.js')
		zip('repo/com/example/jbake/4.2/jbake-4.2-templates.zip').hasDescendants('index.gsp', 'page.gsp')
		file('repo/com/example/jbake/4.2/jbake-4.2.properties').text == jbakePropertiesContent
	}

	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokeebuild.documentation'
				id 'maven-publish'
			}

			version = '4.2'
			group = 'com.example'

			${configureTemplate('foo')}

			documentation {
				samples {
					'foo-bar' {
						template = new TemplateFoo()
					}
				}
			}

			publishing {
				publications {
					jbake(MavenPublication) {
						from components.jbake
						artifactId = 'jbake'
					}
					baked(MavenPublication) {
						from components.baked
						artifactId = 'baked'
					}
				}
				repositories {
					maven { url = 'repo' }
				}
			}
		"""
	}

	protected void writeDocumentationUnderTest() {
		writeBasicSample()
		writeBasicJbakeProject()
	}
}
