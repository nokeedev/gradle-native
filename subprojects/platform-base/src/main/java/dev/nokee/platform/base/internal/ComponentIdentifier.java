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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.EqualsAndHashCode;
import org.gradle.util.Path;

import java.util.Iterator;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class ComponentIdentifier implements DomainObjectIdentifier {
	private static final ComponentName MAIN_COMPONENT_NAME = ComponentName.of("main");
	private static final String DEFAULT_DISPLAY_NAME = "component";
	private final ComponentName name;
	private final String displayName;
	private final ProjectIdentifier projectIdentifier;

	private ComponentIdentifier(ComponentName name, String displayName, ProjectIdentifier projectIdentifier) {
		this.name = requireNonNull(name);
		this.displayName = requireNonNull(displayName);
		this.projectIdentifier = requireNonNull(projectIdentifier);
	}

	public static ComponentIdentifier ofMain(ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(MAIN_COMPONENT_NAME, DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static ComponentIdentifier of(String name, ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(ComponentName.of(name), DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static ComponentIdentifier of(ComponentName name, ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(name, DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public ComponentName getName() {
		return name;
	}

	@Deprecated
	public Path getPath() {
		return getProjectIdentifier().getPath().child(name.get());
	}

	@Deprecated
	public Optional<ProjectIdentifier> getParentIdentifier() {
		return Optional.of(projectIdentifier);
	}

	@Deprecated
	public ProjectIdentifier getProjectIdentifier() {
		return projectIdentifier;
	}

	@Deprecated
	public String getDisplayName() {
		return displayName;
	}

	public boolean isMainComponent() {
		return name.equals(MAIN_COMPONENT_NAME);
	}

	@Override
	public Iterator<Object> iterator() {
		return Iterators.forArray(projectIdentifier, this);
	}

	@Override
	public String toString() {
		return displayName + " '" + getPath() + "'";
	}

	public static Builder builder() {
		return new Builder();
	}

    public static final class Builder {
		private ComponentName name;
		private String displayName = DEFAULT_DISPLAY_NAME;
		private ProjectIdentifier projectIdentifier;

		public Builder name(ComponentName name) {
			this.name = requireNonNull(name);
			return this;
		}

		public Builder displayName(String displayName) {
			this.displayName = requireNonNull(displayName);
			return this;
		}

		public Builder withProjectIdentifier(ProjectIdentifier projectIdentifier) {
			this.projectIdentifier = requireNonNull(projectIdentifier);
			return this;
		}

		public ComponentIdentifier build() {
			return new ComponentIdentifier(name, displayName, projectIdentifier);
		}
	}
}
