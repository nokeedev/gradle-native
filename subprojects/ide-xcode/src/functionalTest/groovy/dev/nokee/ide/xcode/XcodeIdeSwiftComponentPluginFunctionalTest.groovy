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
package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.junit.Assume

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftApplicationFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-application'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterApp()
	}

	@Override
	protected String getGroupName() {
		return "App"
	}

	@Override
	protected String getSchemeName() {
		return "App"
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftApplicationWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new SwiftGreeterApp().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-application'
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent application
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterTest('App')
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return super.configureCustomSourceLayout()
	}

	@Override
	protected String getSchemeName() {
		return 'AppTest'
	}

	@Override
	protected String getGroupName() {
		return 'AppTest'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-library'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeter()
	}

	@Override
	protected String getGroupName() {
		return "Lib"
	}

	@Override
	protected String getSchemeName() {
		return "Lib"
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new SwiftGreeterLib().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-library'
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterTest('Lib')
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return super.configureCustomSourceLayout()
	}

	@Override
	protected String getSchemeName() {
		return 'LibTest'
	}

	@Override
	protected String getGroupName() {
		return 'LibTest'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithStaticLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithSharedLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithBothLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected String getSchemeName() {
		return "${super.getSchemeName()}Shared"
	}
}
