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

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public final class DependenciesBlock extends AbstractBlock {
	private DependenciesBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<DependenciesBlock> dependencies(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("dependencies"), builder.build());
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder add(String name, String notation) {
			builder.add(ExpressionStatement.of(invoke(name, string(notation))));
			return this;
		}

		public Builder add(String name, DependencyNotation notation) {
			builder.add(ExpressionStatement.of(invoke(name, notation.asExpression())));
			return this;
		}

		public DependenciesBlock build() {
			return new DependenciesBlock(builder.build());
		}
	}
}
