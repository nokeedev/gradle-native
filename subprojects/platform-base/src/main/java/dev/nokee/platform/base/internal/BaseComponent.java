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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.Set;

public abstract class BaseComponent<T extends Variant> extends ModelElementSupport implements Component {
	public abstract Property<String> getBaseName();

	public abstract Provider<? extends T> getDevelopmentVariant();

	public abstract VariantView<? extends T> getVariants();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract Provider<Set<BuildVariant>> getBuildVariants();
}
