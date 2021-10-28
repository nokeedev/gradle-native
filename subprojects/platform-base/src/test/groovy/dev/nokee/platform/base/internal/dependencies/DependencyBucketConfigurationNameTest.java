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
import dev.nokee.platform.base.internal.VariantIdentifier;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.ProjectIdentifier.ofRootProject;
import static dev.nokee.platform.base.internal.ComponentIdentifier.ofMain;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.*;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.configurationName;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyBucketConfigurationNameTest {
	@Test
	void canGenerateConfigurationNameForProjectOwnedIdentifierIsTheSameAsBucketName() {
		assertEquals("implementation", configurationName(of(declarable("implementation"), ofRootProject())));
		assertEquals("compileOnly", configurationName(of(declarable("compileOnly"), ofRootProject())));
		assertEquals("linkLibraries", configurationName(of(resolvable("linkLibraries"), ofRootProject())));
		assertEquals("headerSearchPaths", configurationName(of(consumable("headerSearchPaths"), ofRootProject())));
	}

	@Test
	void canGenerateConfigurationNameForMainComponentOwnedIdentifierIsTheSameAsBucketName() {
		assertEquals("implementation", configurationName(of(declarable("implementation"), ofMain(ofRootProject()))));
		assertEquals("compileOnly", configurationName(of(declarable("compileOnly"), ofMain(ofRootProject()))));
		assertEquals("linkLibraries", configurationName(of(resolvable("linkLibraries"), ofMain(ofRootProject()))));
		assertEquals("headerSearchPaths", configurationName(of(consumable("headerSearchPaths"), ofMain(ofRootProject()))));
	}

	@Test
	void canGenerateConfigurationNameForNonMainComponentOwnedIdentifierStartsWithComponentName() {
		assertEquals("testImplementation",
			configurationName(of(declarable("implementation"), ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testCompileOnly",
			configurationName(of(declarable("compileOnly"), ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testLinkLibraries",
			configurationName(of(resolvable("linkLibraries"), ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("testHeaderSearchPaths",
			configurationName(of(consumable("headerSearchPaths"), ComponentIdentifier.of("test", ofRootProject()))));
	}

	@Test
	void canGenerateConfigurationNameForVariantOwnedIdentifierOfMainComponentStartsWithAmbiguousVariantName() {
		assertEquals("macosDebugImplementation",
			configurationName(of(declarable("implementation"), VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugCompileOnly",
			configurationName(of(declarable("compileOnly"), VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugLinkLibraries",
			configurationName(of(resolvable("linkLibraries"), VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("macosDebugHeaderSearchPaths",
			configurationName(of(consumable("headerSearchPaths"), VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForVariantOwnedIdentifierOfNonMainComponentStartsWithComponentNameFollowedByAmbiguousVariantName() {
		assertEquals("testMacosDebugImplementation",
			configurationName(of(declarable("implementation"),
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugCompileOnly",
			configurationName(of(declarable("compileOnly"),
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugLinkLibraries",
			configurationName(of(resolvable("linkLibraries"),
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testMacosDebugHeaderSearchPaths",
			configurationName(of(consumable("headerSearchPaths"),
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForUniqueVariantOwnedIdentifierOfMainComponentIsTheSameAsBucketName() {
		assertEquals("implementation",
			configurationName(of(declarable("implementation"), VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("compileOnly",
			configurationName(of(declarable("compileOnly"), VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("linkLibraries",
			configurationName(of(resolvable("linkLibraries"), VariantIdentifier.of("", ofMain(ofRootProject())))));
		assertEquals("headerSearchPaths",
			configurationName(of(consumable("headerSearchPaths"), VariantIdentifier.of("", ofMain(ofRootProject())))));
	}

	@Test
	void canGenerateConfigurationNameForUniqueVariantOwnedIdentifierOfNonMainComponentStartsWithComponentName() {
		assertEquals("testImplementation",
			configurationName(of(declarable("implementation"),
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testCompileOnly",
			configurationName(of(declarable("compileOnly"),
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testLinkLibraries",
			configurationName(of(resolvable("linkLibraries"),
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("testHeaderSearchPaths",
			configurationName(of(consumable("headerSearchPaths"),
				VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject())))));
	}
}
