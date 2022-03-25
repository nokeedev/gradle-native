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
package dev.gradleplugins.buildscript;

import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.NestedStatement;
import dev.gradleplugins.buildscript.statements.SingleLineCommentBlock;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.GroovySyntax;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public final class BuildScriptMatchers {

	public static Matcher<Object/*Renderable*/> groovyOf(String... l) {
		return new FeatureMatcher<Object/*Renderable*/, Iterable<String>>(contains(l), "", "") {
			@Override
			protected Iterable<String> featureValueOf(Object/*Renderable*/ actual) {
				return asList(actual.toString().trim().split("\r?\n"));
			}
		};
	}

	public static Matcher<Statement> visit(String... l) {
		return new FeatureMatcher<Statement, String>(equalTo(String.join("", l)), "", "") {
			@Override
			protected String featureValueOf(Statement actual) {
				final ToStringRepresentationVisitor visitor = new ToStringRepresentationVisitor();
				visitor.visit(actual);
				return visitor.toString();
			}
		};
	}

	private static final class ToStringRepresentationVisitor extends VisitorAdapter {
		private final StringBuilder builder = new StringBuilder();
		private final GroovySyntax syntax = new GroovySyntax();

		@Override
		public void visit(NestedStatement statement) {
			statement.accept(this);
		}

		@Override
		public void visit(GroupStatement statement) {
			if (statement.isEmpty()) {
				throw new UnsupportedOperationException("Empty group ???");
			} else if (statement.size() == 1) {
				statement.accept(this);
			} else {
				builder.append("<group>");
				statement.accept(this);
				builder.append("</group>");
			}
		}

		@Override
		public void visit(ExpressionStatement statement) {
			builder.append("<statement>").append(syntax.render(statement.get())).append("</statement>");
		}

		@Override
		public void visit(BlockStatement<?> statement) {
			if (statement.isEmpty()) {
				builder.append("<block selector='").append(syntax.render(statement.getSelector())).append("'/>");
			} else {
				builder.append("<block selector='").append(syntax.render(statement.getSelector())).append("'>");
				statement.getContent().accept(this);
				builder.append("</block>");
			}
		}

		@Override
		public void visit(SingleLineCommentBlock<?> statement) {
			builder.append("<!-- ").append(syntax.render(statement.getComment())).append(" -->");
			statement.accept(this);
		}

		@Override
		public String toString() {
			return builder.toString();
		}
	}
}
