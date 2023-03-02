/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class XCStringVariableOperator implements XCString {
	private final XCString variable;
	private final List<Operator> operatorList;

	public XCStringVariableOperator(XCString variable, List<Operator> operatorList) {
		assert variable != null;
		assert operatorList != null;
		this.variable = variable;
		this.operatorList = operatorList;
	}

	@Override
	public String resolve(ResolveContext context) {
		String resolvedVariable = variable.resolve(context);
		if (resolvedVariable == null) {
			return null;
		}
		String result = context.get(resolvedVariable);
		if (result == null) {
			return null;
		}

		for (Operator operator : operatorList) {
			result = operator.transform(result);
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("$(").append(variable);
		if (!operatorList.isEmpty()) {
			builder.append(":").append(operatorList.stream().map(Object::toString).collect(Collectors.joining(",")));
		}
		builder.append(")");
		return builder.toString();
	}

	public interface Operator {
		String transform(String string);
	}
}
