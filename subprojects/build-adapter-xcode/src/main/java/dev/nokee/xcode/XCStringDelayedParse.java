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
public final class XCStringDelayedParse implements XCString {
	private XCStringParser parser;
	private String rawString;
	private XCString parsedString;

	public XCStringDelayedParse(XCStringParser parser, String rawString) {
		assert parser != null;
		assert rawString != null;
		this.parser = parser;
		this.rawString = rawString;
	}

	@Override
	public String resolve(ResolveContext context) {
		return parsedString().resolve(context);
	}

	private XCString parsedString() {
		if (parsedString == null) {
			parsedString = parser.parse(rawString);
			parser = null;
			rawString = null;
		}
		return parsedString;
	}

	@Override
	public String toString() {
		return parsedString().toString();
	}
}
