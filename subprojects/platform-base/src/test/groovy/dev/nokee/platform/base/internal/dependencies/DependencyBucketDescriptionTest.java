/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.names.ElementName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;

class DependencyBucketDescriptionTest {
	@Test
	void capitalizesDisplayName() {
		assertThat(DependencyBucketDescription.of(DisplayName.of("runtime elements")), hasToString(startsWith("Runtime elements")));
	}

	@Test
	void fullSentenceWithoutOwner() {
		assertThat(DependencyBucketDescription.of(DisplayName.of("link elements")), hasToString("Link elements."));
	}

	@Test
	void fullSentenceWithOwner() {
		assertThat(DependencyBucketDescription.of(DisplayName.of("implementation dependencies")).forOwner(new DefaultDomainObjectIdentifier(ElementName.of("lkel"), null, DisplayName.of("binary"), ModelPath.path("lkel"))),
			hasToString("Implementation dependencies for binary ':lkel'."));
	}
}
