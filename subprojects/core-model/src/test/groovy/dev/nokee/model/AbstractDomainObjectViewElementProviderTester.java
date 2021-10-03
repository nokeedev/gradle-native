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
package dev.nokee.model;

import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AbstractDomainObjectViewElementProviderTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract Provider<? extends Iterable<T>> provider(DomainObjectView<T> subject);

	@BeforeEach
	void addElements() {
		elements("e1", "e2");
	}

	// provider returns assertions
	@Test
	void providerIncludesFutureElements() {
		val provider = provider(subject);
		elements("e3", "e4");
		assertThat("all registered elements should be returned",
			provider.get(), contains(e("e1"), e("e2"), e("e3"), e("e4")));
	}





	// Model node state assertion
	@Test
	void doesNotEagerlyRealizeModelNodes() {
		provider(subject);
		assertThat("model nodes should not realize when provider is not queried",
			elementNodes(stateAtLeast(Realized)), empty());
	}
}
