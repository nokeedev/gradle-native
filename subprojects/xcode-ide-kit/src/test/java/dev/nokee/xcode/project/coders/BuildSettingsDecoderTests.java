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
import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.utils.ThrowingDecoderContext;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BuildSettingsDecoderTests {
	ValueDecoder.Context context = new ThrowingDecoderContext();
	BuildSettingsDecoder subject = new BuildSettingsDecoder();

	@Test
	void canDecodeEmptyBuildSettings() {
		assertThat(subject.decode(of(), context), equalTo(BuildSettings.empty()));
	}

	@Test
	void canDecodeBuildSettings() {
		assertThat(subject.decode(of("K1", "V1", "K2", "V2"), context),
			equalTo(BuildSettings.of(of("K1", "V1", "K2", "V2"))));
	}

	@Test
	void hasDecodeType() {
		assertThat(subject.getDecodeType(), equalTo(CoderType.of(BuildSettings.class)));
	}
}
