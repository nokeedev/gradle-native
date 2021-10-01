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

package dev.nokee.internal.testing.runnerkit;

import java.util.ArrayList;
import java.util.List;

public final class PluginsSectionBuilder {
	private final List<Section> sections = new ArrayList<>();

	public PluginsSectionBuilder id(String pluginId) {
		sections.add(new PluginSpec(pluginId));
		return this;
	}

	private static final class PluginSpec extends AbstractSection {
		private final String pluginId;

		private PluginSpec(String pluginId) {
			this.pluginId = pluginId;
		}

		@Override
		protected String getGroovy() {
			return "id '" + pluginId + "'";
		}

		@Override
		protected String getKotlin() {
			// TODO: Support raw plugin id -> java
			// TODO: Support backtick plugin id -> `java-library`
			// TODO: Support plugin id with namespace -> org.foo.bar
			return "id(\"" + pluginId + "\")";
		}
	}

	PluginsSection build() {
		return new PluginsSection(new NamedSection("plugins", sections));
	}

	public String toString(GradleDsl dsl) {
		return build().generateSection(dsl);
	}
}
