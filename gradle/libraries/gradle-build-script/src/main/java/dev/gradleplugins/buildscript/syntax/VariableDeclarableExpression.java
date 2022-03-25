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
package dev.gradleplugins.buildscript.syntax;

import lombok.EqualsAndHashCode;

import java.util.Optional;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

@EqualsAndHashCode
public final class VariableDeclarableExpression implements AssignableExpression {
	private final boolean isFinal;
	private final Expression type;
	private final String variableName;

	VariableDeclarableExpression(boolean isFinal, Expression type, String variableName) {
		this.isFinal = isFinal;
		this.type = type;
		this.variableName = variableName;
	}

	@Override
	public Expression assign(Expression value) {
		return new PropertyAssignmentExpression(this, value);
	}

	@Override
	public Expression get() {
		return this;
	}

	boolean isFinal() {
		return isFinal;
	}

	String getName() {
		return variableName;
	}

	Optional<Expression> getType() {
		return Optional.ofNullable(type);
	}

	public VariableDeclarableExpression ofType(String typeExpression) {
		return new VariableDeclarableExpression(isFinal, literal(typeExpression), variableName);
	}
}
