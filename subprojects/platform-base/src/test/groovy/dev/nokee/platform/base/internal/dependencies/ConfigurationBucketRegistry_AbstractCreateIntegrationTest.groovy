/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import org.gradle.api.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ConfigurationBucketRegistryImpl)
abstract class ConfigurationBucketRegistry_AbstractCreateIntegrationTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	protected abstract def create(ConfigurationBucketRegistryImpl subject, String name, ConfigurationBucketType type)

	@Unroll
	def "can create missing configuration"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		when:
		def result = create(subject, 'foo', type)

		then:
		project.configurations.findByName('foo') != null

		and:
		result != null
		result.canBeResolved == type.canBeResolved
		result.canBeConsumed == type.canBeConsumed

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "return existing configuration without changing properties"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def existingConfiguration = project.configurations.create('foo') {
			canBeResolved = type.canBeResolved
			canBeConsumed = type.canBeConsumed
		}

		when:
		def result = create(subject, 'foo', type)

		then:
		result == existingConfiguration
		result.canBeResolved == type.canBeResolved
		result.canBeConsumed == type.canBeConsumed

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "throw exception when existing configuration is not configured properly"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		project.configurations.create('foo') {
			canBeResolved = true
			canBeConsumed = true
		}

		when:
		create(subject, 'foo', type)

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a ${type.bucketTypeName} bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: ${type.canBeConsumed}, canBeResolved: ${type.canBeResolved}], actual: [canBeConsumed: true, canBeResolved: true])."

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "does not trigger configuration container rules when configuration is absent"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def rule = Mock(Rule)
		project.configurations.addRule(rule)

		when:
		create(subject, 'foo', type)

		then:
		0 * rule.apply('foo')

		where:
		type << ConfigurationBucketType.values()
	}
}
