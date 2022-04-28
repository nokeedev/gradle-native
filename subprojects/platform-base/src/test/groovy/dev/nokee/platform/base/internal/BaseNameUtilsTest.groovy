/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal

import dev.nokee.model.internal.ProjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.BaseNameUtils.from

@Subject(BaseNameUtils)
class BaseNameUtilsTest extends Specification {
	private static ProjectIdentifier projectId(String name) {
		return ProjectIdentifier.of(name)
	}

	private static ComponentIdentifier componentId(ProjectIdentifier owner) {
		return ComponentIdentifier.ofMain(owner)
	}

	private static ComponentIdentifier componentId(String name, ProjectIdentifier owner) {
		return ComponentIdentifier.of(ComponentName.of(name), owner)
	}

	private static VariantIdentifier variantId(String name, ComponentIdentifier owner) {
		return VariantIdentifier.of(name, owner)
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
