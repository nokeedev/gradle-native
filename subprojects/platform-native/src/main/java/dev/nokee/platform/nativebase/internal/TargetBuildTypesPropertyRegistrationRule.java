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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import lombok.val;
import org.gradle.api.Action;

public final class TargetBuildTypesPropertyRegistrationRule implements Action<Component> {
	@Override
	public void execute(Component component) {
		if (component instanceof TargetBuildTypeAwareComponent && component instanceof VariantAwareComponent) {
			final DefaultVariantDimensions dimensions = (DefaultVariantDimensions) ((VariantAwareComponent<?>) component).getDimensions();
			val targetBuildTypes = dimensions.getDimensionFactory().newAxisProperty(BuildType.BUILD_TYPE_COORDINATE_AXIS).elementType(TargetBuildType.class).build();
			dimensions.getElements().add(targetBuildTypes);
			targetBuildTypes.getProperty().value(((TargetBuildTypeAwareComponent) component).getTargetBuildTypes()).disallowChanges();

			((TargetBuildTypeAwareComponent) component).getTargetBuildTypes().finalizeValueOnRead();
		}
	}
}
