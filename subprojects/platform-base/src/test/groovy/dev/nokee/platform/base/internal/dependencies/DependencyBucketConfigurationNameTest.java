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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.VariantIdentifier;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.ProjectIdentifier.ofRootProject;
import static dev.nokee.platform.base.internal.ComponentIdentifier.ofMain;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyBucketConfigurationNameTest {
	private static String configurationName(DependencyBucketIdentifier identifier) {
		return ConfigurationNamer.INSTANCE.determineName(identifier);
	}

	@Test
	void canGenerateConfigurationNameForProjectOwnedIdentifierIsTheSameAsBucketName() {
		assertEquals("implementation", configurationName(of("implementation", ofRootProject())));
		assertEquals("compileOnly", configurationName(of("compileOnly", ofRootProject())));
		assertEquals("linkLibraries", configurationName(of("linkLibraries", ofRootProject())));
		assertEquals("headerSearchPaths", configurationName(of("headerSearchPaths", ofRootProject())));
	}

	@Test
	void canGenerateConfigurationNameForMainComponentOwnedIdentifierIsTheSameAsBucketName() {
		assertEquals("implementation", configurationName(of("implementation", ofMain(ofRootProject()))));
		assertEquals("compileOnly", configurationName(of("compileOnly", ofMain(ofRootProject()))));
		assertEquals("linkLibraries", configurationName(of("linkLibraries", ofMain(ofRootProject()))));
		assertEquals("headerSearchPaths", configurationName(of("headerSearchPaths", ofMain(ofRootProject()))));
	}

	@Test
	void canGenerateConfigurationNameForNonMainComponentOwnedIdentifierStartsWithComponentName() {
		assertEquals("testImplementation",
			configurationName(of("implementation", ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testCompileOnly",
			configurationName(of("compileOnly", ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testLinkLibraries",
			configurationName(of("linkLibraries", ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testHeaderSearchPaths",
			configurationName(of("headerSearchPaths", ComponentIdentifier.of("test", ofRootProject()))));
	}

	@Test
	void canGenerateConfigurationNameForVariantOwnedIdentifierOfMainComponentStartsWithAmbiguousVariantName() {
		assertEquals("macosDebugImplementation",
			configurationName(of("implementation", VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugCompileOnly",
			configurationName(of("compileOnly", VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugLinkLibraries",
			configurationName(of("linkLibraries", VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugHeaderSearchPaths",
			configurationName(of("headerSearchPaths", VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForVariantOwnedIdentifierOfNonMainComponentStartsWithComponentNameFollowedByAmbiguousVariantName() {
		assertEquals("testMacosDebugImplementation",
			configurationName(of("implementation",
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugCompileOnly",
			configurationName(of("compileOnly",
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugLinkLibraries",
			configurationName(of("linkLibraries",
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugHeaderSearchPaths",
			configurationName(of("headerSearchPaths",
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForUniqueVariantOwnedIdentifierOfMainComponentIsTheSameAsBucketName() {
		assertEquals("implementation",
			configurationName(of("implementation", VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("compileOnly",
			configurationName(of("compileOnly", VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("linkLibraries",
			configurationName(of("linkLibraries", VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("headerSearchPaths",
			configurationName(of("headerSearchPaths", VariantIdentifier.of("", ofMain(ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForUniqueVariantOwnedIdentifierOfNonMainComponentStartsWithComponentName() {
		assertEquals("testImplementation",
			configurationName(of("implementation",
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testCompileOnly",
			configurationName(of("compileOnly",
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testLinkLibraries",
			configurationName(of("linkLibraries",
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testHeaderSearchPaths",
			configurationName(of("headerSearchPaths",
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
	}
}
