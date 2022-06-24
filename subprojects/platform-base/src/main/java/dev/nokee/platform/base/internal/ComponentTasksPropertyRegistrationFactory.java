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

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import org.gradle.api.Task;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentTasksPropertyRegistrationFactory {
	private final ComponentElementsPropertyRegistrationFactory factory = new ComponentElementsPropertyRegistrationFactory();

	public ModelRegistration create(String elementName, ModelNode owner, Class<? extends Task> elementType) {
		return ModelRegistration.builder()
			.withComponent(new ElementNameComponent(elementName))
			.withComponent(new ParentComponent(owner))
			.mergeFrom(factory.newProperty().baseRef(owner).elementType(of(elementType)).build())
			.withComponent(createdUsing(of(TaskView.class), () -> new TaskViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), ModelType.of(new TypeOf<ViewAdapter<Task>>() {})))))
			.build();
	}
}
