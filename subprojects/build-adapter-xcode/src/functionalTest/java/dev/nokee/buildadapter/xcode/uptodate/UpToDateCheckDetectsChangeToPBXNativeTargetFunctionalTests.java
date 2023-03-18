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
package dev.nokee.buildadapter.xcode.uptodate;

import dev.nokee.xcode.objects.targets.ProductTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asNativeTarget;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.productName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.productType;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.removeLast;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXNativeTargetFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "App";
	}

	@Nested
	class BuildPhasesField {
		@Test
		void outOfDateWhenBuildPhaseAdded() {
			xcodeproj(targetUnderTest(buildPhases(add(aBuildPhase()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenBuildPhaseRemoved() {
			xcodeproj(targetUnderTest(buildPhases(removeLast())));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Test
	void outOfDateWhenProductNameChanged() {
		xcodeproj(targetUnderTest(productName("NewApp")));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenProductTypeChanged() {
		xcodeproj(targetUnderTest(asNativeTarget(productType(ProductTypes.BUNDLE))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}
}
