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
package dev.nokee.model.internal.registry;

import dev.nokee.internal.testing.ClosureAssertions;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.state.ModelStates;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class DomainObjectFunctorConfigureTester<F> extends AbstractDomainObjectFunctorTester<F> {
	private final ModelConfigurer modelConfigurer = Mockito.mock(ModelConfigurer.class);
	private final MyType myTypeInstance = new MyType();
	private final ModelNode node = node("foo", ModelProjections.ofInstance(myTypeInstance),  builder -> builder.withConfigurer(modelConfigurer));

	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node);
	}

	protected void configure(F functor, Action<?> action) {
		invoke(functor, "configure", new Class[]{Action.class}, new Object[]{action});
	}

	@Test
	void canConfigureProviderUsingAction() {
		configure(createSubject(MyType.class), doNothing());
		verify(modelConfigurer, times(1)).configure(any());
	}

	protected void configure(F functor, Closure<Void> closure) {
		invoke(functor, "configure", new Class[] {Closure.class}, new Object[] {closure});
	}

	@Test
	void canConfigureProviderUsingClosure() {
		doAnswer(invocation -> {
			invocation.getArgument(0, ModelAction.class).execute(ModelStates.realize(node));
			return null;
		})
			.when(modelConfigurer)
			.configure(any());
		ClosureAssertions.executeWith(closure -> configure(createSubject(MyType.class), closure))
			.assertCalledOnce()
			.assertLastCalledArgumentThat(equalTo(myTypeInstance))
			.assertDelegateFirstResolveStrategy();
	}
}
