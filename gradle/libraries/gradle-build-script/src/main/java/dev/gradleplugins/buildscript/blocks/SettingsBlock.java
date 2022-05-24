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

import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;
import dev.gradleplugins.buildscript.syntax.PropertyExpression;
import dev.gradleplugins.buildscript.syntax.Syntax;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.property;

public final class SettingsBlock extends AbstractBlock {
	private SettingsBlock(Statement delegate) {
		super(delegate);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder add(Statement statement) {
			builder.add(statement);
			return this;
		}

		public Builder buildscript(Consumer<? super BuildScriptBlock.Builder> action) {
			builder.add(BuildScriptBlock.buildscript(action));
			return this;
		}

		public Builder plugins(Consumer<? super PluginsBlock.Builder> action) {
			builder.add(PluginsBlock.plugins(action));
			return this;
		}

		public Builder apply(ApplyStatement.Notation notation) {
			builder.add(ApplyStatement.apply(notation));
			return this;
		}

		public Builder pluginManagement(Consumer<? super PluginManagementBlock.Builder> action) {
			builder.add(PluginManagementBlock.pluginManagement(action));
			return this;
		}

		public Builder include(String firstProjectPaths, String... otherProjectPaths) {
			builder.add(IncludeStatement.include(firstProjectPaths, otherProjectPaths));
			return this;
		}

		public Builder rootProject(Function<? super PropertyExpression, ? extends Expression> mapper) {
			builder.add(ExpressionStatement.of(mapper.apply(property(literal("rootProject")))));
			return this;
		}

		public SettingsBlock build() {
			return new SettingsBlock(builder.build());
		}
	}

	public static final class IncludeStatement {
		public static Statement include(String firstProjectPath, String... otherProjectPaths) {
			return ExpressionStatement.of(invoke("include", Stream.concat(Stream.of(firstProjectPath), Stream.of(otherProjectPaths)).map(Syntax::string).toArray(Expression[]::new)));
		}
	}
}
