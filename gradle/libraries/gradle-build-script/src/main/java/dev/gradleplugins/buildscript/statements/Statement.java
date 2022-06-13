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

import dev.gradleplugins.buildscript.GradleDsl;
import dev.gradleplugins.buildscript.PrettyPrinter;
import dev.gradleplugins.buildscript.Visitor;
import dev.gradleplugins.buildscript.syntax.Expression;
import dev.gradleplugins.buildscript.syntax.GroovySyntax;
import dev.gradleplugins.buildscript.syntax.KotlinSyntax;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public interface Statement {

	Statement prefixWith(Expression prefix);

	default void writeTo(PrettyPrinter out) throws IOException {
		out.write(this);
	}

	default void writeTo(Path path) throws IOException {
		try (PrettyPrinter writer = new PrettyPrinter(new FileWriter(path.toFile(), false), path.getFileName().toString().endsWith(".gradle") ? new GroovySyntax() : new KotlinSyntax())) {
			writeTo(writer);
		}
	}

	default void appendTo(Path path) throws IOException {
		try (PrettyPrinter writer = new PrettyPrinter(new FileWriter(path.toFile(), true), path.getFileName().toString().endsWith(".gradle") ? new GroovySyntax() : new KotlinSyntax())) {
			writeTo(writer);
		}
	}

	default String toString(GradleDsl dsl) {
		final StringWriter result = new StringWriter();
		try (PrettyPrinter writer = new PrettyPrinter(result, dsl == GradleDsl.GROOVY ? new GroovySyntax() : new KotlinSyntax())) {
			writeTo(writer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return result.toString();
	}

	void accept(Visitor visitor);

	static <T> Consumer<T> empty() {
		return it -> {};
	}

	static <T extends Statement> BlockStatement<T> block(String selector, T content) {
		return BlockStatement.of(literal(selector), content);
	}

	static Statement expressionOf(Expression expression) {
		return ExpressionStatement.of(expression);
	}
}
