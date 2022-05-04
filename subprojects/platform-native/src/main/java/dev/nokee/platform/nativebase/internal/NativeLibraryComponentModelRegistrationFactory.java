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

import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsComponent;
import lombok.val;
import org.gradle.api.Project;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeLibraryProjection;

public final class NativeLibraryComponentModelRegistrationFactory {
	private final Class<Component> implementationComponentType;
	private final Project project;

	@SuppressWarnings("unchecked")
	public <T extends Component> NativeLibraryComponentModelRegistrationFactory(Class<T> implementationComponentType, Project project) {
		this.implementationComponentType = (Class<Component>) implementationComponentType;
		this.project = project;
	}

	public ModelRegistration.Builder create(ComponentIdentifier identifier) {
		val builder = ModelRegistration.builder()
			.withComponent(createdUsing(of(implementationComponentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsComponent.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(NativeLibraryTag.tag())
			.withComponent(createdUsing(of(DefaultNativeLibraryComponent.class), nativeLibraryProjection(identifier.getName().toString(), project)))
			;

		if (identifier.isMainComponent()) {
			builder.withComponent(ExcludeFromQualifyingNameTag.tag());
		}

		return builder;
	}
}
