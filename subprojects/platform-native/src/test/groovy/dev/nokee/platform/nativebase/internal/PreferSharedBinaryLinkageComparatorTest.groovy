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

import dev.nokee.runtime.nativebase.BinaryLinkage
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferSharedBinaryLinkageComparator)
class PreferSharedBinaryLinkageComparatorTest extends Specification {
	def subject = new PreferSharedBinaryLinkageComparator()

	def "always prefer shared binary linkage"() {
		expect:
		subject.compare(shared, getStatic()) == -1
		subject.compare(getStatic(), shared) == 1
	}

	def "no opinion on different binary linkage that is not shared"() {
		expect:
		subject.compare(getStatic(), notSharedAndStatic) == 0
	}

	def "no opinion same build type"() {
		expect:
		subject.compare(shared, shared) == 0
		subject.compare(getStatic(), getStatic()) == 0
	}

	private BinaryLinkage getShared() {
		return BinaryLinkage.named(BinaryLinkage.SHARED)
	}

	private BinaryLinkage getStatic() {
		return BinaryLinkage.named(BinaryLinkage.STATIC)
	}

	private BinaryLinkage getNotSharedAndStatic() {
		return BinaryLinkage.named('some-linkage')
	}
}
