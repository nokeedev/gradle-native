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

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.plugins.ExtensionAware;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MutableModelDecorator implements ModelDecorator {
	private final List<Consumer<? super NestedObjectContext>> nestedObjects = new ArrayList<>();

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
							for (Consumer<? super NestedObjectContext> nestedObject : nestedObjects) {
								nestedObject.accept(contextFor(obj, method));
							}
						}
					}
				}
			}
		});
	}

	private NestedObjectContext contextFor(ModelElement obj, Method method) {
		return new NestedObjectContext() {
			@Override
			public ModelObjectIdentifier getIdentifier() {
				return obj.getIdentifier();
			}

			@Override
			public ModelType<?> getNestedType() {
				return ModelType.of(TypeToken.of(ModelTypeUtils.toUndecoratedType(obj.getClass())).resolveType(method.getGenericReturnType()).getType());
			}

			@Override
			public String getPropertyName() {
				return StringUtils.uncapitalize(method.getName().substring(3));
			}

			@Override
			public void mixIn(Object value) {
				((ExtensionAware) obj).getExtensions().add(getPropertyName(), value);
			}
		};
	}

	public void nestedObject(Consumer<? super NestedObjectContext> action) {
		nestedObjects.add(action);
	}

	public interface NestedObjectContext {
		ModelObjectIdentifier getIdentifier();
		ModelType<?> getNestedType();
		void mixIn(Object value);
		String getPropertyName();
	}
}
