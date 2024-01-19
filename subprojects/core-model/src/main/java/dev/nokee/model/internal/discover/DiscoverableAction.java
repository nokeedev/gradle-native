/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.model.internal.discover;

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.Action;

import javax.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Discover(DiscoverableAction.Strategy.class)
public @interface DiscoverableAction {
	final class Strategy implements Discovery {
		private final Instantiator instantiator;

		@Inject
		public Strategy(Instantiator instantiator) {
			this.instantiator = instantiator;
		}

		@Override
		public <T> List<DisRule> discover(ModelType<T> discoveringType) {
			// Ensure this annotation is applied to an Action class
			assert discoveringType.isSubtypeOf(ModelType.of(new TypeOf<Action<?>>() {}));
			ModelType<?> targetType = ModelType.of(getGenericTypeFromInterface(discoveringType.getConcreteType(), Action.class));
			DisRule rule = new SubTypeOfRule(targetType, instantiator.newInstance(discoveringType.getConcreteType().getAnnotation(DiscoverRule.class).value()));
			return Collections.singletonList(rule);
		}

		private static Type getGenericTypeFromInterface(Class<?> clazz, Class<?> genericInterface) {
			Type[] interfaces = clazz.getGenericInterfaces();

			for (Type type : interfaces) {
				if (type instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) type;
					Type rawType = parameterizedType.getRawType();

					if (genericInterface.equals(rawType)) {
						Type[] typeArguments = parameterizedType.getActualTypeArguments();
						if (typeArguments.length > 0) {
							return typeArguments[0];
						}
					}
				}
			}
			return null;
		}
	}
}
