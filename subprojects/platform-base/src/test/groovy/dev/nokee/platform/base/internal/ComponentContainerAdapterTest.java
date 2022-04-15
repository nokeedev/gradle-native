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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.View;
import org.mockito.Mockito;

class ComponentContainerAdapterTest implements ViewDelegateTester<Component> {
	@SuppressWarnings("unchecked") private final View<Component> delegate = (View<Component>) Mockito.mock(View.class);
	private final ComponentContainerAdapter subject = new ComponentContainerAdapter(delegate, new ModelNode());

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

	private interface MyComponent extends Component {}
}
