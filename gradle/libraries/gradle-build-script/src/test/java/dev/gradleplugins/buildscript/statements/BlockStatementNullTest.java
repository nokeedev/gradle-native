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

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.statements.BlockStatement.of;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class BlockStatementNullTest {
	@Test
	void throwsExceptionIfSelectorExpressionIsNull() {
		assertThrows(NullPointerException.class, () -> of(null, mock(Statement.class)));
	}

	@Test
	void throwsExceptionIfContentIsNull() {
		assertThrows(NullPointerException.class, () -> of(literal("loop"), null));
	}

	@Test
	void throwsExceptionIfVisitorIsNull() {
		assertThrows(NullPointerException.class, () -> of(literal("poor"), mock(Statement.class)).accept(null));
	}
}
