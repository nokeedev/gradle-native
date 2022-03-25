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

import java.util.Iterator;

@EqualsAndHashCode
final class CompositeExpression implements Expression, Iterable<Expression> {
	private final Iterable<Expression> delegate;

	public CompositeExpression(Iterable<Expression> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Iterator<Expression> iterator() {
		return delegate.iterator();
	}
}
