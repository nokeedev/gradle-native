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

/**
 * Represent an assignment expression.
 *
 * There are different type of assignment to consider.
 *
 * <b>Getter/setter</b>
 * <pre>
 *     myProp = myValue
 *
 *     // if Kotlin DSL and boolean property
 *     isMyProp = true
 * </pre>
 *
 * <b>Decorated Gradle property</b>
 * <pre>
 *     myProp = myValue // Groovy DSL
 *     myProp.set(myValue) // Kotlin DSL
 * </pre>
 *
 * <b>Undecorated Gradle property</b>
 * <pre>
 *     myProp.set(myValue)
 * </pre>
 */
@EqualsAndHashCode
final class PropertyAssignmentExpression implements Expression {
	private final AssignableExpression property;
	private final Expression value;

	public PropertyAssignmentExpression(AssignableExpression property, Expression value) {
		this.property = property;
		this.value = value;
	}

	AssignableExpression getProperty() {
		return property;
	}

	Expression getValue() {
		return value;
	}
}
