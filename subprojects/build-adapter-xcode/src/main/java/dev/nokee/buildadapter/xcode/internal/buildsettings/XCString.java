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
package dev.nokee.buildadapter.xcode.internal.buildsettings;

import java.util.Arrays;

public interface XCString {
	// TODO: Maybe call this toString(ToStringContext ...)?
	String resolve(ResolveContext context);

	interface ResolveContext {
		String get(String variableName);
	}

	static XCString empty() {
		return new EmptyXCString();
	}

	static XCString literal(String s) {
		return new LiteralXCString(s);
	}

	static XCString variable(String s, VariableXCString.Operator... operators) {
		return new VariableXCString(new LiteralXCString(s), Arrays.asList(operators));
	}

	static XCString variable(XCString s, VariableXCString.Operator... operators) {
		return new VariableXCString(s, Arrays.asList(operators));
	}

	static XCString of(XCString... s) {
		return new CompositeXCString(Arrays.asList(s));
	}

	static XCString of(String s) {
		return new DelayedParseXCString(new AntlrMacroExpansionParser(), s);
	}

	static VariableXCString.Operator operator(String name) {
		return new ThrowingOperator(name);
	}

	static VariableXCString.Operator operator(String name, String payload) {
		return new ThrowingOperator(name);
	}
}
