package dev.nokee.model.internal;

import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static dev.nokee.model.internal.SupportedTypes.instanceOf;
import static dev.nokee.model.internal.SupportedTypes.subtypeOf;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.getElementType;

/**
 * Adapt named domain object containers to an uniform registry.
 *
 * @param <T> the base element type to register
 */
abstract class NamedDomainObjectContainerRegistry<T> {
	public abstract <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type);
	public abstract <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action);
	public abstract <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type);
	public abstract <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action);

	public abstract NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes();

	public abstract NamedDomainObjectContainer<T> getContainer();

	static final class NamedContainerRegistry<T> extends NamedDomainObjectContainerRegistry<T> {
		private final NamedDomainObjectRegistry.RegistrableTypes registrableTypes;
		private final NamedDomainObjectContainer<T> container;

		public NamedContainerRegistry(NamedDomainObjectContainer<T> container) {
			this.container = container;
			registrableTypes = new NamedDomainObjectContainerRegistrableTypes(getElementType(container));
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
			return cast(type, container).register(name);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
			return cast(type, container).register(name, action);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(cast(type, container), name);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(cast(type, container), name, action);
		}

		@SuppressWarnings("unchecked")
		private static <S extends T, T> NamedDomainObjectContainer<S> cast(Class<S> type, NamedDomainObjectContainer<T> container) {
			return (NamedDomainObjectContainer<S>) container;
		}

		@Override
		public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return registrableTypes;
		}

		@Override
		public NamedDomainObjectContainer<T> getContainer() {
			return container;
		}

		private static final class NamedDomainObjectContainerRegistrableTypes implements NamedDomainObjectRegistry.RegistrableTypes {
			private final NamedDomainObjectRegistry.SupportedType registrableType;

			private NamedDomainObjectContainerRegistrableTypes(Class<?> containerType) {
				this.registrableType = instanceOf(containerType);
			}

			@Override
			public boolean canRegisterType(Class<?> type) {
				return registrableType.supports(type);
			}

			@Override
			public Iterator<NamedDomainObjectRegistry.SupportedType> iterator() {
				return Stream.of(registrableType).iterator();
			}
		}
	}

	static final class PolymorphicContainerRegistry<T> extends NamedDomainObjectContainerRegistry<T> {
		private final NamedDomainObjectRegistry.RegistrableTypes registrableTypes = new PolymorphicDomainObjectContainerRegistrableTypes();
		private final PolymorphicDomainObjectContainer<T> container;

		public PolymorphicContainerRegistry(PolymorphicDomainObjectContainer<T> container) {
			this.container = container;
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
			return container.register(name, type);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
			return container.register(name, type, action);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type, action);
		}

		@Override
		public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return registrableTypes;
		}

		@Override
		public NamedDomainObjectContainer<T> getContainer() {
			return container;
		}

		private final class PolymorphicDomainObjectContainerRegistrableTypes implements NamedDomainObjectRegistry.RegistrableTypes {
			@Override
			public boolean canRegisterType(Class<?> type) {
				return getSupportedTypes().anyMatch(it -> it.supports(type));
			}

			private Stream<NamedDomainObjectRegistry.SupportedType> getSupportedTypes() {
				return getCreatableType().stream().map(SupportedTypes::instanceOf);
			}

			private Set<? extends Class<? extends T>> getCreatableType() {
				return ((PolymorphicDomainObjectContainerInternal<T>) container).getCreateableTypes();
			}

			@Override
			public Iterator<NamedDomainObjectRegistry.SupportedType> iterator() {
				return getSupportedTypes().iterator();
			}
		}
	}

	static final class TaskContainerRegistry extends NamedDomainObjectContainerRegistry<Task> {
		private static final NamedDomainObjectRegistry.RegistrableTypes REGISTRABLE_TYPES = new TaskContainerRegistrableTypes();
		private final TaskContainer container;

		public TaskContainerRegistry(TaskContainer container) {
			this.container = container;
		}

		@Override
		public NamedDomainObjectContainer<Task> getContainer() {
			return container;
		}

		@Override
		public <S extends Task> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
			return container.register(name, type);
		}

		@Override
		public <S extends Task> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
			return container.register(name, type, action);
		}

		@Override
		public <S extends Task> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type);
		}

		@Override
		public <S extends Task> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
			return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type, action);
		}

		@Override
		public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return REGISTRABLE_TYPES;
		}

		private static final class TaskContainerRegistrableTypes implements NamedDomainObjectRegistry.RegistrableTypes {
			private final NamedDomainObjectRegistry.SupportedType registrableTypes = subtypeOf(Task.class);

			@Override
			public boolean canRegisterType(Class<?> type) {
				return registrableTypes.supports(type);
			}

			@Override
			public Iterator<NamedDomainObjectRegistry.SupportedType> iterator() {
				return Stream.of(registrableTypes).iterator();
			}
		}
	}

	static /*final*/ class SoftwareComponentContainerRegistry extends NamedDomainObjectContainerRegistry<SoftwareComponent> {
		private static final NamedDomainObjectRegistry.RegistrableTypes REGISTRABLE_TYPES = new SoftwareComponentContainerRegistrableTypes();
		private final SoftwareComponentContainer container;
		private final SoftwareComponentFactory softwareComponentFactory;

		@Inject // because of SoftwareComponentFactory
		public SoftwareComponentContainerRegistry(SoftwareComponentContainer container, SoftwareComponentFactory softwareComponentFactory) {
			this.container = container;
			this.softwareComponentFactory = softwareComponentFactory;
		}

		@Override
		public NamedDomainObjectContainer<SoftwareComponent> getContainer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <S extends SoftwareComponent> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
			container.add(softwareComponentFactory.adhoc(name));
			return container.named(name, type);
		}

		@Override
		public <S extends SoftwareComponent> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
			val object = softwareComponentFactory.adhoc(name);
			container.add(object);
			action.execute((S) object);
			return container.named(name, type);
		}

		@Override
		public <S extends SoftwareComponent> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
			val object = container.findByName(name);
			if (object == null) {
				container.add(softwareComponentFactory.adhoc(name));
			}
			return container.named(name, type);
		}

		@Override
		public <S extends SoftwareComponent> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
			SoftwareComponent object = container.findByName(name);
			if (object == null) {
				object = softwareComponentFactory.adhoc(name);
				container.add(object);
				action.execute((S) object);
			}
			return container.named(name, type);
		}

		@Override
		public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return REGISTRABLE_TYPES;
		}

		private static final class SoftwareComponentContainerRegistrableTypes implements NamedDomainObjectRegistry.RegistrableTypes {
			private final NamedDomainObjectRegistry.SupportedType registrableTypes = instanceOf(AdhocComponentWithVariants.class);

			@Override
			public boolean canRegisterType(Class<?> type) {
				return registrableTypes.supports(type);
			}

			@Override
			public Iterator<NamedDomainObjectRegistry.SupportedType> iterator() {
				return Stream.of(registrableTypes).iterator();
			}
		}
	}
}
