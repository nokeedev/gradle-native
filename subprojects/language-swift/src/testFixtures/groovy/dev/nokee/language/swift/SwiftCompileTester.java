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
package dev.nokee.language.swift;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.base.testers.SourceCompileTester;
import dev.nokee.language.nativebase.HasObjectFilesTester;
import dev.nokee.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

public interface SwiftCompileTester extends SourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	SwiftCompile subject();

	@Test
	default void hasToolChain() {
		assertThat("provide NativeToolChain", subject().getToolChain(), providerOf(isA(NativeToolChain.class)));
	}

	@Test
	default void hasModules() {
		assertThat("not null as per contract", subject().getModules(), notNullValue());
	}

	@Test
	default void hasModuleFile() {
		assertThat("not null as per contract", subject().getModuleFile(), notNullValue());
	}

	@Test
	default void hasModuleName() {
		assertThat("not null as per contract", subject().getModuleName(), notNullValue());
	}
}
