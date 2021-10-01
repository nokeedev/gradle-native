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

import org.gradle.api.Plugin;

public class ApplySection extends AbstractSection {
	private final ApplySectionNotation notation;

	private ApplySection(ApplySectionNotation notation) {
		this.notation = notation;
	}

	public static ApplySection apply(ApplySectionNotation notation) {
		return new ApplySection(notation);
	}

	public interface ApplySectionNotation extends CodeSegment {
		static ApplySectionNotation plugin(String pluginId) {
			return new PluginIdApplyNotation(pluginId);
		}

		static ApplySectionNotation plugin(Class<? extends Plugin<?>> pluginType) {
			return new PluginTypeApplyNotation(pluginType);
		}

		static ApplySectionNotation from(String path) {
			return new FromApplyNotation(path);
		}
	}

	private static final class PluginIdApplyNotation extends AbstractSection implements ApplySectionNotation {
		private final String pluginId;

		private PluginIdApplyNotation(String pluginId) {
			this.pluginId = pluginId;
		}

		@Override
		protected String getGroovy() {
			return "plugin: '" + pluginId + "'";
		}

		@Override
		protected String getKotlin() {
			return "plugin = \"" + pluginId + "\"";
		}

		@Override
		public String toString(GradleDsl dsl) {
			return generateSection(dsl);
		}

		@Override
		public String toString() {
			return toString(GradleDsl.GROOVY);
		}
	}

	private static final class PluginTypeApplyNotation extends AbstractSection implements ApplySectionNotation {
		private final Class<? extends Plugin<?>> pluginType;

		private PluginTypeApplyNotation(Class<? extends Plugin<?>> pluginType) {
			this.pluginType = pluginType;
		}

		@Override
		protected String getGroovy() {
			return "plugin: Class.forName('" + pluginType.getTypeName() + "')";
		}

		@Override
		protected String getKotlin() {
			return "plugin = Class.forName(\"" + pluginType.getTypeName() + "\")";
		}

		@Override
		public String toString(GradleDsl dsl) {
			return generateSection(dsl);
		}

		@Override
		public String toString() {
			return toString(GradleDsl.GROOVY);
		}
	}

	private static final class FromApplyNotation extends AbstractSection implements ApplySectionNotation {
		private final String path;

		private FromApplyNotation(String path) {
			this.path = path;
		}

		@Override
		protected String getGroovy() {
			return "from: '" + GradleDsl.GROOVY.fileName(path) + "'";
		}

		@Override
		protected String getKotlin() {
			return "from = \"" + GradleDsl.KOTLIN.fileName(path) + "\"";
		}

		@Override
		public String toString(GradleDsl dsl) {
			return generateSection(dsl);
		}

		@Override
		public String toString() {
			return toString(GradleDsl.GROOVY);
		}
	}

	@Override
	protected String getGroovy() {
		return "apply " + ((Section) notation).generateSection(GradleDsl.GROOVY);
	}

	@Override
	protected String getKotlin() {
		return "apply(" + ((Section) notation).generateSection(GradleDsl.KOTLIN) + ")";
	}
}
