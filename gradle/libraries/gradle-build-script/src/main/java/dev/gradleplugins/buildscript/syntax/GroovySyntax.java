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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class GroovySyntax implements Syntax {
	@Override
	public String render(Expression expression) {
		if (expression instanceof MapLiteralValue) {
			return renderMapLiteral((MapLiteralValue) expression);
		} else if (expression instanceof StringLiteralValue) {
			return renderStringLiteral((StringLiteralValue) expression);
		} else if (expression instanceof LiteralValue) {
			return renderLiteral((LiteralValue) expression);
		} else if (expression instanceof GroovyDslLiteralValue) {
			return renderGroovyDslLiteral((GroovyDslLiteralValue) expression);
		} else if (expression instanceof KotlinDslLiteralValue) {
			return renderKotlinDslLiteral((KotlinDslLiteralValue) expression);
		} else if (expression instanceof GradleDslLiteralValue) {
			return renderGradleDslLiteral((GradleDslLiteralValue) expression);
		} else if (expression instanceof MethodInvocationExpression) {
			return renderMethodInvocation((MethodInvocationExpression) expression);
		} else if (expression instanceof PropertyAssignmentExpression) {
			return renderPropertyAssignment((PropertyAssignmentExpression) expression);
		} else if (expression instanceof ChainedPropertyExpression) {
			return render(((ChainedPropertyExpression) expression).getFirst()) + "." + ((ChainedPropertyExpression) expression).getSecond();
		} else if (expression instanceof CompositeExpression) {
			return StreamSupport.stream(((CompositeExpression) expression).spliterator(), false).map(this::render).collect(Collectors.joining());
		} else if (expression instanceof SetLiteralValue) {
			return renderSetLiteral((SetLiteralValue) expression);
		} else if (expression instanceof VariableDeclarableExpression) {
			return renderStandaloneVariableDeclaration((VariableDeclarableExpression) expression);
		} else if (expression instanceof ContainerElementExpression) {
			return renderContainerElement((ContainerElementExpression) expression);
		} else if (expression instanceof PropertyExpression) {
			return renderProperty((PropertyExpression) expression);
		}
		throw new UnsupportedOperationException("Unknown expression");
	}

	private String renderProperty(PropertyExpression expression) {
		return render(expression.get());
	}

	private String renderContainerElement(ContainerElementExpression expression) {
		return render(expression.getTarget()) + "." + expression.getElementName();
	}

	private String renderSetLiteral(SetLiteralValue expression) {
		if (expression.isEmpty()) {
			return "[]";
		} else {
			final List<String> entries = new ArrayList<>();
			expression.forEach(value -> {
				entries.add(render(value));
			});

			final StringBuilder builder = new StringBuilder();
			builder.append("[");
			builder.append(String.join(", ", entries));
			builder.append("]");
			return builder.toString();
		}
	}

	private String renderMapLiteral(MapLiteralValue expression) {
		if (expression.isEmpty()) {
			return "[:]";
		} else {
			final List<String> entries = new ArrayList<>();
			expression.forEach((key, value) -> {
				entries.add(key + ": " + render(value));
			});

			final StringBuilder builder = new StringBuilder();
			builder.append("[");
			builder.append(String.join(", ", entries));
			builder.append("]");
			return builder.toString();
		}
	}

	private String renderStringLiteral(StringLiteralValue expression) {
		final StringBuilder builder = new StringBuilder();
		builder.append('\'').append(expression.get().toString().replace("'", "\\'")).append('\'');
		return builder.toString();
	}

	private String renderLiteral(LiteralValue expression) {
		return expression.get().toString();
	}

	private String renderGroovyDslLiteral(GroovyDslLiteralValue expression) {
		return expression.get();
	}

	private String renderKotlinDslLiteral(KotlinDslLiteralValue expression) {
		throw new UnsupportedOperationException();
	}

	private String renderGradleDslLiteral(GradleDslLiteralValue expression) {
		return render(expression.getGroovy());
	}

	private String renderMethodInvocation(MethodInvocationExpression expression) {
		final StringBuilder builder = new StringBuilder();
		builder.append(render(expression.getName()));
		if (expression.getArguments().size() == 1 && !expression.noOptionalParenthesis()) {
			builder.append(" ");
			if (expression.getArguments().get(0) instanceof MapLiteralValue) {
				builder.append(renderSingleMapLiteralArgument((MapLiteralValue) expression.getArguments().get(0)));
			} else {
				builder.append(expression.getArguments().stream().map(this::render).collect(Collectors.joining(", ")));
			}
		} else {
			builder.append("(").append(expression.getArguments().stream().map(this::render).collect(Collectors.joining(", "))).append(")");
		}
		return builder.toString();
	}

	private String renderSingleMapLiteralArgument(MapLiteralValue expression) {
		if (expression.isEmpty()) {
			return "[:]";
		} else {
			final List<String> entries = new ArrayList<>();
			expression.forEach((key, value) -> {
				entries.add(key + ": " + render(value));
			});

			return String.join(", ", entries);
		}
	}

	private String renderPropertyAssignment(PropertyAssignmentExpression expression) {
		final StringBuilder builder = new StringBuilder();
		if (isUndecoratedGradleProperty(expression.getProperty())) {
			builder.append(render(expression.getProperty().get()));
			builder.append(".set(");
			builder.append(render(expression.getValue()));
			builder.append(")");
		} else if (isVariable(expression.getProperty())) {
			final VariableDeclarableExpression variable = (VariableDeclarableExpression) expression.getProperty();
			if (variable.isFinal()) {
				builder.append("final ");
			}

			builder.append(variable.getType().map(this::render).orElse("def"));
			builder.append(" ");
			builder.append(variable.getName());
			builder.append(" = ");
			builder.append(render(expression.getValue()));
		} else {
			builder.append(render(expression.getProperty().get()));
			builder.append(" = ");
			builder.append(render(expression.getValue()));
		}
		return builder.toString();
	}

	private String renderStandaloneVariableDeclaration(VariableDeclarableExpression expression) {
		if (expression.isFinal()) {
			throw new UnsupportedOperationException("Cannot declare final variable without value");
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(expression.getType().map(this::render).orElse("def"));
		builder.append(" ");
		builder.append(expression.getName());
		return builder.toString();
	}

	private static boolean isUndecoratedGradleProperty(Expression expression) {
		return expression instanceof GradlePropertyExpression && ((GradlePropertyExpression) expression).isUndecoratedGradleProperty();
	}

	private static boolean isVariable(Expression expression) {
		return expression instanceof VariableDeclarableExpression;
	}
}
