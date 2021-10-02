/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.testers;

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.BaseNameAwareComponent;
import dev.nokee.platform.base.Component;
import lombok.val;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public interface BaseNameAwareComponentTester {
	BaseNameAwareComponent createSubject(String componentName);

	@Test
	default void canChangeBaseNameGlobally() {
		val subject = createSubject("main");
		subject.getBaseName().set("foo");
		ModelNodeUtils.finalizeProjections(ModelNodes.of(subject)); // TODO: it should be automatic when variant are resolved
		assertThat(subject, hasArtifactBaseNameOf("foo"));
	}

	Matcher<Component> hasArtifactBaseNameOf(String name);
}
