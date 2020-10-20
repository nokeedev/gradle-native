package dev.nokee.platform.base.internal

import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.BaseNameUtils.from

@Subject(BaseNameUtils)
class BaseNameUtilsTest extends Specification {
	private static ProjectIdentifier projectId(String name) {
		return ProjectIdentifier.of(name)
	}

	private static ComponentIdentifier componentId(ProjectIdentifier owner) {
		return ComponentIdentifier.ofMain(Component, owner)
	}

	private static ComponentIdentifier componentId(String name, ProjectIdentifier owner) {
		return ComponentIdentifier.of(ComponentName.of(name), Component, owner)
	}

	private static VariantIdentifier variantId(String name, ComponentIdentifier owner) {
		return VariantIdentifier.of(name, Variant, owner)
	}

	def "creates a base name of main component"() {
		expect:
		from(componentId(projectId('foo'))).asString == 'foo'
		from(componentId(projectId('foo-bar'))).asString == 'foo-bar'
		from(componentId(projectId('foo_bar'))).asString == 'foo_bar'
		from(componentId(projectId('fooBar'))).asString == 'fooBar'
	}

	def "creates a base name of non-main component"() {
		expect:
		from(componentId('test', projectId('foo'))).asString == 'foo-test'
		from(componentId('test', projectId('foo-bar'))).asString == 'foo-bar-test'
		from(componentId('test', projectId('foo_bar'))).asString == 'foo_bar-test'
		from(componentId('test', projectId('fooBar'))).asString == 'fooBar-test'

		and:
		from(componentId('integTest', projectId('foo'))).asString == 'foo-integTest'
		from(componentId('functionalTest', projectId('foo'))).asString == 'foo-functionalTest'
	}

	def "creates a base name of variant owned by main component"() {
		expect:
		from(variantId('debug', componentId(projectId('foo')))).asString == 'foo'
		from(variantId('debug', componentId(projectId('foo-bar')))).asString == 'foo-bar'
		from(variantId('debug', componentId(projectId('foo_bar')))).asString == 'foo_bar'
		from(variantId('debug', componentId(projectId('fooBar')))).asString == 'fooBar'
	}

	def "creates a base name of variant ownered by non-main component"() {
		expect:
		from(variantId('debug', componentId('test', projectId('foo')))).asString == 'foo-test'
		from(variantId('debug', componentId('test', projectId('foo-bar')))).asString == 'foo-bar-test'
		from(variantId('debug', componentId('test', projectId('foo_bar')))).asString == 'foo_bar-test'
		from(variantId('debug', componentId('test', projectId('fooBar')))).asString == 'fooBar-test'

		and:
		from(variantId('debug', componentId('integTest', projectId('foo')))).asString == 'foo-integTest'
		from(variantId('debug', componentId('functionalTest', projectId('foo')))).asString == 'foo-functionalTest'
	}
}
