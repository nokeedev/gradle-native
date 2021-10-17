package dev.nokee.platform.base.testers;

import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public final class ConfigureMethodTester<T, U> {
	private final T subject;
	private final Function<? super T, ? extends U> supplier;

	private ConfigureMethodTester(T subject, Function<? super T, ? extends U> supplier) {
		this.subject = subject;
		this.supplier = supplier;
	}

	public static <T, U> ConfigureMethodTester<T, U> of(T subject, Function<? super T, ? extends U> supplier) {
		return new ConfigureMethodTester<>(subject, supplier);
	}

	public <S> ConfigureMethodTester<T, U> testAction(BiConsumer<? super T, ? super Action<? super S>> methodUnderTest) {
		val action = ActionTestUtils.mockAction();
		methodUnderTest.accept(subject, action);
		assertThat("can configure element using action",
			action, calledOnceWith(singleArgumentOf(elementToConfigure())));
		return this;
	}

	public ConfigureMethodTester<T, U> testClosure(BiConsumer<? super T, ? super Closure> methodUnderTest) {
		val closure = ClosureTestUtils.mockClosure(Object.class);
		methodUnderTest.accept(subject, closure);
		assertAll("can configure element using closure",
			() -> assertThat("closure is called once with element to configure as first argument",
				closure, calledOnceWith(singleArgumentOf(elementToConfigure()))),
			() -> assertThat("closure is called once with delegate of element to configure and delegate first strategy",
				closure, calledOnceWith(allOf(delegateOf(elementToConfigure()), delegateFirstStrategy())))
		);
		return this;
	}

	private U elementToConfigure() {
		return supplier.apply(subject);
	}

	public static <T, U> BiConsumer<T, U> forProperty(String propertyName) {
		return (self, actionOrClosure) -> {
			val configureType = configureType(actionOrClosure);
			val configureMethod = assertDoesNotThrow(() -> self.getClass().getMethod(propertyName, configureType),
				() -> "could not find configure method " + configureMethodPrototype(self, propertyName, configureType));
			assertDoesNotThrow(() -> configureMethod.invoke(self, actionOrClosure),
				() -> "could not invoke configure method " + configureMethodPrototype(self, propertyName, configureType));
		};
	}

	private static String configureMethodPrototype(Object target, String methodName, Class<?> configureType) {
		return target.getClass().getSimpleName() + "#" + methodName + "(" + configureType.getSimpleName() + ")";
	}

	private static Class<?> configureType(Object actionOrClosure) {
		if (actionOrClosure instanceof Action) {
			return Action.class;
		} else if (actionOrClosure instanceof Closure) {
			return Closure.class;
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
