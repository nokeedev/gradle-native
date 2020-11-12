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
