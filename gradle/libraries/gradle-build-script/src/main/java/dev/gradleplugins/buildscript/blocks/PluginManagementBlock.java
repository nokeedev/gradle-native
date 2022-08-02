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
package dev.gradleplugins.buildscript.blocks;

import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class PluginManagementBlock extends AbstractBlock {
	private PluginManagementBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<PluginManagementBlock> pluginManagement(Consumer<? super PluginManagementBlock.Builder> action) {
		final PluginManagementBlock.Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("pluginManagement"), builder.build());
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder add(Statement statement) {
			builder.add(statement);
			return this;
		}

		public Builder plugins(Consumer<? super PluginsBlock.Builder> action) {
			builder.add(PluginsBlock.plugins(action));
			return this;
		}

		public Builder repositories(Consumer<? super RepositoriesBlock.Builder> action) {
			builder.add(RepositoriesBlock.repositories(action));
			return this;
		}

		public Builder includeBuild(String buildPath) {
			builder.add(SettingsBlock.IncludeBuildStatement.includeBuild(buildPath));
			return this;
		}

		public PluginManagementBlock build() {
			return new PluginManagementBlock(builder.build());
		}
	}
}
