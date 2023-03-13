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
package dev.nokee.xcode.project;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapper.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.xcode.project.RecodeableKeyedObject.of;
import static org.hamcrest.MatcherAssert.assertThat;

class RecodeableKeyedObjectForwardingTests {
	@Test
	void forwardsGlobalIdToDelegate() {
		assertThat(forwarding(KeyedObject.class, this::forWrapper), forwardsToDelegate(method("globalId")));
	}

	@Test
	void forwardsIsaToDelegate() {
		assertThat(forwarding(KeyedObject.class, this::forWrapper), forwardsToDelegate(method("isa")));
	}

	@Test
	void forwardsTryDecodeToDelegate() {
		assertThat(forwarding(KeyedObject.class, this::forWrapper), forwardsToDelegate(method("tryDecode")));
	}

	@Test
	void forwardsEncodeToDelegate() {
		assertThat(forwarding(KeyedObject.class, this::forWrapper), forwardsToDelegate(method("encode")));
	}

	private RecodeableKeyedObject forWrapper(KeyedObject delegate) {
		return new RecodeableKeyedObject(delegate, of(new CodingKey[0]));
	}
}
