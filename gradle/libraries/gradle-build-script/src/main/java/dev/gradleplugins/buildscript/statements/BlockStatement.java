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
public final class BlockStatement<T extends Statement> implements Statement {
	private final Expression sectionSelector;
	private final T content;

	private BlockStatement(Expression sectionSelector, T content) {
		this.sectionSelector = sectionSelector;
		this.content = content;
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	@Override
	public BlockStatement<Statement> prefixWith(Expression prefix) {
		return new BlockStatement<>(concat(asList(prefix, sectionSelector)), content);
	}

	public BlockStatement<Statement> useExplicitIt() {
		return new BlockStatement<>(sectionSelector, content.prefixWith(literal("it.")));
	}

	public Statement useGetter() {
		return content.prefixWith(concat(asList(sectionSelector, literal("."))));
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public Expression getSelector() {
		return sectionSelector;
	}

	public T getContent() {
		return content;
	}

	public boolean isEmpty() {
		if (content instanceof EmptyAwareSection) {
			return ((EmptyAwareSection) content).isEmpty();
		} else {
			return false;
		}
	}

	public static <T extends Statement> BlockStatement<T> of(Expression selector, T content) {
		return new BlockStatement<>(Objects.requireNonNull(selector), Objects.requireNonNull(content));
	}
}
