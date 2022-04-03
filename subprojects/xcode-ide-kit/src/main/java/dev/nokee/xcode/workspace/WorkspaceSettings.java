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
package dev.nokee.xcode.workspace;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@EqualsAndHashCode
public final class WorkspaceSettings implements Iterable<WorkspaceSettings.Option<?>> {
	private final ImmutableMap<Class<? extends Option<?>>, Option<?>> properties;

	private WorkspaceSettings(ImmutableMap<Class<? extends Option<?>>, Option<?>> properties) {
		this.properties = properties;
	}

	public <T> Optional<T> get(Class<? extends Option<T>> key) {
		Objects.requireNonNull(key);
		@SuppressWarnings("unchecked")
		Option<T> result = (Option<T>) properties.get(key);
		return Optional.ofNullable(result).map(Option::get);
	}

	@Override
	public Iterator<Option<?>> iterator() {
		return properties.values().iterator();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<Class<? extends Option<?>>, Option<?>> properties = new LinkedHashMap<>();

		@SuppressWarnings("unchecked")
		public Builder put(Option<?> property) {
			properties.put((Class<? extends Option<?>>) property.getClass(), property);
			return this;
		}

		public WorkspaceSettings build() {
			return new WorkspaceSettings(ImmutableMap.copyOf(properties));
		}
	}

	public interface Option<T> {
		default String getName() {
			return getClass().getSimpleName();
		};

		T get();
	}

	public enum BuildLocationStyle implements Option<String> {
		UseAppPreferences;

		@Override
		public String get() {
			return name();
		}
	}

	public enum CustomBuildLocationType implements Option<String> {
		RelativeToDerivedData;

		@Override
		public String get() {
			return name();
		}
	}

	public static final class DerivedDataCustomLocation implements Option<String> {
		private final String value;

		public DerivedDataCustomLocation(String value) {
			this.value = value;
		}

		@Override
		public String get() {
			return value;
		}
	}

	public enum DerivedDataLocationStyle implements Option<String> {
		WorkspaceRelativePath;

		@Override
		public String get() {
			return name();
		}
	}

	public enum IssueFilterStyle implements Option<String> {
		ShowActiveSchemeOnly;

		@Override
		public String get() {
			return name();
		}
	}

	public enum LiveSourceIssues implements Option<Boolean> {
		Enabled, Disabled;

		@Override
		public String getName() {
			return "LiveSourceIssuesEnabled";
		}

		@Override
		public Boolean get() {
			return this == Enabled;
		}
	}

	public enum ShowSharedSchemesAutomatically implements Option<Boolean> {
		Enabled, Disabled;

		@Override
		public String getName() {
			return "ShowSharedSchemesAutomaticallyEnabled";
		}

		@Override
		public Boolean get() {
			return this == Enabled;
		}
	}

	public enum AutoCreateSchemes implements Option<Boolean> {
		Enabled, Disabled;

		@Override
		public String getName() {
			return "IDEWorkspaceSharedSettings_AutocreateContextsIfNeeded";
		}

		@Override
		public Boolean get() {
			return this == Enabled;
		}
	}
}
