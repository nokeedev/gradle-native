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

import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.platform.base.testers.VariantDimensionBuilderTester;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.BiPredicate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class VariantDimensionBuilderAdapterTest implements VariantDimensionBuilderTester<VariantDimensionBuilderAdapterTest.MyAxis> {
	@SuppressWarnings("unchecked")
	private final VariantDimensionBuilderAdapter.Callback<MyAxis> listener = mock(VariantDimensionBuilderAdapter.Callback.class);
	private final VariantDimensionBuilderAdapter<MyAxis> subject = new VariantDimensionBuilderAdapter<>(listener);

	@Override
	public VariantDimensionBuilder<MyAxis> subject() {
		return subject;
	}

	@Test
	void forwardsOnlyOnToCallback() {
		subject.onlyOn(new MyOtherAxis());
		Mockito.verify(listener).accept(eq(MyOtherAxis.class), notNull());
	}

	@Test
	void forwardsExceptOnToCallback() {
		subject.exceptOn(new MyOtherAxis());
		Mockito.verify(listener).accept(eq(MyOtherAxis.class), notNull());
	}

	@Test
	void forwardsOnlyIfToCallback() {
		@SuppressWarnings("unchecked")
		BiPredicate<Optional<MyAxis>, MyOtherAxis> predicate = mock(BiPredicate.class);
		subject.onlyIf(MyOtherAxis.class, predicate);
		Mockito.verify(listener).accept(MyOtherAxis.class, notNull());
	}

	@Test
	void forwardsExceptIfToCallback() {
		@SuppressWarnings("unchecked")
		BiPredicate<Optional<MyAxis>, MyOtherAxis> predicate = spy(BiPredicate.class);
		subject.exceptIf(MyOtherAxis.class, predicate);
		Mockito.verify(listener).accept(eq(MyOtherAxis.class), notNull());
	}

	@Value public static class MyAxis {}
	@Value public static class MyOtherAxis {}
}
