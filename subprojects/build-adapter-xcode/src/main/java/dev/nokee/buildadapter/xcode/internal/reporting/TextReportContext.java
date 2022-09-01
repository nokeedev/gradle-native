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
package dev.nokee.buildadapter.xcode.internal.reporting;

import com.google.common.collect.Iterables;
import dev.nokee.buildadapter.xcode.internal.plugins.InspectXcodeTask;

import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * Produces a text-based report.
 */
public final class TextReportContext implements ReportContext {
	private PrintStream out;

	public TextReportContext(PrintStream out) {
		this.out = out;
	}

	@Override
	public void attribute(String attribute, String value) {
		out.println(attribute + ": " + value);
	}

	@Override
	public void attribute(String attribute, Iterable<String> values) {
		if (Iterables.isEmpty(values)) {
			out.println(attribute + ": (none)");
		} else {
			out.println(attribute + ":");
			values.forEach(it -> out.println(" - " + it));
		}
	}

	@Override
	public void beginDocument() {

	}

	@Override
	public void endDocument() {

	}

	@Override
	public void attributeGroup(String attribute, Consumer<? super ReportContext> action) {
		out.println(attribute + ":");
		action.accept(new ReportContext() {
			@Override
			public void attribute(String attribute, String value) {
				out.print('\t');
				TextReportContext.this.attribute(attribute, value);
			}

			@Override
			public void attribute(String attribute, Iterable<String> values) {
				out.print('\t');
				TextReportContext.this.attribute(attribute, values);
			}

			@Override
			public void beginDocument() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void endDocument() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void attributeGroup(String attribute, Consumer<? super ReportContext> action) {
				out.print('\t');
				TextReportContext.this.attributeGroup(attribute, action);
			}
		});
	}
}
