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

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.View;
import org.mockito.Mockito;

class BinaryViewAdapterTest implements ViewDelegateTester<Binary> {
	@SuppressWarnings("unchecked") private final View<Binary> delegate = (View<Binary>) Mockito.mock(View.class);
	private final BinaryViewAdapter<Binary> subject = new BinaryViewAdapter<>(delegate);

	@Override
	public View<Binary> subject() {
		return subject;
	}

	@Override
	public View<Binary> delegate() {
		return delegate;
	}

	@Override
	public Class<? extends Binary> subElementType() {
		return MyBinary.class;
	}

	private interface MyBinary extends Binary {}
}
