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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.ProjectIdentifier.ofChildProject;
import static dev.nokee.model.internal.ProjectIdentifier.ofRootProject;
import static dev.nokee.platform.base.internal.ComponentIdentifier.ofMain;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.*;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.toDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyBucketDescriptionTest {
	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByARootProject() {
		assertEquals("Implementation dependencies for project ':'.", toDescription(of(declarable("implementation"), ofRootProject())));
	}

	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByAProject() {
		assertEquals("Implementation dependencies for project ':jine'.",
			toDescription(of(declarable("implementation"), ofChildProject("jine"))));
	}

	@Test
	void canGenerateNameForTwoWordsBucketNameOwnedByAProject() {
		assertEquals("Compile only dependencies for project ':'.", toDescription(of(declarable("compileOnly"), ofRootProject())));
		assertEquals("Link only dependencies for project ':'.", toDescription(of(declarable("linkOnly"), ofRootProject())));
		assertEquals("Runtime elements for project ':'.", toDescription(of(consumable("runtimeElements"), ofRootProject())));
		assertEquals("Link libraries for project ':'.", toDescription(of(resolvable("linkLibraries"), ofRootProject())));
	}

	@Test
	void canGenerateNameForThreeWordsBucketNameOwnedByAProject() {
		assertEquals("Header search paths for project ':'.", toDescription(of(resolvable("headerSearchPaths"), ofRootProject())));
		assertEquals("Import swift modules for project ':'.", toDescription(of(resolvable("importSwiftModules"), ofRootProject())));
		// TODO: Maybe we should capitalize the language name
	}

	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByTheMainComponent() {
		assertEquals("Implementation dependencies for component ':main'.",
			toDescription(of(declarable("implementation"), ofMain(ofRootProject()))));
	}

	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByANonMainComponent() {
		assertEquals("Implementation dependencies for component ':test'.",
			toDescription(of(declarable("implementation"), ComponentIdentifier.of("test", ofRootProject()))));
		assertEquals("Implementation dependencies for component ':integTest'.",
			toDescription(of(declarable("implementation"), ComponentIdentifier.of("integTest", ofRootProject()))));
		assertEquals("Implementation dependencies for component ':uiTest'.",
			toDescription(of(declarable("implementation"), ComponentIdentifier.of("uiTest", ofRootProject()))));
		assertEquals("Implementation dependencies for component ':unitTest'.",
			toDescription(of(declarable("implementation"), ComponentIdentifier.of("unitTest", ofRootProject()))));
	}

	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByAVariantOfTheMainComponent() {
		assertEquals("Implementation dependencies for variant ':main:macosDebug'.",
			toDescription(of(declarable("implementation"), VariantIdentifier.of("macosDebug", ofMain(ofRootProject())))));
		assertEquals("Implementation dependencies for variant ':main:x86-64Debug'.",
			toDescription(of(declarable("implementation"), VariantIdentifier.of("x86-64Debug", ofMain(ofRootProject())))));
		assertEquals("Implementation dependencies for variant ':main:windowsX86'.",
			toDescription(of(declarable("implementation"), VariantIdentifier.of("windowsX86", ofMain(ofRootProject())))));
	}

	@Test
	void canGenerateNameForSingleWordBucketNameOwnedByAVariantOfTheNonMainComponent() {
		assertEquals("Implementation dependencies for variant ':test:macosDebug'.",
			toDescription(of(declarable("implementation"),
				VariantIdentifier.of("macosDebug", ComponentIdentifier.of("test", ofRootProject())))));
		assertEquals("Implementation dependencies for variant ':integTest:x86-64Debug'.",
			toDescription(of(declarable("implementation"),
				VariantIdentifier.of("x86-64Debug", ComponentIdentifier.of("integTest", ofRootProject())))));
		assertEquals("Implementation dependencies for variant ':uiTest:windowsX86'.",
			toDescription(of(declarable("implementation"),
				VariantIdentifier.of("windowsX86", ComponentIdentifier.of("uiTest", ofRootProject())))));
	}

	@Test
	void alwaysCapitalizeApiWord() {
		assertEquals("API dependencies for project ':'.", toDescription(of(declarable("api"), ofRootProject())));
		assertEquals("API elements for project ':'.", toDescription(of(consumable("apiElements"), ofRootProject())));
	}

	@Test
	void alwaysCapitalizeJvmWord() {
		assertEquals("JVM implementation dependencies for project ':'.",
			toDescription(of(declarable("jvmImplementation"), ofRootProject())));
		assertEquals("JVM runtime only dependencies for project ':'.",
			toDescription(of(declarable("jvmRuntimeOnly"), ofRootProject())));
	}

	@Test
	void alwaysCapitalizeApiAndJvmWord() {
		assertEquals("JVM API elements for project ':'.",
			toDescription(of(consumable("jvmApiElements"), ofRootProject())));
	}

	@Test
	void doesNotAddTheWordDependenciesToTheConsumableBuckets() {
		assertEquals("Runtime elements for project ':'.", toDescription(of(consumable("runtimeElements"), ofRootProject())));
		assertEquals("Compile elements for project ':'.", toDescription(of(consumable("compileElements"), ofRootProject())));
	}

	@Test
	void doesNotAddTheWordDependenciesToTheResolvableBuckets() {
		assertEquals("Link libraries for project ':'.", toDescription(of(resolvable("linkLibraries"), ofRootProject())));
		assertEquals("Runtime libraries for project ':'.", toDescription(of(resolvable("runtimeLibraries"), ofRootProject())));
		assertEquals("Header search paths for project ':'.", toDescription(of(resolvable("headerSearchPaths"), ofRootProject())));
		assertEquals("Import modules for project ':'.", toDescription(of(resolvable("importModules"), ofRootProject())));
	}

	@Test
	void usesComponentDisplayNameForSingleVariant() {
		val mainOwnerIdentifier = VariantIdentifier.of("", ofMain(ofRootProject()));
		val testOwnerIdentifier = VariantIdentifier.of("", ComponentIdentifier.of("test", ofRootProject()));
		val customDisplayNameOwnerIdentifier = VariantIdentifier.of("", ComponentIdentifier.builder().name(ComponentName.of("test")).withProjectIdentifier(ofRootProject()).displayName("JNI library").build());

		assertEquals("Implementation dependencies for component ':main'.",
			toDescription(of(declarable("implementation"), mainOwnerIdentifier)));
		assertEquals("Implementation dependencies for component ':test'.",
			toDescription(of(declarable("implementation"), testOwnerIdentifier)));
		assertEquals("Implementation dependencies for JNI library ':test'.",
			toDescription(of(declarable("implementation"), customDisplayNameOwnerIdentifier)));
	}
}
