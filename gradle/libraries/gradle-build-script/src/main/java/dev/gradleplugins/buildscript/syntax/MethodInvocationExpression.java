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

import java.util.List;

@EqualsAndHashCode
public final class MethodInvocationExpression implements Expression {
	private final Expression methodName;
	private final List<Expression> methodArgs;
	private final boolean alwaysUseParenthesis;

	MethodInvocationExpression(Expression methodName, List<Expression> methodArgs) {
		this(methodName, methodArgs, false);
	}

	private MethodInvocationExpression(Expression methodName, List<Expression> methodArgs, boolean alwaysUseParenthesis) {
		this.methodName = methodName;
		this.methodArgs = methodArgs;
		this.alwaysUseParenthesis = alwaysUseParenthesis;
	}

	Expression getName() {
		return methodName;
	}

	List<Expression> getArguments() {
		return methodArgs;
	}

	boolean noOptionalParenthesis() {
		return alwaysUseParenthesis;
	}

	public Expression alwaysUseParenthesis() {
		return new MethodInvocationExpression(methodName, methodArgs, true);
	}
}
