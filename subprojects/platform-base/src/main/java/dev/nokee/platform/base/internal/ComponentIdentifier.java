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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class ComponentIdentifier implements DomainObjectIdentifier, HasName {
	private static final String DEFAULT_DISPLAY_NAME = "component";
	private final ComponentIdentity identity;
	private final String displayName;
	private final ProjectIdentifier projectIdentifier;

	private ComponentIdentifier(ComponentIdentity identity, String displayName, ProjectIdentifier projectIdentifier) {
		this.identity = requireNonNull(identity);
		this.displayName = requireNonNull(displayName);
		this.projectIdentifier = requireNonNull(projectIdentifier);
	}

	public static ComponentIdentifier ofMain(ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(ComponentIdentity.ofMain(), DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static ComponentIdentifier of(String name, ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(ComponentIdentity.of(name), DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static ComponentIdentifier of(ComponentName name, ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(ComponentIdentity.of(name), DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	@Override
	public ComponentName getName() {
		return identity.getName();
	}

	// FIXME: Remove this API
	public ProjectIdentifier getProjectIdentifier() {
		return projectIdentifier;
	}

	// FIXME: Remove this API
	public String getDisplayName() {
		return displayName;
	}

	public boolean isMainComponent() {
		return identity.isMainComponent();
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(projectIdentifier).add(this).build().iterator();
	}

	@Override
	public String toString() {
		return displayName + " '" + toGradlePath(this) + "'";
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
			return new ComponentIdentifier(ComponentIdentity.of(name), displayName, projectIdentifier);
		}
	}
}
