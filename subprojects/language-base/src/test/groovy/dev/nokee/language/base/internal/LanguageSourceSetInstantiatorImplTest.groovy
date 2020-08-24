package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import spock.lang.Specification
import spock.lang.Subject

@Subject(LanguageSourceSetInstantiatorImpl)
class LanguageSourceSetInstantiatorImplTest extends Specification {
	def subject = new LanguageSourceSetInstantiatorImpl()

	def "can register factories"() {
		given:
		def factory = Mock(LanguageSourceSetFactory)

		when:
		subject.registerFactory(TestSourceSet, factory)

		then:
		subject.creatableTypes == [TestSourceSet] as Set
	}

	def "has no creatable types"() {
		expect:
		subject.creatableTypes == [] as Set
	}

	// TODO: throws an exception when registering multiple time the same factory type
	// TODO: throws an exception if factory type is outside base type

	interface TestSourceSet extends LanguageSourceSet {}
}
