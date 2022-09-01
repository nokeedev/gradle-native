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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ReportTester {
	abstract ReportContext subject();

	@Nested
	class DocumentTest {
		@Test
		void canCreateEmptyReport() {
			subject().beginDocument();
			subject().endDocument();

			verifyReport__empty();
		}
	}

	abstract void verifyReport__empty();

	@Nested
	class AttributeTest {
		@Test
		void canCreateReportWithAttributes() {
			subject().beginDocument();
			subject().attribute("a0", "v0");
			subject().attribute("a1", "v1");
			subject().endDocument();

			verifyReportWithAttributes__a0_to_v0_a1_to_v1();
		}
	}

	abstract void verifyReportWithAttributes__a0_to_v0_a1_to_v1();

	@Nested
	class AttributeListTest {
		@Test
		void canCreateReportWithEmptyAttributeList() {
			subject().beginDocument();
			subject().attribute("a0", Collections.emptyList());
			subject().endDocument();

			verifyReportWithAttributeList__a0_to_empty();
		}

		@Test
		void canCreateReportWithAttributeList() {
			subject().beginDocument();
			subject().attribute("a0", Arrays.asList("v0", "v1", "v2"));
			subject().endDocument();

			verifyReportWithAttributeList__a0_to_v0_v1_v2();
		}
	}

	abstract void verifyReportWithAttributeList__a0_to_empty();
	abstract void verifyReportWithAttributeList__a0_to_v0_v1_v2();

	@Nested
	class AttributeGroupTest {
		@Test
		void canCreateReportWithAttributeGroup() {
			subject().beginDocument();
			subject().attributeGroup("a0", it -> it.attribute("a1", "v1"));
			subject().endDocument();

			verifyReportWithGroup__a0_with_a1_to_v1();
		}
	}

	abstract void verifyReportWithGroup__a0_with_a1_to_v1();
}
