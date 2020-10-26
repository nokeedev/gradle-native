package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import org.gradle.util.Path
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.language.base.internal.LanguageSourceSetIdentifier.of
import static dev.nokee.language.base.internal.LanguageSourceSetName.of as languageName

@Subject(LanguageSourceSetIdentifier)
class LanguageSourceSetIdentifierTest extends Specification {
	protected LanguageSourceSetIdentifier newSubject(String name) {
		return LanguageSourceSetIdentifier.of(languageName(name), LanguageSourceSet, Stub(DomainObjectIdentifierInternal))
	}

	protected LanguageSourceSetIdentifier newSubject(String name, DomainObjectIdentifierInternal owner) {
		return LanguageSourceSetIdentifier.of(languageName(name), LanguageSourceSet, owner)
	}

	protected LanguageSourceSetIdentifier newSubject(String name, Class type) {
		return LanguageSourceSetIdentifier.of(languageName(name), type, Stub(DomainObjectIdentifierInternal))
	}

	def "can create identifier"() {
		when:
		newSubject('c')

		then:
		noExceptionThrown()
	}

	def "can query the language source set name"() {
		expect:
		newSubject('c').name == languageName('c')
		newSubject('cpp').name == languageName('cpp')
	}

	def "can query the language source set type"() {
		expect:
		newSubject('c', LanguageSourceSet).type == LanguageSourceSet
		newSubject('c', TestableSourceSet).type == TestableSourceSet
	}

	def "can create language source set owned by a component"() {
		given:
		def ownerIdentifier1 = Stub(DomainObjectIdentifierInternal)
		def ownerIdentifier2 = Stub(DomainObjectIdentifierInternal)

		expect:
		newSubject('c', ownerIdentifier1).ownerIdentifier == ownerIdentifier1
		newSubject('c', ownerIdentifier2).ownerIdentifier == ownerIdentifier2

		and:
		newSubject('c', ownerIdentifier1).parentIdentifier.present
		newSubject('c', ownerIdentifier1).parentIdentifier.get() == ownerIdentifier1
		newSubject('c', ownerIdentifier2).parentIdentifier.present
		newSubject('c', ownerIdentifier2).parentIdentifier.get() == ownerIdentifier2
	}

	def "assert name is not null"() {
		when:
		of(null, LanguageSourceSet, Stub(DomainObjectIdentifierInternal))

		then:
		thrown(AssertionError)
	}

	def "assert type is not null"() {
		when:
		of(languageName('c'), null, Stub(DomainObjectIdentifierInternal))

		then:
		thrown(AssertionError)
	}

	def "assert owner is not null"() {
		when:
		of(languageName('c'), LanguageSourceSet, null)

		then:
		thrown(AssertionError)
	}

	def "has meaningful toString() implementation"() {
		given:
		def rootOwner = Stub(DomainObjectIdentifierInternal) {
			getPath() >> Path.ROOT
		}
		def childOwner = Stub(DomainObjectIdentifierInternal) {
			getPath() >> Path.path(':test')
		}

		expect:
		of(languageName('c'), TestableSourceSet, rootOwner).toString() == "source set ':c' (${TestableSourceSet.simpleName})"
		of(languageName('swift'), TestableSourceSet, childOwner).toString() == "source set ':test:swift' (${TestableSourceSet.simpleName})"
	}

	interface TestableSourceSet extends LanguageSourceSet {}
}
