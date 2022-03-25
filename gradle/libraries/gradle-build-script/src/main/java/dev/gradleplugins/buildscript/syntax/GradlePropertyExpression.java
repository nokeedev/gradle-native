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

@EqualsAndHashCode
public final class GradlePropertyExpression implements AssignableExpression {
	private final Expression propertyName;
	private final boolean isUndecorated;

	GradlePropertyExpression(Expression propertyName) {
		this(propertyName, false);
	}

	private GradlePropertyExpression(Expression propertyName, boolean isUndecorated) {
		this.propertyName = propertyName;
		this.isUndecorated = isUndecorated;
	}

	public Expression get() {
		return propertyName;
	}

	public boolean isUndecoratedGradleProperty() {
		return isUndecorated;
	}

	public AssignableExpression undecorated() {
		return new GradlePropertyExpression(propertyName, true);
	}

	@Override
	public Expression assign(Expression value) {
		return new PropertyAssignmentExpression(this, value);
	}
}
