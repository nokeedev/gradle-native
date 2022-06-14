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

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public final class TasksBlock extends AbstractBlock {
	private TasksBlock(Statement delegate) {
		super(delegate);
	}

	public static Statement tasks(Consumer<? super TasksBlock.Builder> action) {
		final TasksBlock.Builder builder = new TasksBlock.Builder();
		action.accept(builder);
		return BlockStatement.of(literal("tasks"), builder.build()).useGetter();
	}

	public static final class Builder implements GradleBuildScriptBlockBuilder<TasksBlock.Builder> {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder register(String name) {
			builder.add(ExpressionStatement.of(invoke("register", string(name)).alwaysUseParenthesis()));
			return this;
		}

		public Builder register(String name, Consumer<? super TaskBlock.Builder> action) {
			TaskBlock.Builder taskBuilder = TaskBlock.builder();
			action.accept(taskBuilder);
			builder.add(BlockStatement.of(invoke("register", string(name)).alwaysUseParenthesis(), taskBuilder.build()));
			return this;
		}

		public Builder named(String name, Consumer<? super TaskBlock.Builder> action) {
			TaskBlock.Builder taskBuilder = TaskBlock.builder();
			action.accept(taskBuilder);
			builder.add(BlockStatement.of(invoke("named", string(name)).alwaysUseParenthesis(), taskBuilder.build()));
			return this;
		}

		public TasksBlock build() {
			return new TasksBlock(builder.build());
		}

		@Override
		public Builder comment(String comment, Consumer<? super TasksBlock.Builder> action) {
			final Builder builder = new Builder();
			action.accept(builder);
			this.builder.add(SingleLineCommentBlock.comment(comment, builder.build()));
			return this;
		}
	}
}
