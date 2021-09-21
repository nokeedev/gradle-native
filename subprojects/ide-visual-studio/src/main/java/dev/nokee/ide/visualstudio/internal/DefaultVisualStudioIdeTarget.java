/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class DefaultVisualStudioIdeTarget implements VisualStudioIdeTarget, Named, TaskDependencyContainer {
	private final ObjectFactory objectFactory;
	@Getter private final VisualStudioIdeProjectConfiguration projectConfiguration;
	@Getter private final DefaultVisualStudioIdePropertyGroup properties;
	@Getter private final NamedDomainObjectContainer<VisualStudioIdePropertyGroup> itemProperties;
	@Getter private final RegularFileProperty productLocation;

	public DefaultVisualStudioIdeTarget(VisualStudioIdeProjectConfiguration projectConfiguration, ObjectFactory objectFactory) {
		this.projectConfiguration = projectConfiguration;
		this.objectFactory = objectFactory;
		this.productLocation = configureDisplayName(objectFactory.fileProperty(), "productLocation");
		this.properties = objectFactory.newInstance(DefaultVisualStudioIdePropertyGroup.class);
		this.itemProperties = objectFactory.domainObjectContainer(VisualStudioIdePropertyGroup.class, this::newPropertyGroup);
	}

	public void setProductLocation(Object value) {
		ConfigureUtils.setPropertyValue(productLocation, value);
	}

	@Override
	public String getName() {
		return VisualStudioIdeUtils.asName(projectConfiguration);
	}

	private VisualStudioIdePropertyGroup newPropertyGroup(String name) {
		return objectFactory.newInstance(NamedVisualStudioIdePropertyGroup.class, name);
	}

	@Override
	public void visitDependencies(TaskDependencyResolveContext context) {
		properties.visitDependencies(context);
		itemProperties.stream().map(DefaultVisualStudioIdePropertyGroup.class::cast).forEach(it -> it.visitDependencies(context));
	}
}
