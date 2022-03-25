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
package dev.gradleplugins.buildscript.statements;

import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.syntax.Expression;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class SingleLineCommentBlock<T extends Statement> implements Statement {
	private final String comment;
	private final T block;

	private SingleLineCommentBlock(String comment, T block) {
		this.comment = comment;
		this.block = block;
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	public static <T extends Statement> SingleLineCommentBlock<T> comment(String comment, T block) {
		return new SingleLineCommentBlock<>(comment, block);
	}

	public Expression getComment() {
		return literal(comment);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(block);
	}
}
