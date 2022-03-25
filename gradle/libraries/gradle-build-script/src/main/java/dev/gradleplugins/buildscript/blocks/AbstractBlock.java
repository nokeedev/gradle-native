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

import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.statements.EmptyAwareSection;
import dev.gradleplugins.buildscript.statements.NestedStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;

import java.util.Objects;

abstract class AbstractBlock implements Statement, EmptyAwareSection, NestedStatement {
	private final Statement delegate;

	protected AbstractBlock(Statement delegate) {
		this.delegate = delegate;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(delegate);
	}

	public boolean isEmpty() {
		if (delegate instanceof EmptyAwareSection) {
			return ((EmptyAwareSection) delegate).isEmpty();
		} else {
			return false;
		}
	}

	@Override
	public Statement prefixWith(Expression prefix) {
		return delegate.prefixWith(prefix);
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!this.getClass().isInstance(o)) {
			return false;
		}
		AbstractBlock that = (AbstractBlock) o;
		return Objects.equals(delegate, that.delegate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(delegate);
	}
}
