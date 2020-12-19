package dev.nokee.model;

import org.junit.jupiter.api.Nested;

import java.util.function.Consumer;

public abstract class DomainObjectViewConfigureEachTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

//	@BeforeEach
//	void createSubjectUnderTest() {
//		subject = createSubject();
//	}
//
//	private ArgumentCaptor<T> configureEach(ConfigureEachMethod configureEach, DomainObjectView<T> subject) {
//		@SuppressWarnings("unchecked")
//		val action = (Action<T>) mock(Action.class);
//		val captor = ArgumentCaptor.forClass(getElementType());
//		doNothing().when(action).execute(captor.capture());
//
//		configureEach.invoke(subject, action);
//		return captor;
//	}
//
//	private <S extends T> ArgumentCaptor<S> configureEach(ConfigureEachByTypeMethod configureEach, DomainObjectView<T> subject, Class<S> type) {
//		@SuppressWarnings("unchecked")
//		val action = (Action<S>) mock(Action.class);
//		val captor = ArgumentCaptor.forClass(type);
//		doNothing().when(action).execute(captor.capture());
//
//		configureEach.invoke(subject, type, action);
//		return captor;
//	}
//
//	private ArgumentCaptor<T> configureEach(ConfigureEachBySpecMethod configureEach, DomainObjectView<T> subject, Spec<? super T> spec) {
//		@SuppressWarnings("unchecked")
//		val action = (Action<T>) mock(Action.class);
//		val captor = ArgumentCaptor.forClass(getElementType());
//		doNothing().when(action).execute(captor.capture());
//
//		configureEach.invoke(subject, spec, action);
//		return captor;
//	}
//
//
//	// TODO: Add elements and sub elements where some are realized and other are not...
//	protected final <U> U when(ThrowingSupplier<U> executable) {
//		U result = null;
//		element("e0", getElementType());
//		element("e1", getElementType()).get();
//		element("e2", getSubElementType());
//		element("e3", getSubElementType()).get();
//		try {
//			result = executable.get();
//			element("e4", getElementType());
//			element("e5", getElementType()).get();
//			element("e6", getSubElementType());
//			element("e7", getSubElementType()).get();
//		} catch (Throwable throwable) {
//			ExceptionUtils.throwAsUncheckedException(throwable);
//		}
//		return result;
//	}
//
////	@ParameterizedTest
////	@EnumSource(ConfigureEach.class)
////	void callsConfigureEach(ConfigureEachMethod methodUnderTest) {
////		val captor = when(() -> configureEach(methodUnderTest, subject));
////		assertThat("can configure realized view elements, e.g. e1, e3, e5 and e7",
////			captor.getAllValues(), contains(e("e1"), e("e3"), e("e5"), e("e7")));
////	}
//
////	@ParameterizedTest
////	@EnumSource(value = ConfigureEach.class, names = {"ConfigureEachByTypeUsingAction", "ConfigureEachByTypeUsingClosure"})
////	void callsConfigureEachByType(ConfigureEachByTypeMethod methodUnderTest) {
////		val captor = when(() -> configureEach(methodUnderTest, subject, getSubElementType()));
////		assertThat("can configure elements matching a type, e.g. e3 and e7",
////			captor.getAllValues(), contains(e("e3"), e("e7")));
////	}
//
////	@ParameterizedTest
////	@EnumSource(value = ConfigureEach.class, names = {"ConfigureEachBySpecUsingAction", "ConfigureEachBySpecUsingClosure"})
////	void callsConfigureEachBySpec(ConfigureEachBySpecMethod methodUnderTest) {
////		val captor = when(() -> configureEach(methodUnderTest, subject, t -> t.equals(e("e1"))));
////		assertThat("can configure elements matching a spec, e.g. e1",
////			captor.getAllValues(), contains(e("e1")));
////	}
//
////	@ParameterizedTest
////	@EnumSource(ConfigureEach.class)
////	void doesNotCallConfigureEachWhenEmpty(ConfigureEachMethod methodUnderTest) {
////		assertThat("configure action is not called when view is empty",
////			configureEach(methodUnderTest, subject).getAllValues(), empty());
////	}
//
////	@ParameterizedTest
////	@EnumSource(ConfigureEach.class)
////	void doesNotCallConfigureEachForUnrelatedTypes(ConfigureEachMethod methodUnderTest) {
////		element("e0", getElementType()).get();
////		element("e1", getSubElementType()).get();
////		element("e3", WrongType.class).get();
////		val captor = configureEach(methodUnderTest, subject);
////		assertThat("element of unrelated types are not configured",
////			captor.getAllValues(), contains(e("e0"), e("e1")));
////	}
//
//	// TODO: create same type element but in a different path (not nested)
//
//	// TODO: What about nested node of the same type element?....
//
////	private static <T> Closure<Void> adaptToClosure(Action<? super T> action) {
////		return new Closure<Void>(new Object()) {
////			public Void doCall(T t) {
////				action.execute(t);
////				return null;
////			}
////		};
////	}
////
////	private static <T> Class<T> elementType(DomainObjectView<T> target) {
////		return ModelNodes.of(target).get(DomainObjectViewProjection.class).getElementType();
////	}
////	interface ConfigureEachMethod {
////		<T> void invoke(DomainObjectView<T> target, Action<? super T> action);
////	}
////	interface ConfigureEachByTypeMethod {
////		default <T, S extends T> void invoke(DomainObjectView<T> target, Class<S> type, Action<? super S> action) {
////			throw new UnsupportedOperationException();
////		}
////	}
////	interface ConfigureEachBySpecMethod {
////		default <T> void invoke(DomainObjectView<T> target, Spec<? super T> spec, Action<? super T> action) {
////			throw new UnsupportedOperationException();
////		}
////	}
////	enum ConfigureEach implements ConfigureEachMethod, ConfigureEachByTypeMethod, ConfigureEachBySpecMethod {
////		ConfigureEachUsingAction {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(action);
////			}
////		},
////		ConfigureEachUsingClosure {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(adaptToClosure(action));
////			}
////		},
////		ConfigureEachByTypeUsingAction {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(elementType(target), action);
////			}
////
////			@Override
////			public <T, S extends T> void invoke(DomainObjectView<T> target, Class<S> type, Action<? super S> action) {
////				target.configureEach(type, action);
////			}
////		},
////		ConfigureEachByTypeUsingClosure {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(elementType(target), adaptToClosure(action));
////			}
////
////			@Override
////			public <T, S extends T> void invoke(DomainObjectView<T> target, Class<S> type, Action<? super S> action) {
////				target.configureEach(type, adaptToClosure(action));
////			}
////		},
////		ConfigureEachBySpecUsingAction {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(Specs.satisfyAll(), action);
////			}
////
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Spec<? super T> spec, Action<? super T> action) {
////				target.configureEach(spec, action);
////			}
////		},
////		ConfigureEachBySpecUsingClosure {
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Action<? super T> action) {
////				target.configureEach(Specs.satisfyAll(), adaptToClosure(action));
////			}
////
////			@Override
////			public <T> void invoke(DomainObjectView<T> target, Spec<? super T> spec, Action<? super T> action) {
////				target.configureEach(spec, adaptToClosure(action));
////			}
////		}
////	}
////
////	interface WrongType {}
//
//

	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureEachElementsUsingAction extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(action::accept);
		}
	}

	@Nested
	class CanConfigureEachElementsUsingClosure extends AbstractDomainObjectViewConfigureEachElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewConfigureEachTester.this.getSubjectGenerator();
		}

		@Override
		protected void configureEach(DomainObjectView<T> subject, Consumer<? super T> action) {
			subject.configureEach(adaptToClosure(action));
		}
	}
}
