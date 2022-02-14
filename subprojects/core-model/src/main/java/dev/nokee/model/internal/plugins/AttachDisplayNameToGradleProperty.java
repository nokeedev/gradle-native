/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.plugins;

import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.utils.ConfigureUtils;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

final class AttachDisplayNameToGradleProperty extends ModelActionWithInputs.ModelAction2<DisplayNameComponent, GradlePropertyComponent> {
	@Override
	protected void execute(ModelNode entity, DisplayNameComponent displayName, GradlePropertyComponent property) {
		if (property.get() instanceof Property) {
			ConfigureUtils.configureDisplayName((Property<?>) property.get(), displayName.get().toString());
		} else if (property.get() instanceof SetProperty) {
			ConfigureUtils.configureDisplayName((SetProperty<?>) property.get(), displayName.get().toString());
		} else if (property.get() instanceof ListProperty) {
			ConfigureUtils.configureDisplayName((ListProperty<?>) property.get(), displayName.get().toString());
		}
	}
}
