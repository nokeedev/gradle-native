package dev.nokee.language.base.internal

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import spock.lang.Specification

import static dev.nokee.language.base.internal.ConventionalRelativeLanguageSourceSetPath.builder
import static dev.nokee.language.base.internal.ConventionalRelativeLanguageSourceSetPath.of

class ConventionalRelativeLanguageSourceSetPathTest extends Specification {
	protected LanguageSourceSetIdentifier newSourceSetIdentifier(String componentName, String sourceSetName) {
		return LanguageSourceSetIdentifier.of(LanguageSourceSetName.of(sourceSetName), LanguageSourceSetInternal, ComponentIdentifier.of(ComponentName.of(componentName), Component, ProjectIdentifier.of('root')))
	}

	def "can create conventional relative path from source set identifier owned by main component"() {
		expect:
		of(newSourceSetIdentifier('main', 'c')).get() == 'src/main/c'
		of(newSourceSetIdentifier('main', 'cpp')).get() == 'src/main/cpp'
		of(newSourceSetIdentifier('main', 'public')).get() == 'src/main/public'
		of(newSourceSetIdentifier('test', 'objc')).get() == 'src/test/objc'
		of(newSourceSetIdentifier('integTest', 'swift')).get() == 'src/integTest/swift'
	}

	def "can use conventional value as Gradle file value"() {
		given:
		def project = TestUtils.rootProject()

		expect:
		project.file(of(newSourceSetIdentifier('main', 'c'))) == project.file('src/main/c')
		project.file(of(newSourceSetIdentifier('main', 'public'))) == project.file('src/main/public')
		project.file(of(newSourceSetIdentifier('test', 'objc'))) == project.file('src/test/objc')
		project.file(of(newSourceSetIdentifier('integTest', 'swift'))) == project.file('src/integTest/swift')
	}

	def "can build conventional relative path from source set identifier"() {
		expect:
		builder().fromIdentifier(newSourceSetIdentifier('main', 'c')).build().get() == 'src/main/c'
		builder().fromIdentifier(newSourceSetIdentifier('main', 'public')).build().get() == 'src/main/public'
		builder().fromIdentifier(newSourceSetIdentifier('test', 'cpp')).build().get() == 'src/test/cpp'
	}

	def "can overwrite builder source set name"() {
		expect:
		builder().fromIdentifier(newSourceSetIdentifier('main', 'c')).withSourceSetName('cpp').build().get() == 'src/main/cpp'
		builder().fromIdentifier(newSourceSetIdentifier('main', 'public')).withSourceSetName('headers').build().get() == 'src/main/headers'
		builder().fromIdentifier(newSourceSetIdentifier('test', 'cpp')).withSourceSetName('c').build().get() == 'src/test/c'
	}

	def "can build conventional relative path from component and source set name"() {
		expect:
		builder().withComponentName('main').withSourceSetName('c').build().get() == 'src/main/c'
		builder().withComponentName('main').withSourceSetName('public').build().get() == 'src/main/public'
		builder().withComponentName('test').withSourceSetName('cpp').build().get() == 'src/test/cpp'
	}

	def "throws exception when builder is missing component name"() {
		when:
		builder().withSourceSetName('c').build()

		then:
		thrown(AssertionError)
	}

	def "throws exception when builder is missing sourceSet name"() {
		when:
		builder().withComponentName('main').build()

		then:
		thrown(AssertionError)
	}
}
