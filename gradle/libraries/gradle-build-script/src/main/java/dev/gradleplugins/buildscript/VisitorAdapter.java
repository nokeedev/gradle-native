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
package dev.gradleplugins.buildscript;

import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.NestedStatement;
import dev.gradleplugins.buildscript.statements.SingleLineCommentBlock;
import dev.gradleplugins.buildscript.statements.Statement;

public abstract class VisitorAdapter implements Visitor {
	@Override
	public final void visit(Statement statement) {
		if (statement instanceof NestedStatement) {
			visit((NestedStatement) statement);
		} else if (statement instanceof GroupStatement) {
			visit((GroupStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			visit((ExpressionStatement) statement);
		} else if (statement instanceof BlockStatement) {
			visit((BlockStatement<?>) statement);
		} else if (statement instanceof SingleLineCommentBlock) {
			visit((SingleLineCommentBlock<?>) statement);
		} else {
			throw new UnsupportedOperationException("Unknown statement type");
		}
	}

	// nested
	public void visit(NestedStatement statement) {}

	// group
	public void visit(GroupStatement statement) {}

	// statement (expression)
	public void visit(ExpressionStatement statement) {}

	// block
	public void visit(BlockStatement<?> statement) {}

	// comment on group
	public void visit(SingleLineCommentBlock<?> statement) {}
}
