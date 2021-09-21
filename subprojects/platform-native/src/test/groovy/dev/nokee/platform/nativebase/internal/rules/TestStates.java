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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.DomainObjectEventPublisherImpl;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.RealizableDomainObjectRealizerImpl;

import java.util.Map;
import java.util.WeakHashMap;

class TestStates {
	private static final Map<Object, TestState> STATES = new WeakHashMap<>();

	public static TestState getState(Object testSuite) {
		return STATES.computeIfAbsent(testSuite, TestState::new);
	}

	static class TestState {
		private DomainObjectEventPublisher eventPublisher;
		private RealizableDomainObjectRealizer entityRealizer;
		TestState(Object testSuite) {}

		DomainObjectEventPublisher getEventPublisher() {
			if (eventPublisher == null) {
				eventPublisher = new DomainObjectEventPublisherImpl();
			}
			return eventPublisher;
		}

		public RealizableDomainObjectRealizer getEntityRealizer() {
			if (entityRealizer == null) {
				entityRealizer = new RealizableDomainObjectRealizerImpl(getEventPublisher());
			}
			return entityRealizer;
		}
	}
}
