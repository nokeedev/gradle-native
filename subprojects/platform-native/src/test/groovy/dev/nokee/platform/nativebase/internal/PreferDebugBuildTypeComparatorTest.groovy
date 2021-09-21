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
package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.BuildType
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferDebugBuildTypeComparator)
class PreferDebugBuildTypeComparatorTest extends Specification {
	def subject = new PreferDebugBuildTypeComparator()

	def "always prefer debug build type"() {
		expect:
		subject.compare(debug, release) == -1
		subject.compare(release, debug) == 1
	}

	def "no opinion on different build types that is not debug"() {
		expect:
		subject.compare(release, notDebugOrRelease) == 0
	}

	def "no opinion on same build type"() {
		expect:
		subject.compare(debug, debug) == 0
		subject.compare(release, release) == 0
	}

	def "disregards the case of the debug build type"() {
		expect:
		subject.compare(buildTypeOf('debug'), buildTypeOf('Debug')) == 0
		subject.compare(buildTypeOf('dEbUg'), release) == -1
		subject.compare(release, buildTypeOf('DeBuG')) == 1
	}

	private BuildType getDebug() {
		return BuildType.named('debug')
	}

	private BuildType getRelease() {
		return BuildType.named('release')
	}

	private BuildType getNotDebugOrRelease() {
		return BuildType.named('final')
	}

	private BuildType buildTypeOf(String name) {
		return BuildType.named(name)
	}
}
