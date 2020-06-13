package dev.nokee.docs

import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture
import dev.gradleplugins.test.fixtures.file.TestFile

class SampleFunctionalTest extends AbstractDocumentationFunctionalSpec implements ArchiveTestFixture {
	def "can generate sample"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		and:
		def indexDotAdoc = file('build/tmp/stageFooBarSample/index.adoc')
		indexDotAdoc.assertExists()
		indexDotAdoc.text == ''':jbake-version: 4.2
:toc:
:toclevels: 1
:toc-title: Contents
:icons: font
:idprefix:
:jbake-status: published
:encoding: utf-8
:lang: en-US
:sectanchors: true
:sectlinks: true
:linkattrs: true
:gradle-user-manual: https://docs.gradle.org/6.2.1/userguide
:gradle-language-reference: https://docs.gradle.org/6.2.1/dsl
:gradle-api-reference: https://docs.gradle.org/6.2.1/javadoc
:gradle-guides: https://guides.gradle.org/
:jbake-permalink: foo-bar
:jbake-archivebasename: FooBar
:includedir: .
= Foo Bar
:jbake-type: page

[listing.terminal]
----
$ ls

BUILD SUCCESSFUL
4 actionable tasks: 4 executed
----

'''
	}

	def "can stage for bake with fully assembled samples"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('stageBake')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToStageBake)
		file('build/staging').assertHasDescendants('assets/js/foo.js',
			'content/docs/4.2/samples/foo-bar/index.adoc', 'content/docs/4.2/samples/foo-bar/FooBar-4.2-groovy-dsl.zip', 'content/docs/4.2/samples/foo-bar/FooBar-4.2-kotlin-dsl.zip',
			'content/docs/4.2/samples/foo-bar/groovy-dsl/.jbakeignore', 'content/docs/4.2/samples/foo-bar/groovy-dsl/build.gradle', 'content/docs/4.2/samples/foo-bar/groovy-dsl/settings.gradle', 'content/docs/4.2/samples/foo-bar/groovy-dsl/gradlew', 'content/docs/4.2/samples/foo-bar/groovy-dsl/gradlew.bat', 'content/docs/4.2/samples/foo-bar/groovy-dsl/gradle/wrapper/gradle-wrapper.jar', 'content/docs/4.2/samples/foo-bar/groovy-dsl/gradle/wrapper/gradle-wrapper.properties', 'content/docs/4.2/samples/foo-bar/groovy-dsl/src/main/java/com/example/Foo.java',
			'content/docs/4.2/samples/foo-bar/kotlin-dsl/.jbakeignore', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/build.gradle.kts', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/settings.gradle.kts', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/gradlew', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/gradlew.bat', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/gradle/wrapper/gradle-wrapper.jar', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/gradle/wrapper/gradle-wrapper.properties', 'content/docs/4.2/samples/foo-bar/kotlin-dsl/src/main/java/com/example/Foo.java',
			'content/page.adoc', 'jbake.properties', 'templates/index.gsp', 'templates/page.gsp')
	}

	def "can restore from cache assembling the samples"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		executer = executer.withBuildCacheEnabled().requireOwnGradleUserHomeDirectory()
		executer = executer.withoutDeprecationChecks() // because of :bake task

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		assert file('build').deleteDir()
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleSamples)
	}

	def "can restore from cache assembling the sample documentation"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		executer = executer.withBuildCacheEnabled().requireOwnGradleUserHomeDirectory()
		executer = executer.withoutDeprecationChecks() // because of :bake task

		when:
		succeeds('assembleDocumentation')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleDocumentation)

		when:
		assert file('build').deleteDir()
		succeeds('assembleDocumentation', '-i')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleDocumentation)
	}

	def "detects changes when assembling the sample documentation"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		executer = executer.withoutDeprecationChecks() // because of :bake task

		when:
		succeeds('assembleDocumentation')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleDocumentation)

		when:
		succeeds('assembleDocumentation', '-i')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleDocumentation)

		when:
		contentFile << '''
Some more content
'''
		succeeds('assembleDocumentation', '-i')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleDocumentation)
		result.assertTasksNotSkipped(':processFooBarSampleAsciidoctor', ':stageFooBarSample', ':stageSamples', ':stageBake', ':bake',
			':generateFooBarSampleAsciinema', ':compileFooBarSampleAsciicast',
			':stageDocumentation', ':assembleDocumentation')
	}

	def "detects change to asciidoctor content"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		succeeds('assembleSamples', '-i')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		contentFile << '''
Some more content
'''
		succeeds('assembleSamples')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
		result.assertTasksNotSkipped(':processFooBarSampleAsciidoctor', ':stageFooBarSample', ':stageSamples',
			':assembleSamples')
	}

	def "detects change to Groovy DSL content"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		file('src/docs/samples/foo-bar/groovy-dsl/some-new-file') << '''
			Content
		'''
		succeeds('assembleSamples')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
		result.assertTasksNotSkipped(':zipFooBarSampleGroovyDsl', ':stageFooBarSample', ':stageSamples', ':assembleSamples')
	}

	def "detects change to Kotlin DSL content"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		file('src/docs/samples/foo-bar/kotlin-dsl/some-new-file') << '''
			Content
		'''
		succeeds('assembleSamples')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
		result.assertTasksNotSkipped(':zipFooBarSampleKotlinDsl', ':stageFooBarSample', ':stageSamples', ':assembleSamples')
	}

	def "detects changes to project version"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		buildFile << '''
			version = '4.3'
		'''
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(':generateFooBarSampleGradleWrapper', ':generateFooBarSampleContent')
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
	}

	def "can define null sample template"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		buildFile << '''
			documentation.samples.'foo-bar'.template = null
		'''

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
		result.assertTasksSkipped(':generateFooBarSampleContent')
	}

	def "detects changes to sample template"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(tasks.withSample('foo-bar').allToAssembleSamples)

		when:
		buildFile << configureTemplateBar() << '''
			documentation.samples.'foo-bar'.template = new TemplateBar()
		'''
		succeeds('assembleSamples')
		then:
		result.assertTasksSkipped(':generateFooBarSampleGradleWrapper',
			':configureGroovyDslSettingsConfiguration', ':processFooBarGroovyDslSettingsFile',
			':configureKotlinDslSettingsConfiguration', ':processFooBarKotlinDslSettingsFile',
			':processFooBarSampleAsciidoctor')
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToAssembleSamples)
	}

	def "can assemble sample archives"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()

		when:
		succeeds(tasks.withSample('foo-bar').zipKotlinDsl)
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToZipKotlinDsl)
		zip("build/tmp/zipFooBarSampleKotlinDsl/FooBar-4.2-kotlin-dsl.zip").hasDescendants('build.gradle.kts', 'settings.gradle.kts', 'gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties', 'src/main/java/com/example/Foo.java')

		when:
		succeeds(tasks.withSample('foo-bar').zipGroovyDsl)
		then:
		result.assertTasksExecuted(tasks.withSample('foo-bar').allToZipGroovyDsl)
		zip("build/tmp/zipFooBarSampleGroovyDsl/FooBar-4.2-groovy-dsl.zip").hasDescendants('build.gradle', 'settings.gradle', 'gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties', 'src/main/java/com/example/Foo.java')
	}

	def "can include png files"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		file('src/docs/samples/foo-bar/image.png') << 'some-image-data'

		when:
		succeeds('assembleSamples')

		then:
		file('build/tmp/stageFooBarSample/image.png').assertExists()
	}

	def "can include gif files"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		file('src/docs/samples/foo-bar/image.gif') << 'some-image-data'

		when:
		succeeds('assembleSamples')

		then:
		file('build/tmp/stageFooBarSample/image.gif').assertExists()
	}

	def "can specify another wrapper version"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		buildFile << """
			documentation.samples.'foo-bar'.minimumGradleVersion = '6.4'
		"""

		when:
		succeeds('assembleSamples')

		then:
		file('build/tmp/stageFooBarSample/groovy-dsl/gradle/wrapper/gradle-wrapper.properties').text.contains('-6.4-')
		file('build/tmp/stageFooBarSample/kotlin-dsl/gradle/wrapper/gradle-wrapper.properties').text.contains('-6.4-')
	}

	def "can generate samples with dot files"() {
		given:
		makeSingleProject()
		writeComponentUnderTest()
		file('src/docs/samples/foo-bar/foo.dot') << '''
digraph G {
a -> b
}
		'''
		file('src/docs/samples/foo-bar/potato/bar.dot') << '''
digraph G {
a -> b
}
		'''

		when:
		succeeds('assembleSamples')

		then:
		file('build/tmp/stageFooBarSample/foo.png').assertExists()
		file('build/tmp/stageFooBarSample/potato/bar.png').assertExists()
	}

	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokeebuild.documentation'
			}

			version = '4.2'

			${configureTemplateFoo()}

			documentation {
				samples {
					'foo-bar' {
						template = new TemplateFoo()
					}
				}
			}
		"""
	}

	String configureTemplateFoo() {
		return configureTemplate('foo')
	}

	String configureTemplateBar() {
		return configureTemplate('bar')
	}

	TestFile getContentFile() {
		return file('src/docs/samples/foo-bar/README.adoc')
	}

	void writeComponentUnderTest() {
		writeBasicSample()
		writeBasicJbakeProject()
	}
}
