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
package dev.nokee.internal.testing.runnerkit;

import lombok.experimental.Delegate;
import lombok.val;

import java.util.function.Consumer;

public final class DependenciesSection implements Section, CodeSegment {
	@Delegate private final Section delegate;

	public DependenciesSection(Section delegate) {
		this.delegate = delegate;
	}

	public static DependenciesSection dependencies(Consumer<? super DependenciesSectionBuilder> builderAction) {
		val builder = new DependenciesSectionBuilder();
		builderAction.accept(builder);
		return new DependenciesSection(builder.build());
	}

	@Override
	public String toString(GradleDsl dsl) {
		return delegate.generateSection(dsl);
	}

	@Override
	public String toString() {
		return toString(GradleDsl.GROOVY);
	}
}
