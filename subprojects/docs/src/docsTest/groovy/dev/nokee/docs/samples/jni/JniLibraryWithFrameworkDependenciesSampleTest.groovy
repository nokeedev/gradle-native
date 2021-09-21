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
package dev.nokee.docs.samples.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest
import org.apache.commons.lang3.SystemUtils
import org.junit.Assume

class JniLibraryWithFrameworkDependenciesSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'jni-library-with-framework-dependencies'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		Assume.assumeTrue(SystemUtils.IS_OS_MAC)
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
