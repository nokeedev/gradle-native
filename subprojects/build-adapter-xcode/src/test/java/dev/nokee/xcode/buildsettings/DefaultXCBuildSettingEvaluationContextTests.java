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
package dev.nokee.xcode.buildsettings;

import dev.nokee.internal.testing.forwarding.ForwardingWrapperEx;
import dev.nokee.xcode.DefaultXCBuildSettingEvaluationContext;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettings;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapperEx.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwards;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static org.hamcrest.MatcherAssert.assertThat;

class DefaultXCBuildSettingEvaluationContextTests {
	@Test
	void forwardsGetBuildSettingByNameToBuildSettings() {
		ForwardingWrapperEx<XCBuildSettings, XCBuildSetting.EvaluationContext> subject = forwarding(XCBuildSettings.class, DefaultXCBuildSettingEvaluationContext::new);
		assertThat(subject, forwards(method(XCBuildSetting.EvaluationContext::get)).to(method(XCBuildSettings::get)));
	}
}
