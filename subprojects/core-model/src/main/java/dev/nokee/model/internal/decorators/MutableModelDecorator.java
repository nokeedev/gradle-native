/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal.decorators;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.type.ModelType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class MutableModelDecorator implements ModelDecorator {
	private final List<BiConsumer<? super ModelElement, ? super Method>> nestedObjects = new ArrayList<>();

	@Override
	public void decorate(ModelElement obj) {
		ModelType.typeOf(obj).walkTypeHierarchy(new ModelType.Visitor<ModelElement>() {
			private final Set<String> processed = new HashSet<>();
			@Override
			public void visitType(ModelType<? super ModelElement> type) {
				for (final Method method : type.getConcreteType().getDeclaredMethods()) {
					if (method.isAnnotationPresent(NestedObject.class)) {
						if (processed.add(method.getName())) { // avoid processing method multiple time
							assert method.getParameterCount() == 0;
							assert method.getName().startsWith("get");
							for (BiConsumer<? super ModelElement, ? super Method> nestedObject : nestedObjects) {
								nestedObject.accept(obj, method);
							}
						}
					}
				}
			}
		});
	}

	public void nestedObject(BiConsumer<? super ModelElement, ? super Method> action) {
		nestedObjects.add(action);
	}
}
