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

import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeWorkspaceExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeWorkspaceExtension extends DefaultXcodeIdeProjectExtension implements XcodeIdeWorkspaceExtension, IdeWorkspaceExtension<XcodeIdeProject>, HasPublicType {
	private final DefaultXcodeIdeWorkspace workspace;

	@Inject
	public DefaultXcodeIdeWorkspaceExtension() {
		this.workspace = getObjects().newInstance(DefaultXcodeIdeWorkspace.class);
	}

	@Override
	public DefaultXcodeIdeWorkspace getWorkspace() {
		return workspace;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(XcodeIdeWorkspaceExtension.class);
	}
}
