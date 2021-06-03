package dev.nokee.model.internal;

import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import org.gradle.api.InvalidActionClosureException;

import java.util.Objects;
import java.util.function.BiConsumer;

@EqualsAndHashCode
final class ClosureBackedBiAction<A, B> implements BiConsumer<A, B> {
    private final Closure closure;
    private final int resolveStrategy;

    public ClosureBackedBiAction(Closure closure) {
        this(closure, Closure.DELEGATE_FIRST);
    }

    public ClosureBackedBiAction(Closure closure, int resolveStrategy) {
        this.closure = closure;
        this.resolveStrategy = resolveStrategy;
    }

    @Override
    public void accept(A delegate, B firstArgument) {
        if (closure == null) {
            return;
        }

        try {
			Closure copy = (Closure) closure.clone();
			copy.setResolveStrategy(resolveStrategy);
			copy.setDelegate(delegate);
			if (copy.getMaximumNumberOfParameters() == 0) {
				copy.call();
			} else if (copy.getMaximumNumberOfParameters() == 1) {
				copy.call(firstArgument);
			} else {
				copy.call(delegate, firstArgument);
			}
        } catch (groovy.lang.MissingMethodException e) {
            if (Objects.equals(e.getType(), closure.getClass()) && Objects.equals(e.getMethod(), "doCall")) {
                throw new InvalidActionClosureException(closure, delegate);
            }
            throw e;
        }
    }

    public Closure getClosure() {
        return closure;
    }
}
