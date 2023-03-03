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

@EqualsAndHashCode
public final class XCStringVariable implements XCString {
	private final String variable;
	public XCStringVariable(String variable) {
		assert variable != null;
		assert !variable.isEmpty();
		this.variable = variable;
	}

	@Override
	public String resolve(ResolveContext context) {
		String result = context.get(variable);
		if (result == null) {
			result = "$" + variable; // stay literal
		}
		return result;
	}

	@Override
	public String toString() {
		return "$" + variable + "";
	}
}
