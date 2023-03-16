/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode.project;

import javax.annotation.Nullable;
import java.util.function.Function;

public final class KeyedObjectCodeableAdapter implements KeyedObject {
	private final Codeable delegate;

	public KeyedObjectCodeableAdapter(Codeable delegate) {
		this.delegate = delegate;
	}

	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public long age() {
		return delegate.age();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	public static Function<Codeable, Codeable> asKeyedObject(Function<? super KeyedObject, ? extends Codeable> function) {
		return it -> function.apply(new KeyedObjectCodeableAdapter(it));
	}
}
