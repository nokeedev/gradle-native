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
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.syntax.Syntax.gradle;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.property;

public final class ConfigurationsBlock extends AbstractBlock {
	private ConfigurationsBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<ConfigurationsBlock> configurations(Consumer<? super Builder> action) {
		final Builder builder = new Builder(new HashSet<>());
		action.accept(builder);
		return BlockStatement.of(literal("configurations"), builder.build());
	}

	static BlockStatement<ConfigurationsBlock> configurations(Set<String> alreadyCreated, Consumer<? super Builder> action) {
		final Builder builder = new Builder(alreadyCreated);
		action.accept(builder);
		return BlockStatement.of(literal("configurations"), builder.build());
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();
		private final Set<String> alreadyCreated;

		Builder(Set<String> alreadyCreated) {
			this.alreadyCreated = alreadyCreated;
		}

		public Builder withConfiguration(String name, Consumer<? super ConfigurationStatement.Builder> action) {
			final ConfigurationStatement.Builder builder = new ConfigurationStatement.Builder();
			action.accept(builder);
			if (alreadyCreated.add(name)) {
				this.builder.add(BlockStatement.of(gradle(name, "create(\"" + name + "\")"), builder.build()));
			} else {
				this.builder.add(BlockStatement.of(literal(name), builder.build()));
			}
			return this;
		}

		public ConfigurationsBlock build() {
			return new ConfigurationsBlock(builder.build());
		}
	}

	public static final class ConfigurationStatement extends AbstractBlock implements Statement {
		private ConfigurationStatement(GroupStatement delegate) {
			super(delegate);
		}

		public static final class Builder {
			private final GroupStatement.Builder builder = GroupStatement.builder();

			public Builder extendsFrom(String firstSuperConfig, String... otherSuperConfigs) {
				Expression[] args = Stream.concat(Stream.of(firstSuperConfig), Stream.of(otherSuperConfigs)).map(it -> gradle(it, "configurations.getByName(\"" + it + "\")")).toArray(Expression[]::new);
				builder.add(ExpressionStatement.of(invoke("extendsFrom", args)));
				return this;
			}

			public Builder canBeConsumed(boolean value) {
				builder.add(ExpressionStatement.of(property(literal("canBeConsumed")).ofBooleanType().assign(literal(value))));
				return this;
			}

			public Builder canBeResolved(boolean value) {
				builder.add(ExpressionStatement.of(property(literal("canBeResolved")).ofBooleanType().assign(literal(value))));
				return this;
			}

			public ConfigurationStatement build() {
				return new ConfigurationStatement(builder.build());
			}
		}
	}
}
