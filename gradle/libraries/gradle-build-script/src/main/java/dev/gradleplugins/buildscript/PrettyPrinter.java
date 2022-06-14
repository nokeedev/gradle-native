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
import dev.gradleplugins.buildscript.syntax.Syntax;

import java.io.*;
import java.nio.file.Path;
import java.util.Stack;

import static dev.gradleplugins.buildscript.PrettyPrinter.GradleBuildScriptScope.EMPTY_SCOPE;


public final class PrettyPrinter implements Closeable, Flushable {
	private final Writer out;
	private final Syntax syntax;
	private final String indent = "  ";

	public static PrettyPrinter buildFile(GradleDsl dsl, Path destinationDirectory) throws IOException {
		return new PrettyPrinter(new FileWriter(destinationDirectory.resolve(dsl.fileName("build")).toFile()));
	}

	public PrettyPrinter(Writer out) {
		this(out, new GroovySyntax());
	}

	public PrettyPrinter(Writer out, Syntax syntax) {
		this.out = out;
		this.syntax = syntax;
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	public PrettyPrinter write(Statement statement) throws IOException {
		statement.accept(new WriterVisitor(out, syntax, indent));
		return this;
	}

	enum GradleBuildScriptScope {
		EMPTY_SCOPE,
		NONEMPTY_SCOPE
	}

	private static final class WriterVisitor extends VisitorAdapter {
		private final Writer out;
		private final Syntax syntax;
		private final String indent;
		private final Stack<GradleBuildScriptScope> stack = new Stack<>();

		public WriterVisitor(Writer out, Syntax syntax, String indent) {
			this.out = out;
			this.syntax = syntax;
			this.indent = indent;
			this.stack.push(EMPTY_SCOPE);
		}

		@Override
		public void visit(NestedStatement statement) {
			statement.accept(this);
		}

		@Override
		public void visit(GroupStatement statement) {
			if (statement.isEmpty()) {
				// nothing to print
			} else if (statement.size() == 1) {
				doInScope(() -> {
					newLineIfNonEmptyScope();
					statement.accept(this);
				});
			} else {
				doInScope(() -> {
					newLineIfNonEmptyScope();
					statement.accept(this);
				});
			}
		}

		@Override
		public void visit(ExpressionStatement statement) {
			writeIndent();
			write(syntax.render(statement.get()));
			newLine();
		}

		@Override
		public void visit(BlockStatement<?> statement) {
			if (statement.isEmpty()) {
				doInScope(() -> {
					write(syntax.render(statement.getSelector()));
					newLine();
				});
			} else {
				doInScope(() -> {
					writeIndent();
					write(syntax.render(statement.getSelector()));
					write(" {");
					//if (content is single instruction) keep collpse
					newLine();

					newScope(() -> statement.getContent().accept(this));

					writeIndent();
					write("}");
					//end collapse
					newLine();
				});
			}
		}

		@Override
		public void visit(SingleLineCommentBlock<?> statement) {
			doInScope(() -> {
				newLineIfNonEmptyScope();

				writeIndent();
				write("// ");
				write(syntax.render(statement.getComment()));
				newLine();

				statement.accept(this);
			});
		}

		private void write(String s) {
			try {
				out.write(s);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private void writeIndent() {
			for (int i = 0; i < stack.size() - 1; ++i) {
				write(indent);
			}
		}

		private void newLine() {
			write(System.lineSeparator());
		}

		private void newLineIfNonEmptyScope() {
			if (GradleBuildScriptScope.NONEMPTY_SCOPE.equals(stack.peek())) {
				newLine();
			}
		}

		private void markNonEmptyScope() {
			stack.set(stack.size() - 1, GradleBuildScriptScope.NONEMPTY_SCOPE);
		}

		private void newScope(Runnable runnable) {
			stack.push(EMPTY_SCOPE);
			runnable.run();
			stack.pop();
		}

		private void doInScope(Runnable runnable) {
			runnable.run();
			markNonEmptyScope();
		}
	}
}
