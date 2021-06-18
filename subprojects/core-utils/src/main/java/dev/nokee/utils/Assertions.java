package dev.nokee.utils;

import com.google.common.annotations.VisibleForTesting;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Assertions {
	private Assertions() {}

	static <T> Action<T> assertUsing(Assertable<? super T> spec) {
		return it -> {
			assertConfigured(it, spec);
		};
	}

	static <T> NamedDomainObjectProvider<T> configure(NamedDomainObjectProvider<T> domainObjectProvider, Assertable<T> spec) {
		domainObjectProvider.configure(assertUsing(spec));
		return domainObjectProvider;
	}

	@VisibleForTesting
	static <T> void assertConfigured(T instance, Assertable<? super T> spec) {
		val context = new Context<>(instance);
		spec.satisfiedBy(context);

		context.throwsIfErrors();
	}

	private static final class Context<T> implements Assertable.Context<T> {
		private final T instance;
		private final List<String> errors = new ArrayList<>();

		public Context(T instance) {
			this.instance = instance;
		}

		@Override
		public Assertable.Context<T> assertThat(Predicate<? super T> predicate, Function<? super T, ? extends String> messageSupplier) {
			if (!predicate.test(instance)) {
				errors.add(messageSupplier.apply(instance));
			}
			return this;
		}

		public void throwsIfErrors() {
			if (!errors.isEmpty()) {
				throw new IllegalStateException(String.join(System.lineSeparator(), errors));
			}
		}
	}

	public static <T> AssertableAction<T> doNothingWhenPresent(Action<? super T> whenAbsentAction) {
		return new AssertableAction<T>() {
			@Override
			public void satisfiedBy(Context<? extends T> context) {
				// do nothing...
			}

			@Override
			public void execute(T t) {
				whenAbsentAction.execute(t);
			}
		};
	}

	public interface AssertableAction<T> extends Action<T>, Assertable<T> {}
}
