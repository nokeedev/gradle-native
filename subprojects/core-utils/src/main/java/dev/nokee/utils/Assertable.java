package dev.nokee.utils;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Assertable<T> {
	void satisfiedBy(Context<? extends T> context);

	interface Context<T> {
		Context<T> assertThat(Predicate<? super T> predicate, Function<? super T, ? extends String> messageSupplier);
	}
}
