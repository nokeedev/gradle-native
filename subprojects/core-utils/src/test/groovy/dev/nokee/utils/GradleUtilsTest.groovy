/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils

import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.invocation.Gradle
import spock.lang.Specification
import spock.lang.Subject

@Subject(GradleUtils)
class GradleUtilsTest extends Specification {
	def "can detect root project"() {
		given:
		def hostBuild = Mock(Gradle) {
			getParent() >> null
		}
		def includedBuild = Mock(Gradle) {
			getParent() >> hostBuild
		}

		expect:
		GradleUtils.isHostBuild(hostBuild)
		!GradleUtils.isHostBuild(includedBuild)
	}

	def "can detect included builds"() {
		given:
		def buildWithIncludedBuilds = Mock(Gradle) {
			getIncludedBuilds() >> [Mock(IncludedBuild), Mock(IncludedBuild)]
		}
		def buildWithoutIncludedBuilds = Mock(Gradle) {
			getIncludedBuilds() >> []
		}

		expect:
		GradleUtils.hasIncludedBuilds(buildWithIncludedBuilds)
		!GradleUtils.hasIncludedBuilds(buildWithoutIncludedBuilds)
	}

	def "can detect when a build is included inside another one"() {
		given:
		def root = Mock(Gradle) {
			getParent() >> null
		}
		def child = Mock(Gradle) {
			getParent() >> root
		}

		expect:
		!GradleUtils.isIncludedBuild(root)
		GradleUtils.isIncludedBuild(child)
	}

	def "can detect when a build is a composite build"() {
		given:
		def root = Mock(Gradle) {
			getParent() >> null
		}
		def child = Mock(Gradle) {
			getParent() >> root
		}

		and:
		root.getIncludedBuilds() >> [child]
		child.getIncludedBuilds() >> []

		expect:
		GradleUtils.isCompositeBuild(root)
		GradleUtils.isCompositeBuild(child)
	}

	def "can detect when a build is a not a composite build"() {
		given:
		def build = Mock(Gradle) {
			getParent() >> null
			getIncludedBuilds() >> []
		}

		expect:
		!GradleUtils.isCompositeBuild(build)
	}
}
