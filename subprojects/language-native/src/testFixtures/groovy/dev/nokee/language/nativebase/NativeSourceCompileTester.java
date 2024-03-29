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
package dev.nokee.language.nativebase;

import dev.nokee.internal.testing.NativeServicesInitializedOnWindows;
import dev.nokee.language.base.testers.SourceCompileTester;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

public interface NativeSourceCompileTester extends SourceCompileTester, HasObjectFilesTester {
	NativeSourceCompile subject();

	@Test
	@NativeServicesInitializedOnWindows
	default void hasToolChain() {
		assertThat("provide NativeToolChain", subject().getToolChain(), providerOf(isA(NativeToolChain.class)));
	}

	@Test
	default void hasHeaderSearchPaths() {
		assertThat("not null as per contract", subject().getHeaderSearchPaths(), notNullValue());
		assertThat("provide a value", subject().getHeaderSearchPaths(), presentProvider());
	}
}
