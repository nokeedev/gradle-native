package dev.nokee.language.base.internal.rules

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInternal
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(LanguageSourceSetConventionRule)
class LanguageSourceSetConventionRuleTest extends Specification {
	def project = ProjectBuilder.builder().build()

	protected File file(String relativePath) {
		def result = project.file(relativePath)
		result.parentFile.mkdirs()
		result.createNewFile()
		return result
	}

	def "configures source set convention based on main component owner and source set name"() {
		given:
		def subject = new LanguageSourceSetConventionRule(project.objects)
		def identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('c'), LanguageSourceSetInternal, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))
		def sourceSet = Mock(LanguageSourceSetInternal) {
			getIdentifier() >> identifier
		}

		and:
		def file1 = file('src/main/c/foo')
		def file2 = file('src/main/cpp/foo')
		def file3 = file('src/test/c/foo')

		when:
		subject.execute(sourceSet)

		then:
		1 * sourceSet.convention({ it.files == [file1] as Set })
	}

	def "configures source set convention based on non-main component owner and source set name"() {
		given:
		def subject = new LanguageSourceSetConventionRule(project.objects)
		def identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('cpp'), LanguageSourceSetInternal, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project)))
		def sourceSet = Mock(LanguageSourceSetInternal) {
			getIdentifier() >> identifier
		}

		and:
		def file1 = file('src/test/c/foo')
		def file2 = file('src/test/cpp/foo')
		def file3 = file('src/main/c/foo')

		when:
		subject.execute(sourceSet)

		then:
		1 * sourceSet.convention({ it.files == [file2] as Set })
	}

	def "throws exception when owner is not component"() {
		given:
		def subject = new LanguageSourceSetConventionRule(project.objects)
		def identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('c'), LanguageSourceSetInternal, Stub(DomainObjectIdentifierInternal))
		def sourceSet = Stub(LanguageSourceSetInternal) {
			getIdentifier() >> identifier
		}

		when:
		subject.execute(sourceSet)

		then:
		thrown(AssertionError)
	}
}
