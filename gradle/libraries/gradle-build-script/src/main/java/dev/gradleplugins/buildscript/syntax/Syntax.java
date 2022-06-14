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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.Collections.singletonMap;

public interface Syntax {
	String render(Expression expression);

	/**
	 * Creates a singleton map expression from the specified key/value pair.
	 *
	 * @param k1  the key of the single entry in the map expression, must not be null
	 * @param v1  the value of the single entry in the map expression, must not be null
	 * @return an expression representing a singleton map, never null
	 */
	static Expression mapOf(String k1, Expression v1) {
		return new MapLiteralValue(singletonMap(k1, v1));
	}

	/**
	 * Creates a map expression from the specified map literal.
	 *
	 * @param mapLiteral  the content of the map expression, must not be null
	 * @return an expression representing a map, never null
	 */
	static Expression mapOf(Map<String, Expression> mapLiteral) {
		return new MapLiteralValue(mapLiteral);
	}

	/**
	 * Creates a set expression from the specified elements.
	 *
	 * @param setLiteral  the elements, can be empty
	 * @return an expression representing a set, never null
	 */
	static Expression setOf(Expression... setLiteral) {
		return new SetLiteralValue(new LinkedHashSet<>(Arrays.asList(setLiteral)));
	}

	/**
	 * Creates a literal expression from the specified value.
	 * The object literal is evaluated via {@link Object#toString()}.
	 *
	 * @param literal  the literal value, must not be null
	 * @return an expression representing a literal value, never null
	 */
	static Expression literal(Object literal) {
		return new LiteralValue(literal);
	}

	/**
	 * Creates a string expression from the specified string value.
	 *
	 * @param string  the string value, must not be null
	 * @return an expression representing a string value, never null
	 */
	static Expression string(CharSequence string) {
		return new StringLiteralValue(string);
	}

	static Expression groovy(String script) {
		return new GroovyDslLiteralValue(script);
	}

	static Expression kotlin(String script) {
		return new KotlinDslLiteralValue(script);
	}

	static Expression gradle(String groovy, String kotlin) {
		return new GradleDslLiteralValue(new GroovyDslLiteralValue(groovy), new KotlinDslLiteralValue(kotlin));
	}

	/**
	 * Creates a property expression from the specified target expression.
	 * A property mostly refers to plain POJO getter/setter.
	 * The property expression can further customize its behaviour, see {@link PropertyExpression}.
	 * The target expression can represent the property by name or by a more complex expression such as train wreck, e.g. {@code a.b.c}.
	 *
	 * @param target  the expression representing the property, must not be null
	 * @return an expression representing a plain property, never null
	 */
	static PropertyExpression property(Expression target) {
		return new PropertyExpression(target);
	}

	/**
	 * Create a Gradle property expression from the specified target expression.
	 * A Gradle property refers to {@literal Property}, {@literal RegularFileProperty}, etc.
	 * Assignments can differ based the syntax, i.e. {@code prop = 'value'} (Groovy DSL) vs {@code prop.set("value")} (Kotlin DSL).
	 * For this reason, it's important to choose the correct property expression when considering multi-syntax expression.
	 * The property expression can further customize its behaviour, see {@link PropertyExpression}.
	 * The target expression can represent the property by name or by a more complex expression such as train wreck, e.g. {@code a.b.c}.
	 *
	 * @param target  the expression representing the property, must not be null
	 * @return an expression representing a Gradle property, never null
	 */
	static GradlePropertyExpression gradleProperty(Expression target) {
		return new GradlePropertyExpression(target);
	}

	/**
	 * Creates a method invocation expression from the specified method name and arguments expressions.
	 * The invocation expression can further customize its behaviour, see {@link MethodInvocationExpression}.
	 *
	 * @param methodName  the method name of the invocation expression, must not be null
	 * @param methodArgs  the method arguments of the invocation expression, can be empty
	 * @return an expression representing a method invocation, never null
	 */
	static MethodInvocationExpression invoke(String methodName, Expression... methodArgs) {
		return new MethodInvocationExpression(literal(methodName), Arrays.asList(methodArgs));
	}

	/**
	 * Creates a method invocation expression from the specified target and method arguments expressions.
	 * The invocation expression can further customize its behaviour, see {@link MethodInvocationExpression}.
	 *
	 * @param target  the target of the invocation expression, must not be null
	 * @param methodArgs  the method arguments of the invocation expression, can be empty
	 * @return an expression representing a method invocation, never null
	 */
	static MethodInvocationExpression invoke(Expression target, Expression... methodArgs) {
		return new MethodInvocationExpression(target, Arrays.asList(methodArgs));
	}

	/**
	 * Creates a concatenated expression from multiple expressions.
	 *
	 * @param expressions  the expressions to concatenate, must not be null
	 * @return an expression representing all expression one after another, never null
	 */
	static Expression concat(Iterable<Expression> expressions) {
		return new CompositeExpression(expressions);
	}

	/**
	 * Creates a final variable declaration expression.
	 *
	 * @param name  the variable name, must not be null
	 * @return an expression representing final variable declaration, never null
	 */
	static VariableDeclarableExpression val(String name) {
		return new VariableDeclarableExpression(true, null, name);
	}

	/**
	 * Creates a mutable variable declaration expression.
	 *
	 * @param name  the variable name, must not be null
	 * @return an expression representing mutable variable declaration, never null
	 */
	static VariableDeclarableExpression var(String name) {
		return new VariableDeclarableExpression(false, null, name);
	}

	static ContainerElementExpression containerElement(Expression target, String elementName) {
		return new ContainerElementExpression(target, elementName);
	}
}
