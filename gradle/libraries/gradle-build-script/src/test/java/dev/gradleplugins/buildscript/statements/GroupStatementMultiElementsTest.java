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
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GroupStatementMultiElementsTest {
	private final Statement e0 = Mockito.mock(Statement.class);
	private final Statement e1 = Mockito.mock(Statement.class);
	private final Statement e2 = Mockito.mock(Statement.class);
	private final GroupStatement subject = GroupStatement.of(e0, e1, e2);

	@Test
	void isNotEmpty() {
		assertFalse(subject.isEmpty());
	}

	@Test
	void isIterableWithMultipleElements() {
		assertThat(subject, contains(e0, e1, e2));
	}

	@Test
	void visitsAllElements() {
		final Visitor visitor = Mockito.mock(Visitor.class);
		subject.accept(visitor);
		final InOrder inOrder = Mockito.inOrder(visitor);
		inOrder.verify(visitor).visit(e0);
		inOrder.verify(visitor).visit(e1);
		inOrder.verify(visitor).visit(e2);
	}

	@Test
	void throwsExceptionIfVisitorIsNull() {
		assertThrows(NullPointerException.class, () -> subject.accept(null));
	}

	@Test
	void canBuildMultiElementGroupUsingBuilder() {
		assertEquals(subject, GroupStatement.builder().add(e0).add(e1).add(e2).build());
	}
}
