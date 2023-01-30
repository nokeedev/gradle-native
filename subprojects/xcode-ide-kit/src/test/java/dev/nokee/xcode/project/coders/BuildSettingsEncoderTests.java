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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.project.ValueEncoder;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

class BuildSettingsEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
	BuildSettingsEncoder subject = new BuildSettingsEncoder();

	@Test
	void canEncodeEmptyBuildSettings() {
		assertThat(subject.encode(BuildSettings.empty(), context), anEmptyMap());
	}

	@Test
	void canEncodeBuildSettings() {
		assertThat(subject.encode(BuildSettings.of(of("K1", "V1", "K2", "V2")), context),
			allOf(hasEntry("K1", "V1"), hasEntry("K2", "V2")));
	}

	@Test
	void hasEncodeType() {
		assertThat(subject.getEncodeType(), equalTo(CoderType.of(BuildSettings.class)));
	}
}
