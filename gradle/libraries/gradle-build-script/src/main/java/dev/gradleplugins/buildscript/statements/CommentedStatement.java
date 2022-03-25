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

import java.util.Objects;

public final class CommentedStatement<T extends Statement> implements Statement {
	private final T commentedStatement;

	private CommentedStatement(T commentedStatement) {
		this.commentedStatement = commentedStatement;
	}

	public T getContent() {
		return commentedStatement;
	}

	public static <T extends Statement> CommentedStatement<T> commented(T commentedBlock) {
		return new CommentedStatement<>(Objects.requireNonNull(commentedBlock));
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(commentedStatement);
	}
}
