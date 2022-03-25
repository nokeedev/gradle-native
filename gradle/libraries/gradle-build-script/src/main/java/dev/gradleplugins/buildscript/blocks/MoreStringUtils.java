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
package dev.gradleplugins.buildscript.blocks;

import dev.gradleplugins.buildscript.PrettyPrinter;
import dev.gradleplugins.buildscript.statements.Statement;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

final class MoreStringUtils {
	private MoreStringUtils() {}

	public static String toString(Statement delegate) {
		final StringWriter writer = new StringWriter();
		final PrettyPrinter printer = new PrettyPrinter(writer);
		try {
			delegate.writeTo(printer);
			printer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return writer.toString();
	}
}
