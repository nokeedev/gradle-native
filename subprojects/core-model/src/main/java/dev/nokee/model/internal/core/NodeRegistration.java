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
package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static dev.nokee.model.internal.core.ModelRegistration.builder;

// The major difference between {@link ModelRegistration} and {@link NodeRegistration} is the fact that ModelRegistration is absolute, e.g. starts from root node where NodeRegistration is relative, e.g. relative to a model node.
@ToString
@EqualsAndHashCode
public final class NodeRegistration {
	private final String name;
	private final List<Object> components = new ArrayList<>();
	private final List<NodeAction> actionRegistrations = new ArrayList<>();

	private NodeRegistration(String name, ModelProjection defaultProjection) {
		this.name = name;
		components.add(defaultProjection);
	}

	ModelRegistration scope(ModelPath path) {
		val builder = builder().withComponent(new ModelPathComponent(path.child(name)));
		components.forEach(builder::withComponent);
		actionRegistrations.stream().map(it -> it.scope(path.child(name))).forEach(builder::action);
		return builder.build();
	}

	public static <T> NodeRegistration of(String name, ModelType<T> type, Object... parameters) {
		return new NodeRegistration(name, ModelProjections.managed(type, parameters));
	}

	public static <T> NodeRegistration unmanaged(String name, ModelType<T> type, Factory<T> factory) {
		return new NodeRegistration(name, ModelProjections.createdUsing(type, factory));
	}

	public NodeRegistration withComponent(Object component) {
		assert !(component instanceof ModelAction) && !(component instanceof NodeAction): "Use action(...) instead.";
		components.add(component);
		return this;
	}

	public NodeRegistration action(NodeAction action) {
		actionRegistrations.add(action);
		return this;
	}
}
