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

import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.EmptyAwareSection;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;
import lombok.val;

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class TaskBlock extends AbstractBlock {

	private TaskBlock(Statement delegate) {
		super(delegate);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder doLast() {
			builder.add(BlockStatement.of(literal("doLast"), new EmptyStatement()));
			return this;
		}

		public Builder doLast(Consumer<? super GroupStatement.Builder> builderConsumer) {
			final GroupStatement.Builder builder = GroupStatement.builder();
			builderConsumer.accept(builder);
			this.builder.add(BlockStatement.of(literal("doLast"), builder.build()));
			return this;
		}

		public TaskBlock build() {
			return new TaskBlock(builder.build());
		}
	}

	private static final class EmptyStatement implements Statement, EmptyAwareSection {
		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Statement prefixWith(Expression prefix) {
			return this;
		}

		@Override
		public void accept(Visitor visitor) {
			// do nothing
		}
	}
}
