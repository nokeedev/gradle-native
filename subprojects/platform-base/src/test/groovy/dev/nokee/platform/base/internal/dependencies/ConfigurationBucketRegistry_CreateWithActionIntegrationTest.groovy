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
package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ConfigurationBucketRegistryImpl)
class ConfigurationBucketRegistry_CreateWithActionIntegrationTest extends ConfigurationBucketRegistry_AbstractCreateIntegrationTest {
	@Override
	protected create(ConfigurationBucketRegistryImpl subject, String name, ConfigurationBucketType type) {
		return subject.createIfAbsent(name, type, Stub(Action))
	}

	@Unroll
	def "execute action when configuration is absent"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def action = Mock(Action)

		when:
		subject.createIfAbsent('foo', type, action)

		then:
		1 * action.execute({ it.name == 'foo' })

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "does not execute action when configuration is present"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def action = Mock(Action)

		and:
		project.configurations.create('foo') {
			canBeConsumed = type.canBeConsumed
			canBeResolved = type.canBeResolved
		}

		when:
		subject.createIfAbsent('foo', type, action)

		then:
		0 * action.execute(_)

		where:
		type << ConfigurationBucketType.values()
	}
}

