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
package dev.nokee.model.internal;

import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.GradlePropertyTypes;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.provider.HasConfigurableValue;

public final class ModelBackedGradlePropertyConvertibleStrategy implements PropertyConvertibleStrategy {
	private final ModelNode entity;

	public ModelBackedGradlePropertyConvertibleStrategy(ModelNode entity) {
		this.entity = entity;
	}

	@Override
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public <P extends HasConfigurableValue> P asProperty(ModelType<P> propertyType) {
		val result = entity.getComponent(GradlePropertyComponent.class).get();
		if (GradlePropertyTypes.of(result).isSubtypeOf(propertyType)) {
			return (P) result;
		} else {
			throw new RuntimeException();
		}
	}
}
