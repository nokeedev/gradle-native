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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.BuildInputService;
import dev.nokee.utils.internal.ConfigurationTimeTransformerAdapter;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BuildInputServiceTests {
	Transformer<String, String> transformer = aTransformer();
	BuildInputService service = new BuildInputService(providerFactory());

	@Test
	void createsCapturedTransformer() {
		assertThat(service.capture(transformer),
			equalTo(new ConfigurationTimeTransformerAdapter<>(providerFactory(), transformer)));
	}
}