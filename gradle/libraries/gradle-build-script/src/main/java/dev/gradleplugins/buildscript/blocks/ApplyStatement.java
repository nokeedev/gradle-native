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

import dev.gradleplugins.buildscript.GradleDsl;
import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.NestedStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Expression;
import lombok.EqualsAndHashCode;

import static dev.gradleplugins.buildscript.syntax.Syntax.gradle;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.mapOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;
import static java.util.Collections.singletonMap;

public final class ApplyStatement implements Statement, NestedStatement {
	private final ExpressionStatement delegate;

	private ApplyStatement(ExpressionStatement delegate) {
		this.delegate = delegate;
	}

	public static ApplyStatement apply(Notation notation) {
		return new ApplyStatement(ExpressionStatement.of(invoke("apply", notation.getDelegate())));
	}

	@Override
	public void accept(Visitor visitor) {
		delegate.accept(visitor);
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	@EqualsAndHashCode
	public static final class Notation {
		private final Expression delegate;

		public static Notation plugin(String pluginId) {
			return new Notation(mapOf(singletonMap("plugin", string(pluginId))));
		}

		public static Notation plugin(Class<?> pluginType) {
			return new Notation(mapOf(singletonMap("plugin", literal(pluginType.getTypeName()))));
		}

		public static Notation from(String path) {
			return new Notation(mapOf(singletonMap("from", gradle(
				"'" + GradleDsl.GROOVY.fileName(path) + "'",
				"\"" + GradleDsl.KOTLIN.fileName(path) + "\""))));
		}

		private Notation(Expression delegate) {
			this.delegate = delegate;
		}

		Expression getDelegate() {
			return delegate;
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}
}
