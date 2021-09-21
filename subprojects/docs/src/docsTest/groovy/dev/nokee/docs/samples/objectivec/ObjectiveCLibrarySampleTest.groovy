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
package dev.nokee.docs.samples.objectivec

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest
import org.apache.commons.lang3.SystemUtils

import static org.junit.Assume.assumeFalse

class ObjectiveCLibrarySampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'objective-c-library'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		assumeFalse(SystemUtils.IS_OS_WINDOWS)
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
