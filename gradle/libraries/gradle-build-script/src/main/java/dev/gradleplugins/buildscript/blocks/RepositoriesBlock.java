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

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class RepositoriesBlock extends AbstractBlock {
	private RepositoriesBlock(Statement delegate) {
		super(delegate);
	}

	public static Statement repositories(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("repositories"), builder.build());
	}

	public static final class Builder implements GradleBuildScriptBlockBuilder<Builder> {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder add(Statement statement) {
			builder.add(statement);
			return this;
		}

		public Builder mavenLocal() {
			builder.add(ArtifactRepositoryStatement.mavenLocal());
			return this;
		}

		public Builder mavenCentral() {
			builder.add(ArtifactRepositoryStatement.mavenCentral());
			return this;
		}

		public Builder gradlePluginPortal() {
			builder.add(ArtifactRepositoryStatement.gradlePluginPortal());
			return this;
		}

		public Builder maven(String uri) {
			builder.add(ArtifactRepositoryStatement.maven(ArtifactRepositoryStatement.uri(uri)));
			return this;
		}

		public Builder maven(Consumer<? super ArtifactRepositoryStatement.Builder> action) {
			final ArtifactRepositoryStatement.Builder builder = new ArtifactRepositoryStatement.Builder();
			action.accept(builder);
			this.builder.add(builder.build());
			return this;
		}

		public RepositoriesBlock build() {
			return new RepositoriesBlock(builder.build());
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
