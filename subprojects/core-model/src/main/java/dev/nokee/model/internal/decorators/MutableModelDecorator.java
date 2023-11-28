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

import dev.nokee.internal.reflect.DefaultInstantiator;
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.type.ModelType;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MutableModelDecorator implements ModelDecorator {
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
							if (method.getName().equals("getDependencies")) {
								obj.getExtensions().add(propertyNameOf(method), DefaultInstantiator.getNext().init(propertyNameOf(method)));
							}
						}
					}
				}
			}
		});
	}

	private static String propertyNameOf(Method method) {
		return StringUtils.uncapitalize(method.getName().substring(3));
	}
}
