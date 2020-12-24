package dev.nokee.internal.testing.utils;

import groovy.lang.Closure;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public final class ClosureTestUtils {
	private ClosureTestUtils() {}

	public static <S> Closure<Void> adaptToClosure(Consumer<? super S> action) {
		return new Closure<Void>(new Object()) {
			public Void doCall(S t) {
				assertThat("delegate should be the first parameter", getDelegate(), equalTo(t));
				assertThat("resolve strategy should be delegate first", getResolveStrategy(), equalTo(Closure.DELEGATE_FIRST));
				action.accept(t);
				return null;
			}
		};
	}
}
