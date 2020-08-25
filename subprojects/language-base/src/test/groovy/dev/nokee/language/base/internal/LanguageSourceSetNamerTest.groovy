package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import org.gradle.api.reflect.TypeOf
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(LanguageSourceSetNamer)
class LanguageSourceSetNamerTest extends Specification {
	def subject = LanguageSourceSetNamer.INSTANCE

	@Unroll
	def "calculates display name from public type name"(publicType, name) {
		given:
		def type = Mock(LanguageSourceSet) {
			getPublicType() >> TypeOf.typeOf(publicType)
		}

		expect:
		subject.determineName(type) == name

		where:
		publicType                | name
		SomeTypeLanguageSourceSet | "SomeType"
		SomeTypeSourceSet         | "SomeType"
		SomeTypeSource            | "SomeType"
		SomeTypeSet               | "SomeType"
		SomeType                  | "SomeType"
		SomeResourcesSet          | "SomeResources"
	}

	interface SomeTypeLanguageSourceSet extends LanguageSourceSet {}

	interface SomeTypeSourceSet extends LanguageSourceSet {}

	interface SomeTypeSet extends LanguageSourceSet {}

	interface SomeTypeSource extends LanguageSourceSet {}

	interface SomeType extends LanguageSourceSet {}

	interface SomeResourcesSet extends LanguageSourceSet {}
}
