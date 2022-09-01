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
package dev.nokee.buildadapter.xcode.reporting;

import dev.nokee.buildadapter.xcode.internal.reporting.ReportContext;
import dev.nokee.buildadapter.xcode.internal.reporting.TextReportContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class TextReportingTest extends ReportTester {
	ByteArrayOutputStream output = new ByteArrayOutputStream();
	TextReportContext subject = new TextReportContext(new PrintStream(output));

	@Override
	ReportContext subject() {
		return subject;
	}

	@Override
	void verifyReport__empty() {
		assertThat(lines(output.toString()), contains(""));
	}

	@Override
	void verifyReportWithAttributes__a0_to_v0_a1_to_v1() {
		assertThat(lines(output.toString()), contains("a0: v0", "a1: v1"));
	}

	@Override
	void verifyReportWithAttributeList__a0_to_empty() {
		assertThat(lines(output.toString()), contains("a0: (none)"));
	}

	@Override
	void verifyReportWithAttributeList__a0_to_v0_v1_v2() {
		assertThat(lines(output.toString()), contains("a0:", " - v0", " - v1", " - v2"));
	}

	@Override
	void verifyReportWithGroup__a0_with_a1_to_v1() {
		assertThat(lines(output.toString()), contains("a0:", "\ta1: v1"));
	}

	private static Iterable<String> lines(String output) {
		return Arrays.asList(output.split("\r?\n"));
	}
}
