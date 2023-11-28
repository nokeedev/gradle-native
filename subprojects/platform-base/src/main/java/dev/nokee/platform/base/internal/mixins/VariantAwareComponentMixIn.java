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
package dev.nokee.platform.base.internal.mixins;

import dev.nokee.model.internal.decorators.Decorate;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.NestedViewDecorator;
import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

public interface VariantAwareComponentMixIn<T extends Variant> extends VariantAwareComponent<T>, VariantAwareComponentInternal<T> {
	@Override
	@Decorate(NestedViewDecorator.class)
	VariantView<T> getVariants();

	@Override
	default void variants(Action<? super VariantView<T>> action) {
		action.execute(getVariants());
	}

	@Override
	default void variants(@SuppressWarnings("rawtypes") Closure closure) {
		variants(new ClosureWrappedConfigureAction<>(closure));
	}

	@Override
	@NestedObject
	DefaultVariantDimensions getDimensions();

	@Override
	default Provider<Set<BuildVariant>> getBuildVariants() {
		return getDimensions().getBuildVariants();
	}
}
