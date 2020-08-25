package dev.nokee.language.base.internal.rules

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.language.base.LanguageSourceSetFactory
import dev.nokee.language.base.LanguageSourceSetFactoryRegistry
import spock.lang.Specification
import spock.lang.Subject

@Subject(RegisterLanguageFactoriesRule)
class RegisterLanguageFactoriesRuleTest extends Specification {
	def "can register factories from a map"() {
		given:
		def factoryA = Mock(LanguageSourceSetFactory)
		def factoryB = Mock(LanguageSourceSetFactory)
		def factories = [(ASourceSet.class): factoryA, (BSourceSet.class): factoryB]
		def registry = Mock(LanguageSourceSetFactoryRegistry)
		def subject = new RegisterLanguageFactoriesRule(factories, registry)

		when:
		subject.run()

		then:
		1 * registry.registerFactory(ASourceSet, factoryA)
		1 * registry.registerFactory(BSourceSet, factoryB)
		0 * _
	}

	interface ASourceSet extends LanguageSourceSet {}
	interface BSourceSet extends LanguageSourceSet {}
}
