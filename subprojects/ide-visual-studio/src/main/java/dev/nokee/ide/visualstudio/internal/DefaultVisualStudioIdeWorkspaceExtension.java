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

import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeWorkspaceExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeWorkspaceExtension extends DefaultVisualStudioIdeProjectExtension implements VisualStudioIdeWorkspaceExtension, IdeWorkspaceExtension<VisualStudioIdeProject>, HasPublicType {
	private final DefaultVisualStudioIdeSolution solution;

	@Inject
	public DefaultVisualStudioIdeWorkspaceExtension() {
		this.solution = getObjects().newInstance(DefaultVisualStudioIdeSolution.class);
	}

	@Override
	public DefaultVisualStudioIdeSolution getSolution() {
		return solution;
	}

	@Override
	public DefaultVisualStudioIdeSolution getWorkspace() {
		return solution;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(VisualStudioIdeWorkspaceExtension.class);
	}
}
