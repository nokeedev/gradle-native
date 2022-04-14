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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;

public final class AttachAttributesToConfigurationRule<T extends LinkedEntity> extends ModelActionWithInputs.ModelAction2<T, BuildVariantComponent> {
	private final ModelRegistry registry;
	private final ObjectFactory objects;

	public AttachAttributesToConfigurationRule(Class<T> configurationType, ModelRegistry registry, ObjectFactory objects) {
		super(ModelComponentReference.of(configurationType), ModelComponentReference.of(BuildVariantComponent.class));
		this.registry = registry;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, T configuration, BuildVariantComponent buildVariant) {
		registry.instantiate(ModelAction.configure(configuration.get().getId(), Configuration.class, ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) buildVariant.get(), objects)));
		registry.instantiate(ModelAction.configure(configuration.get().getId(), Configuration.class, ConfigurationUtilsEx::configureAsGradleDebugCompatible));
	}
}
