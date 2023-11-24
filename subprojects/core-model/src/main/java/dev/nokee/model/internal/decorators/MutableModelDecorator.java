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
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MutableModelDecorator implements ModelDecorator, DecoratorHandlers {
	private final List<Consumer<? super NestedObjectContext>> nestedObjects = new ArrayList<>();
	private final List<Consumer<? super InjectServiceContext>> injectServices = new ArrayList<>();

	@Override
	public void decorate(ModelMixIn obj) {
		ModelType.typeOf(obj).walkTypeHierarchy(new ModelType.Visitor<ModelMixIn>() {
			private final Set<String> processed = new HashSet<>();
			@Override
			public void visitType(ModelType<? super ModelMixIn> type) {
				for (final Method method : type.getConcreteType().getDeclaredMethods()) {
					// TODO: Raise exception if method annotated with both NestedObject and InjectService
					// TODO: Stop passing along to other consumer when a mixIn invocation was done by the action
					// TODO: Save the action and dehydrated context so speed up the decoration of the same object later

					if (method.isAnnotationPresent(NestedObject.class)) {
						if (processed.add(method.getName())) { // avoid processing method multiple time
							assert method.getParameterCount() == 0;
							assert method.getName().startsWith("get");
							for (Consumer<? super NestedObjectContext> nestedObject : nestedObjects) {
								nestedObject.accept(nestedContextFor(obj, method));
							}
						}
					} else if (method.isAnnotationPresent(InjectService.class)) {
						if (processed.add(method.getName())) { // avoid processing method multiple time
							assert method.getParameterCount() == 0;
							assert method.getName().startsWith("get");
							for (Consumer<? super InjectServiceContext> injectService : injectServices) {
								injectService.accept(serviceContextFor(obj, method));
							}
						}
					}
				}
			}
		});
	}

	private NestedObjectContext nestedContextFor(ModelMixIn obj, Method method) {
		return new NestedObjectContext() {
			@Override
			public ModelObjectIdentifier getIdentifier() {
				return obj.getIdentifier();
			}

			@Override
			public ModelType<?> getNestedType() {
				try {
					Method m = method.getDeclaringClass().getMethod(method.getName());
					return ModelType.of(TypeToken.of(ModelTypeUtils.toUndecoratedType(obj.getClass())).resolveType(m.getGenericReturnType()).getType());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}

			public NestedObject getAnnotation() {
				return method.getAnnotation(NestedObject.class);
			}

			@Override
			public String getPropertyName() {
				return StringUtils.uncapitalize(method.getName().substring(3));
			}

			@Override
			public void mixIn(Object value) {
				obj.getExtensions().add(getPropertyName(), value);
			}
		};
	}

	private InjectServiceContext serviceContextFor(ModelMixIn obj, Method method) {
		return new InjectServiceContext() {
			@Override
			public ModelType<?> getServiceType() {
				try {
					Method m = method.getDeclaringClass().getMethod(method.getName());
					return ModelType.of(TypeToken.of(ModelTypeUtils.toUndecoratedType(obj.getClass())).resolveType(m.getGenericReturnType()).getType());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}

			public String getPropertyName() {
				return StringUtils.uncapitalize(method.getName().substring(3));
			}

			@Override
			public void mixIn(Object value) {
				obj.getExtensions().add(getPropertyName(), value);
			}
		};
	}

	@Override
	public void nestedObject(Consumer<? super NestedObjectContext> action) {
		nestedObjects.add(action);
	}

	@Override
	public void injectService(Consumer<? super InjectServiceContext> action) {
		injectServices.add(action);
	}
}
