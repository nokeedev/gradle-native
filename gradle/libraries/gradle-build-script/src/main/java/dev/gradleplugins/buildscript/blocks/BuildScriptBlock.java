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
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.SingleLineCommentBlock;
import dev.gradleplugins.buildscript.statements.Statement;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class BuildScriptBlock extends AbstractBlock {
	private BuildScriptBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<BuildScriptBlock> buildscript(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("buildscript"), builder.build());
	}

	public static BlockStatement<BuildScriptBlock> initscript(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("initscript"), builder.build());
	}

	public static Consumer<DependenciesBlock.Builder> classpath(DependencyNotation notation) {
		return builder -> builder.add("classpath", notation);
	}

	public static final class Builder implements GradleBuildScriptBlockBuilder<Builder> {
		private final GroupStatement.Builder builder = GroupStatement.builder();
		private final Set<String> alreadyCreatedConfigurations = new HashSet<>();

		public Builder repositories(Consumer<? super RepositoriesBlock.Builder> action) {
			builder.add(RepositoriesBlock.repositories(action));
			return this;
		}

		public Builder dependencies(Consumer<? super DependenciesBlock.Builder> action) {
			builder.add(DependenciesBlock.dependencies(action));
			return this;
		}

		public Builder configurations(Consumer<? super ConfigurationsBlock.Builder> action) {
			builder.add(ConfigurationsBlock.configurations(alreadyCreatedConfigurations, action));
			return this;
		}

		public BuildScriptBlock build() {
			return new BuildScriptBlock(builder.build());
		}

		@Override
		public Builder comment(String comment, Consumer<? super Builder> action) {
			final Builder builder = new Builder();
			action.accept(builder);
			this.builder.add(SingleLineCommentBlock.comment(comment, builder.build()));
			return this;
		}
	}
}
