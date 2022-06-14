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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

@EqualsAndHashCode
public final class GroupStatement implements Statement, Iterable<Statement>, EmptyAwareSection {
	private final List<Statement> statementList;

	private GroupStatement(List<Statement> statementList) {
		this.statementList = statementList;
	}

	@Override
	public Iterator<Statement> iterator() {
		return statementList.iterator();
	}

	@Override
	public String toString() {
		return MoreStringUtils.toString(this);
	}

	@Override
	public void accept(Visitor visitor) {
		Objects.requireNonNull(visitor);
		for (Statement statement : statementList) {
			visitor.visit(statement);
		}
	}

	@Override
	public Statement prefixWith(Expression prefix) {
		final GroupStatement.Builder builder = GroupStatement.builder();
		for (Statement statement : statementList) {
			builder.add(statement.prefixWith(prefix));
		}
		return builder.build();
	}

	@Override
	public boolean isEmpty() {
		return statementList.isEmpty();
	}

	public int size() {
		return statementList.size();
	}

	public static Collector<Statement, ?, GroupStatement> toGroupStatement() {
		return Collector.of(GroupStatement::builder, Builder::add, Builder::mergeFrom, Builder::build);
	}

	public static GroupStatement of(Statement statement) {
		return new GroupStatement(Collections.singletonList(Objects.requireNonNull(statement)));
	}

	public static GroupStatement of(Statement firstStatement, Statement... otherStatements) {
		final Builder builder = builder().add(firstStatement);
		for (Statement otherStatement : otherStatements) {
			builder.add(otherStatement);
		}
		return builder.build();
	}

	public static GroupStatement empty() {
		return new GroupStatement(Collections.emptyList());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<Statement> statementList = new ArrayList<>();

		public Builder add(Statement statement) {
			statementList.add(Objects.requireNonNull(statement));
			return this;
		}

		public Builder block(String selector, Consumer<? super Builder> action) {
			final GroupStatement.Builder builder = GroupStatement.builder();
			action.accept(builder);
			statementList.add(BlockStatement.of(literal(selector), builder.build()));
			return this;
		}

		private Builder mergeFrom(Builder other) {
			statementList.addAll(other.statementList);
			return this;
		}

		public GroupStatement build() {
			return new GroupStatement(statementList);
		}
	}
}
