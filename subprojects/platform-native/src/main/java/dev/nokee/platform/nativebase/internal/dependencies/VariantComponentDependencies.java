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
package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.common.base.Suppliers;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;

import java.util.function.Supplier;

public final class VariantComponentDependencies<T extends NativeComponentDependencies> implements ModelComponent {
	private final Supplier<T> dependencies;
	private final NativeOutgoingDependencies outgoing;

	public VariantComponentDependencies(Supplier<T> dependencies, NativeOutgoingDependencies outgoing) {
		this.dependencies = dependencies;
		this.outgoing = outgoing;
	}

	public T getDependencies() {
		return dependencies.get();
	}

	public NativeOutgoingDependencies getOutgoing() {
		return outgoing;
	}
}
