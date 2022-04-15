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
package dev.nokee.platform.base.internal;

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.*;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public interface ModelBackedVariantAwareComponentMixIn<T extends Variant> extends VariantAwareComponent<T> {
	@Override
	@SuppressWarnings("unchecked")
	default VariantView<T> getVariants() {
		return ModelProperties.getProperty(this, "variants").as((Class<VariantView<T>>)new TypeToken<VariantView<T>>(getClass()) {}.getRawType()).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	default void variants(Action<? super VariantView<T>> action) {
		ModelProperties.getProperty(this, "variants").as((Class<VariantView<T>>)new TypeToken<VariantView<T>>(getClass()) {}.getRawType()).configure(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void variants(@SuppressWarnings("rawtypes") Closure closure) {
		ModelProperties.getProperty(this, "variants").as((Class<VariantView<T>>)new TypeToken<VariantView<T>>(getClass()) {}.getRawType()).configure(closure);
	}

	@Override
	default VariantDimensions getDimensions() {
		return ModelNodes.of(this).get(ModelBackedVariantDimensions.class);
	}

	@Override
	default Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}
}
