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

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.View;
import org.mockito.Mockito;

class VariantViewAdapterTest implements ViewDelegateTester<Variant> {
	@SuppressWarnings("unchecked") private final View<Variant> delegate = (View<Variant>) Mockito.mock(View.class);
	private final VariantViewAdapter<Variant> subject = new VariantViewAdapter<>(delegate);

	@Override
	public View<Variant> subject() {
		return subject;
	}

	@Override
	public View<Variant> delegate() {
		return delegate;
	}

	@Override
	public Class<? extends Variant> subElementType() {
		return MyVariant.class;
	}

	private interface MyVariant extends Variant {}
}
