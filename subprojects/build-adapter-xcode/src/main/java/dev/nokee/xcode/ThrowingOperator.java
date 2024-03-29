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

import java.io.Serializable;

@EqualsAndHashCode
public final class ThrowingOperator implements XCStringVariableOperator.Operator, Serializable {
	private final String operator;

	public ThrowingOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public String transform(String s) {
		throw new UnsupportedOperationException(String.format("operator '%s' not supported", operator));
	}

	@Override
	public String toString() {
		return operator;
	}
}
