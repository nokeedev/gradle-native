/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.model.DependencyFactory;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;

public final class DependencyElement {
	private final Object notation;
	private final Action<? super Dependency> mutator;

	public DependencyElement(Object notation) {
		this.notation = notation;
		this.mutator = ActionUtils.doNothing();
	}

	public DependencyElement(Object notation, Action<? super ExternalModuleDependency> mutator) {
		this.notation = notation;
		this.mutator = dependency -> {
			assert dependency instanceof ExternalModuleDependency;
			mutator.execute((ExternalModuleDependency) dependency);
		};
	}

	public Dependency resolve(DependencyFactory factory) {
		val result = factory.create(notation);
		mutator.execute(result);
		return result;
	}

	@Override
	public String toString() {
		return "dependency '" + notation + "'";
	}
}
