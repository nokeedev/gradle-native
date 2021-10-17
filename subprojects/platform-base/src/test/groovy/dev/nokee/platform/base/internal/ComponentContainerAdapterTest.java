/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSpec;
import dev.nokee.platform.base.View;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComponentContainerAdapterTest implements ViewDelegateTester<Component> {
	@SuppressWarnings("unchecked") private final View<Component> delegate = (View<Component>) Mockito.mock(View.class);
	@SuppressWarnings("unchecked") private final BiFunction<String, Class<? extends ComponentSpec>, ModelElement> registry = (BiFunction<String, Class<? extends ComponentSpec>, ModelElement>) Mockito.mock(BiFunction.class);
	private final ComponentContainerAdapter subject = new ComponentContainerAdapter(delegate, registry);

	@Override
	public View<Component> subject() {
		return subject;
	}

	@Override
	public View<Component> delegate() {
		return delegate;
	}

	@Override
	public Class<? extends Component> subElementType() {
		return MyComponent.class;
	}

	@Test
	void forwardsRegisterToDelegate() {
		val result = Mockito.mock(ModelElement.class);
		when(registry.apply("qose", MyComponentSpec.class)).thenReturn(result);
		assertEquals(result, subject.register("qose", MyComponentSpec.class));
		verify(registry).apply("qose", MyComponentSpec.class);
	}

	private interface MyComponentSpec extends ComponentSpec {}
	private interface MyComponent extends Component {}
}
