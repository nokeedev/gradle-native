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
package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.platform.base.Binary
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.StaticLibraryBinary
import dev.nokee.runtime.nativebase.BinaryLinkage
import dev.nokee.utils.ProviderUtils
import spock.lang.Specification

import static dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention.*

class NativeDevelopmentBinaryConventionTest extends Specification {
	def "select EXECUTABLE convention on executable linkage"() {
		expect:
		of(BinaryLinkage.named(BinaryLinkage.EXECUTABLE)) == EXECUTABLE
	}

	def "select SHARED convention on shared linkage"() {
		expect:
		of(BinaryLinkage.named(BinaryLinkage.SHARED)) == SHARED
	}

	def "select STATIC convention on static linkage"() {
		expect:
		of(BinaryLinkage.named(BinaryLinkage.STATIC)) == STATIC
	}

	def "throws exception for unsupported linkage"() {
		when:
		of(BinaryLinkage.named(BinaryLinkage.BUNDLE))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unsupported binary linkage 'bundle' for native development binary convention"
	}
}


abstract class AbstractNativeDevelopmentBinaryConventionTest extends Specification {
	protected abstract NativeDevelopmentBinaryConvention newSubject()
	protected abstract Class<? extends Binary> getDevelopmentBinaryType()

	def "returns undefined provider on empty list"() {
		expect:
		newSubject().transform([]) == ProviderUtils.notDefined()
	}

	def "throws exception when multiple development binaries"() {
		when:
		newSubject().transform([Stub(developmentBinaryType), Stub(developmentBinaryType)])

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "expected one element but was: <Mock for type '${developmentBinaryType.simpleName}', Mock for type '${developmentBinaryType.simpleName}'>"
	}

	def "returns a provider of the single development binary list"() {
		given:
		def binary = Stub(developmentBinaryType)

		expect:
		def result = newSubject().transform([binary])
		result.present
		result.get() == binary
	}

	def "returns a provider of a multi-binary list containing one development binary"() {
		given:
		def binary = Stub(developmentBinaryType)

		expect:
		def result1 = newSubject().transform([binary, Stub(Binary)])
		result1.present
		result1.get() == binary

		and:
		def result2 = newSubject().transform([Stub(Binary), binary])
		result2.present
		result2.get() == binary

		and:
		def result3 = newSubject().transform([Stub(Binary), binary, Stub(Binary)])
		result3.present
		result3.get() == binary
	}
}

class NativeDevelopmentBinaryConventionExecutableTest extends AbstractNativeDevelopmentBinaryConventionTest {

	@Override
	protected NativeDevelopmentBinaryConvention newSubject() {
		return EXECUTABLE
	}

	@Override
	protected Class<? extends Binary> getDevelopmentBinaryType() {
		return ExecutableBinary
	}
}

class NativeDevelopmentBinaryConventionSharedTest extends AbstractNativeDevelopmentBinaryConventionTest {

	@Override
	protected NativeDevelopmentBinaryConvention newSubject() {
		return NativeDevelopmentBinaryConvention.SHARED
	}

	@Override
	protected Class<? extends Binary> getDevelopmentBinaryType() {
		return SharedLibraryBinary
	}
}

class NativeDevelopmentBinaryConventionStaticTest extends AbstractNativeDevelopmentBinaryConventionTest {

	@Override
	protected NativeDevelopmentBinaryConvention newSubject() {
		return NativeDevelopmentBinaryConvention.STATIC
	}

	@Override
	protected Class<? extends Binary> getDevelopmentBinaryType() {
		return StaticLibraryBinary
	}
}
