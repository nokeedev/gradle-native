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

import dev.gradleplugins.buildscript.GradleBuildScriptBlockBuilder;
import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.SingleLineCommentBlock;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.concat;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public final class PluginsBlock extends AbstractBlock {
	private PluginsBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<PluginsBlock> plugins(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("plugins"), builder.build());
	}

	public static final class Builder implements GradleBuildScriptBlockBuilder<Builder> {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder id(String pluginId) {
			builder.add(IdStatement.id(pluginId).build());
			return this;
		}

		public Builder id(String pluginId, Consumer<? super IdStatement.Builder> action) {
			final IdStatement.Builder builder = IdStatement.id(pluginId);
			action.accept(builder);
			this.builder.add(builder.build());
			return this;
		}

		public PluginsBlock build() {
			return new PluginsBlock(builder.build());
		}

		@Override
		public Builder comment(String comment, Consumer<? super Builder> action) {
			final Builder builder = new Builder();
			action.accept(builder);
			this.builder.add(SingleLineCommentBlock.comment(comment, builder.build()));
			return this;
		}
	}

	public static final class IdStatement extends AbstractBlock {
		public IdStatement(Statement delegate) {
			super(delegate);
		}

		public static Builder id(String pluginId) {
			return new Builder().id(pluginId);
		}

		public static final class Builder {
			private String pluginId;
			private String version = null;
			private Boolean shouldApply = null;

			private Builder id(String pluginId) {
				this.pluginId = Objects.requireNonNull(pluginId);
				return this;
			}

			public Builder version(String version) {
				this.version = Objects.requireNonNull(version);
				return this;
			}

			public Builder apply(boolean shouldApply) {
				this.shouldApply = shouldApply;
				return this;
			}

			public IdStatement build() {
				final List<Expression> builder = new ArrayList<>();
				builder.add(invoke("id", string(pluginId)));
				if (version != null) {
					builder.add(literal(" "));
					builder.add(invoke("version", string(version)));
				}
				if (shouldApply != null && !shouldApply) {
					builder.add(literal(" "));
					builder.add(invoke("apply", literal("false")));
				}
				return new IdStatement(ExpressionStatement.of(concat(builder)));
			}
		}
	}
}
