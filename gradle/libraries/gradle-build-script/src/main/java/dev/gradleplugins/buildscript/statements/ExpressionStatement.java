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
import lombok.EqualsAndHashCode;

import java.util.Objects;

import static dev.gradleplugins.buildscript.syntax.Syntax.concat;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static java.util.Arrays.asList;

@EqualsAndHashCode
public final class ExpressionStatement implements Statement {
	private final Expression delegate;

	private ExpressionStatement(Expression delegate) {
		this.delegate = delegate;
	}

	public Expression get() {
		return delegate;
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	@Override
	public ExpressionStatement prefixWith(Expression prefix) {
		return new ExpressionStatement(concat(asList(prefix, delegate)));
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public static ExpressionStatement of(Expression expression) {
		return new ExpressionStatement(Objects.requireNonNull(expression));
	}
}
