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
package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeProjectExtension implements XcodeIdeProjectExtension, IdeProjectExtension<XcodeIdeProject>, HasPublicType {
	private static final XcodeIdeProductTypes PRODUCT_TYPES_FACTORY = new XcodeIdeProductTypes() {};
	@Getter private final NamedDomainObjectContainer<XcodeIdeProject> projects;

	@Inject
	public DefaultXcodeIdeProjectExtension() {
		projects = getObjects().domainObjectContainer(XcodeIdeProject.class, this::newProject);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void projects(Action<? super NamedDomainObjectContainer<XcodeIdeProject>> action) {
		action.execute(projects);
	}

	private XcodeIdeProject newProject(String name) {
		return getObjects().newInstance(DefaultXcodeIdeProject.class, name);
	}

	@Override
	public XcodeIdeProductTypes getProductTypes() {
		return PRODUCT_TYPES_FACTORY;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(XcodeIdeProjectExtension.class);
	}
}
