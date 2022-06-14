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

public final class ContainerElementExpression implements Expression {
	private final Expression target;
	private final String elementName;

	public ContainerElementExpression(Expression target, String elementName) {
		this.target = target;
		this.elementName = elementName;
	}

	public PropertyExpression property(String propertyName) {
		return new PropertyExpression(new ChainedPropertyExpression(this, propertyName));
	}

	Expression getTarget() {
		return target;
	}

	String getElementName() {
		return elementName;
	}
}
