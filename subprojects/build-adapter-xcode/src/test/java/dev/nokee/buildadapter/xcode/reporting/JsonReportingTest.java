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

import com.google.gson.Gson;
import dev.nokee.buildadapter.xcode.internal.reporting.JsonReportContext;
import dev.nokee.buildadapter.xcode.internal.reporting.ReportContext;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class JsonReportingTest extends ReportTester {
	ByteArrayOutputStream output = new ByteArrayOutputStream();
	JsonReportContext subject = new JsonReportContext(new OutputStreamWriter(output));

	@Override
	ReportContext subject() {
		return subject;
	}

	@Override
	void verifyReport__empty() {
		assertThat(normalize(output.toString()), equalTo("{}"));
	}

	@Override
	void verifyReportWithAttributes__a0_to_v0_a1_to_v1() {
		assertThat(normalize(output.toString()), equalTo("{\"a0\":\"v0\",\"a1\":\"v1\"}"));
	}

	@Override
	void verifyReportWithAttributeList__a0_to_empty() {
		assertThat(normalize(output.toString()), equalTo("{\"a0\":[]}"));
	}

	@Override
	void verifyReportWithAttributeList__a0_to_v0_v1_v2() {
		assertThat(normalize(output.toString()), equalTo("{\"a0\":[\"v0\",\"v1\",\"v2\"]}"));
	}

	@Override
	void verifyReportWithGroup__a0_with_a1_to_v1() {
		assertThat(normalize(output.toString()), equalTo("{\"a0\":{\"a1\":\"v1\"}}"));
	}

	private static String normalize(String output) {
		val gson = new Gson();
		return gson.toJson(gson.fromJson(output, Object.class));
	}
}
