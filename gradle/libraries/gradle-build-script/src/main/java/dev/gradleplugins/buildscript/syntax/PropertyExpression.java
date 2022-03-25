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

// Property expression could have a selector -> container.elementName.propertyName
@EqualsAndHashCode
public final class PropertyExpression implements AssignableExpression {
	private final Expression propertyName;

	PropertyExpression(Expression propertyName) {
		this.propertyName = propertyName;
	}

	public Expression get() {
		return propertyName;
	}

	public AssignableExpression ofBooleanType() {
		return new BooleanPropertyExpression(propertyName);
	}

	public PropertyExpression property(String propertyName) {
		return new PropertyExpression(new ChainedPropertyExpression(this.propertyName, propertyName));
	}

	public GradlePropertyExpression asGradleProperty() {
		return new GradlePropertyExpression(propertyName);
	}

	@Override
	public Expression assign(Expression value) {
		return new PropertyAssignmentExpression(this, value);
	}
}
