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
package dev.nokee.platform.base.internal.plugins;

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.DiscoverRule;
import dev.nokee.model.internal.discover.DiscoverableAction;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeUtils;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.gradle.api.Action;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static dev.nokee.model.internal.type.ModelType.of;

@SuppressWarnings({"unchecked"})
@DiscoverableAction
@DiscoverRule(RegisterVariants.VariantRule.class)
public final class RegisterVariants<T extends VariantComponentSpec<U>, U extends Variant> implements Action<VariantComponentSpec<? extends Variant>> {
	private final ModelObjectRegistry<Variant> variantRegistry;

	public RegisterVariants(ModelObjectRegistry<Variant> variantRegistry) {
		this.variantRegistry = variantRegistry;
	}

	@Override
	public void execute(VariantComponentSpec<? extends Variant> component) {
		for (BuildVariant it : component.getDimensions().getBuildVariants().get()) {
			final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
			final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(((dev.nokee.model.internal.ModelElement) component).getIdentifier()).build();
			variantRegistry.register(variantIdentifier, variantTypeOf(component));
		}
	}

	@SneakyThrows
	private static <T extends Variant/*Spec*/> Class<T> variantTypeOf(VariantComponentSpec<T> component) {
		return (Class<T>) ((ParameterizedType) TypeToken.of(ModelTypeUtils.toUndecoratedType(component.getClass())).resolveType(VariantComponentSpec.class.getMethod("getVariants").getGenericReturnType()).getType()).getActualTypeArguments()[0];
	}

	@EqualsAndHashCode
	public static final class VariantRule implements DisRule {
		private static final ModelType<VariantComponentSpec<?>> targetType = ModelType.of(new TypeOf<VariantComponentSpec<?>>() {});

		@Override
		public void execute(Details details) {
			ModelType<?> producedType = of(getGenericTypeFromInterface(details.getCandidate().getType().getRawType(), targetType.getRawType()));
			details.newCandidate(ElementName.of("*"), producedType);
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

		@Override
		public String toString() {
			return "produce 1 or more child of generic type T from VariantComponentSpec<T>";
		}
	}
}
