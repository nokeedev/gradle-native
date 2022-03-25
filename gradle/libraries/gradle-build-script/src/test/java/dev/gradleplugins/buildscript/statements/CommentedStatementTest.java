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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentedStatementTest {
	private final Statement target = Mockito.mock(Statement.class);
	private final CommentedStatement<Statement> subject = CommentedStatement.commented(target);

	@Test
	void returnsCommentedStatement() {
		assertSame(target, subject.getContent());
	}

	@Test
	void visitsContent() {
		final Visitor visitor = Mockito.mock(Visitor.class);
		subject.accept(visitor);
		Mockito.verify(visitor).visit(target);
	}

	@Test
	void throwsExceptionIfVisitorIsNull() {
		assertThrows(NullPointerException.class, () -> subject.accept(null));
	}
}
