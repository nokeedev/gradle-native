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
package dev.nokee.platform.ios

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import dev.nokee.platform.ios.fixtures.SwiftIosApp
import org.junit.Assume
import spock.lang.Ignore
import spock.lang.Requires

@Ignore
@Requires({ os.macOs })
@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftIosApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames, IosTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftIosApp()
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << '''
			rootProject.name = 'application'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-ios-application'
			}
		'''
	}

	@Override
	protected void makeProjectWithLibrary() {
		Assume.assumeTrue(false)
	}
}
