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

import dev.gradleplugins.buildscript.TestStatement;
import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.syntax.Expression;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.statements.BlockStatement.of;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BlockStatementTest {
	@Test
	void isEmptyWhenBlockContentIsEmpty() {
		assertTrue(of(literal("kole"), TestStatement.empty).isEmpty());
	}

	@Test
	void isNotEmptyWhenBlockContentIsNotEmpty() {
		assertFalse(of(literal("pwel"), TestStatement.nonEmpty).isEmpty());
	}

	@Test
	void isNotEmptyWhenBlockContentIsNotEmptyAware() {
		assertFalse(of(literal("aqed"), mock(Statement.class)).isEmpty());
	}

	@Test
	void returnsSelectorExpressionWhenSelectorIsExpression() {
		final Expression selector = literal("yout");
		assertSame(selector, of(selector, mock(Statement.class)).getSelector());
	}

	@Test
	void returnsContentStatement() {
		final Statement content =  mock(Statement.class);
		assertSame(content, of(literal("koll"), content).getContent());
	}

	@Test
	void visitsStatement() {
		final Statement content =  mock(Statement.class);
		final Statement subject = of(literal("lope"), content);
		final Visitor visitor = mock(Visitor.class);
		subject.accept(visitor);
		verify(visitor).visit(subject);
	}
}
