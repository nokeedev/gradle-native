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
package dev.nokee.buildadapter.xcode.internal.plugins.xcfilelist;

import com.google.common.collect.ImmutableList;
import dev.nokee.util.internal.NotPredicate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static dev.nokee.util.internal.NotPredicate.not;

public final class XCFileListReader implements AutoCloseable {
	private final BufferedReader reader;

	public XCFileListReader(Reader reader) {
		this.reader = new BufferedReader(reader);
	}

	public List<String> read() {
		return reader.lines().map(String::trim).filter(not(this::comment)).collect(ImmutableList.toImmutableList());
	}

	private boolean comment(String line) {
		return line.startsWith("#");
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
