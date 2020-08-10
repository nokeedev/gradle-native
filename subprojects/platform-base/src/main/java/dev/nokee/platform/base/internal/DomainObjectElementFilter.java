package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectElement;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

public abstract class DomainObjectElementFilter<T> {

	public abstract Action<DomainObjectElement<T>> filter(Action<DomainObjectElement<T>> action);
	public abstract <S extends T> DomainObjectElementFilter<T> withType(Class<S> type);
	public abstract DomainObjectElementFilter<T> matching(Spec<T> spec);

	public static <T> DomainObjectElementFilter<T> none() {
		return new None<>();
	}

	public static <T> DomainObjectElementFilter<T> typed(Class<T> type) {
		return new WithType<>(type);
	}

	private static final class None<T> extends DomainObjectElementFilter<T> {
		@Override
		public Action<DomainObjectElement<T>> filter(Action<DomainObjectElement<T>> action) {
			return action;
		}

		@Override
		public <S extends T> DomainObjectElementFilter<T> withType(Class<S> type) {
			return new WithType<>(type);
		}

		@Override
		public DomainObjectElementFilter<T> matching(Spec<T> spec) {
			return new WithSpec<>(spec);
		}
	}

	private static final class WithType<T> extends DomainObjectElementFilter<T> {
		private final Class<? extends T> type;

		private WithType(Class<? extends T> type) {
			this.type = type;
		}

		@Override
		public Action<DomainObjectElement<T>> filter(Action<DomainObjectElement<T>> action) {
			return new Action<DomainObjectElement<T>>() {
				@Override
				public void execute(DomainObjectElement<T> element) {
					if (type.isAssignableFrom(element.getType())) {
						action.execute(element);
					}
				}
			};
		}

		@Override
		public <S extends T> DomainObjectElementFilter<T> withType(Class<S> type) {
			return new DomainObjectElementFilter.WithType<>(type);
		}

		@Override
		public DomainObjectElementFilter<T> matching(Spec<T> spec) {
			return new WithTypeAndSpec<>(type, spec);
		}
	}

	private static final class WithSpec<T> extends DomainObjectElementFilter<T> {
		private final Spec<? super T> spec;

		private WithSpec(Spec<? super T> spec) {
			this.spec = spec;
		}

		@Override
		public Action<DomainObjectElement<T>> filter(Action<DomainObjectElement<T>> action) {
			return new Action<DomainObjectElement<T>>() {
				@Override
				public void execute(DomainObjectElement<T> element) {
					if (spec.isSatisfiedBy(element.get())) {
						action.execute(element);
					}
				}
			};
		}

		@Override
		public <S extends T> DomainObjectElementFilter<T> withType(Class<S> type) {
			return new DomainObjectElementFilter.WithType<>(type);
		}

		@Override
		public DomainObjectElementFilter<T> matching(Spec<T> spec) {
			return new WithSpec<>(Specs.intersect(this.spec, spec));
		}
	}

	private static final class WithTypeAndSpec<T> extends DomainObjectElementFilter<T> {
		private final Class<? extends T> type;
		private final Spec<? super T> spec;

		private WithTypeAndSpec(Class<? extends T> type, Spec<? super T> spec) {
			this.type = type;
			this.spec = spec;
		}

		@Override
		public Action<DomainObjectElement<T>> filter(Action<DomainObjectElement<T>> action) {
			return new Action<DomainObjectElement<T>>() {
				@Override
				public void execute(DomainObjectElement<T> element) {
					if (type.isAssignableFrom(element.getType())) {
						if (spec.isSatisfiedBy(element.get())) {
							action.execute(element);
						}
					}
				}
			};
		}

		@Override
		public <S extends T> DomainObjectElementFilter<T> withType(Class<S> type) {
			return new DomainObjectElementFilter.WithTypeAndSpec<>(type, spec);
		}

		@Override
		public DomainObjectElementFilter<T> matching(Spec<T> spec) {
			return new WithTypeAndSpec<T>(type, Specs.intersect(this.spec, spec));
		}
	}
}
