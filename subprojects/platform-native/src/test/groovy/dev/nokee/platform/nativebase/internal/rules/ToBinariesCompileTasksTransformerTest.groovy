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
package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.TaskView
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.utils.ProviderUtils
import org.gradle.api.provider.Provider
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

import static ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS

@Subject(ToBinariesCompileTasksTransformer)
@Ignore
class ToBinariesCompileTasksTransformerTest extends Specification {
	def "returns provider to empty list when development binary is not a native binary"() {
		given:
		def variant = Stub(Variant) {
			getDevelopmentBinary() >> ProviderUtils.fixed(Stub(Binary))
		}

		when:
		def provider = TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)

		then:
		provider != null
		provider.present
		provider.get().isEmpty()
	}

	def "returns provider to binary's compile tasks when development binary is a native binary"() {
		given:
		def taskViewElements = Stub(Provider)
		def taskView = Mock(TaskView)
		def binary = Mock(NativeBinary)
		def variant = Stub(Variant) {
			getDevelopmentBinary() >> ProviderUtils.fixed(binary)
		}

		when:
		def provider = TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)

		then:
		provider == taskViewElements
		1 * binary.getCompileTasks() >> taskView
		1 * taskView.getElements() >> taskViewElements
	}
}
