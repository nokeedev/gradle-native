package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.NamedDomainObjectIdentifier;
import dev.nokee.platform.base.DomainObjectElement;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gradle.api.Named;

import java.util.Objects;
import java.util.function.Supplier;

public class DomainObjectElements {
	public static final class Existing<T> implements DomainObjectElement<T> {
		@Getter private final Class<T> type;
		private final T value;

		public Existing(Class<T> type, T value) {
			Objects.requireNonNull(type);
			Objects.requireNonNull(value);
			this.type = type;
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public DomainObjectIdentifier getIdentity() {
			return DomainObjectIdentifier.named(String.valueOf(System.identityHashCode(value)));
		}
	}

	public static final class Supplying<T> implements DomainObjectElement<T> {
		@Getter private final Class<T> type;
		private final Supplier<T> valueSupplier;

		public Supplying(Class<T> type, Supplier<T> valueSupplier) {
			this.type = type;
			this.valueSupplier = valueSupplier;
		}

		@Override
		public T get() {
			return Objects.requireNonNull(valueSupplier.get());
		}

		@Override
		public DomainObjectIdentifier getIdentity() {
			return DomainObjectIdentifier.named(String.valueOf(System.identityHashCode(valueSupplier)));
		}
	}

	public static final class Memoizing<T> implements DomainObjectElement<T> {
		@Getter private final Class<T> type;
		@Getter private final DomainObjectIdentifier identity;
		private Supplying<T> elementSupplier;
		private T value;
		private Throwable exception;

		public Memoizing(Supplying<T> elementSupplier) {
			this.type = elementSupplier.getType();
			this.identity = elementSupplier.getIdentity();
			this.elementSupplier = elementSupplier;
		}

		@Override
		public T get() {
			if (exception != null) {
				return ExceptionUtils.rethrow(exception);
			} else if (elementSupplier != null) {
				try {
					value = elementSupplier.get();
				} catch (Throwable ex) {
					exception = ex;
					return ExceptionUtils.rethrow(ex);
				} finally {
					elementSupplier = null;
				}
			}
			return value;
		}
	}

	public static final class Naming<T> implements DomainObjectElement<T>, Named {
		@Getter private final DomainObjectIdentifier identity;
		@Getter private final Class<T> type;
		private final T value;

		public Naming(DomainObjectIdentifier identity, Class<T> type, T value) {
			this.identity = identity;
			this.type = type;
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public String getName() {
			return ((NamedDomainObjectIdentifier)identity).getName();
		}
	}
}
