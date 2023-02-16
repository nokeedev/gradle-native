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
package dev.nokee.internal.testing.forwarding;

import java.util.function.Function;

final class DefaultForwardingWrapper<DelegateType, ObjectType> implements ForwardingWrapperEx<DelegateType, ObjectType> {
	private final Class<DelegateType> interfaceType;
	private final Function<? super DelegateType, ? extends ObjectType> wrapperFactory;

	public DefaultForwardingWrapper(Class<DelegateType> interfaceType, Function<? super DelegateType, ? extends ObjectType> wrapperFactory) {
		this.interfaceType = interfaceType;
		this.wrapperFactory = wrapperFactory;
	}

	@Override
	public ObjectType wrap(DelegateType proxy) {
		return wrapperFactory.apply(proxy);
	}

	@Override
	public Class<DelegateType> getForwardType() {
		return interfaceType;
	}
}
