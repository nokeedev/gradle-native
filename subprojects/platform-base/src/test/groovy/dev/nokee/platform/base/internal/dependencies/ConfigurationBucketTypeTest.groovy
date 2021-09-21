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

import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ConfigurationBucketType)
class ConfigurationBucketTypeTest extends Specification {
	def "declarable bucket type cannot be consumed or resolved"() {
		expect:
		!ConfigurationBucketType.DECLARABLE.canBeResolved
		!ConfigurationBucketType.DECLARABLE.canBeConsumed
	}

	def "resolvable bucket type can only be resolved"() {
		expect:
		ConfigurationBucketType.RESOLVABLE.canBeResolved
		!ConfigurationBucketType.RESOLVABLE.canBeConsumed
	}

	def "consumable bucket type can only be consumed"() {
		expect:
		!ConfigurationBucketType.CONSUMABLE.canBeResolved
		ConfigurationBucketType.CONSUMABLE.canBeConsumed
	}

	def "can configure configuration as a declarable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.DECLARABLE.configure(configuration)

		then:
		1 * configuration.setCanBeResolved(false)
		1 * configuration.setCanBeConsumed(false)
		0 * configuration._
	}

	def "can configure configuration as a resolvable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.RESOLVABLE.configure(configuration)

		then:
		1 * configuration.setCanBeResolved(true)
		1 * configuration.setCanBeConsumed(false)
		0 * configuration._
	}

	def "can configure configuration as a consumable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.CONSUMABLE.configure(configuration)

		then:
		1 * configuration.setCanBeResolved(false)
		1 * configuration.setCanBeConsumed(true)
		0 * configuration._
	}

	def "consumable bucket type has name"() {
		expect:
		ConfigurationBucketType.CONSUMABLE.bucketTypeName == 'consumable'
	}

	def "resolvable bucket type has name"() {
		expect:
		ConfigurationBucketType.RESOLVABLE.bucketTypeName == 'resolvable'
	}

	def "declarable bucket type has name"() {
		expect:
		ConfigurationBucketType.DECLARABLE.bucketTypeName == 'declarable'
	}

	def "can assert existing configuration configured as declarable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.DECLARABLE.assertConfigured(configuration)

		then:
		1 * configuration.isCanBeResolved() >> false
		1 * configuration.isCanBeConsumed() >> false
		0 * configuration._

		and:
		noExceptionThrown()
	}

	@Unroll
	def "throws exception when existing configuration is not configured as declarable bucket type"(configuredCanBeConsumed, configuredCanBeResolved) {
		given:
		def configuration = Stub(Configuration) {
			getName() >> 'foo'
			isCanBeConsumed() >> configuredCanBeConsumed
			isCanBeResolved() >> configuredCanBeResolved
		}

		when:
		ConfigurationBucketType.DECLARABLE.assertConfigured(configuration)

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a declarable bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: false, canBeResolved: false], actual: [canBeConsumed: ${configuredCanBeConsumed}, canBeResolved: ${configuredCanBeResolved}])."

		where:
		configuredCanBeConsumed | configuredCanBeResolved
		true 					| true
		true 					| false
		false 					| true
	}

	def "can assert existing configuration configured as consumable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.CONSUMABLE.assertConfigured(configuration)

		then:
		1 * configuration.isCanBeResolved() >> false
		1 * configuration.isCanBeConsumed() >> true
		0 * configuration._

		and:
		noExceptionThrown()
	}

	@Unroll
	def "throws exception when existing configuration is not configured as consumable bucket type"(configuredCanBeConsumed, configuredCanBeResolved) {
		given:
		def configuration = Stub(Configuration) {
			getName() >> 'foo'
			isCanBeConsumed() >> configuredCanBeConsumed
			isCanBeResolved() >> configuredCanBeResolved
		}

		when:
		ConfigurationBucketType.CONSUMABLE.assertConfigured(configuration)

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a consumable bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: true, canBeResolved: false], actual: [canBeConsumed: ${configuredCanBeConsumed}, canBeResolved: ${configuredCanBeResolved}])."

		where:
		configuredCanBeConsumed | configuredCanBeResolved
		true 					| true
		false 					| false
		false 					| true
	}

	def "can assert existing configuration configured as resolvable bucket type"() {
		given:
		def configuration = Mock(Configuration)

		when:
		ConfigurationBucketType.RESOLVABLE.assertConfigured(configuration)

		then:
		1 * configuration.isCanBeResolved() >> true
		1 * configuration.isCanBeConsumed() >> false
		0 * configuration._

		and:
		noExceptionThrown()
	}

	@Unroll
	def "throws exception when existing configuration is not configured as resolvable bucket type"(configuredCanBeConsumed, configuredCanBeResolved) {
		given:
		def configuration = Stub(Configuration) {
			getName() >> 'foo'
			isCanBeConsumed() >> configuredCanBeConsumed
			isCanBeResolved() >> configuredCanBeResolved
		}

		when:
		ConfigurationBucketType.RESOLVABLE.assertConfigured(configuration)

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a resolvable bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: false, canBeResolved: true], actual: [canBeConsumed: ${configuredCanBeConsumed}, canBeResolved: ${configuredCanBeResolved}])."

		where:
		configuredCanBeConsumed | configuredCanBeResolved
		true 					| true
		true 					| false
		false 					| false
	}
}
