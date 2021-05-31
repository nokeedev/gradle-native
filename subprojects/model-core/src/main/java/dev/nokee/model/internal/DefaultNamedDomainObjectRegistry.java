package dev.nokee.model.internal;

import com.google.common.collect.Streams;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static dev.nokee.model.internal.RegistrableTypeAssertions.unregistrableTypeException;

final class DefaultNamedDomainObjectRegistry implements NamedDomainObjectRegistry {
	private final RegistrableTypes registrableTypes = new DefaultRegistrableTypes();
	private final List<NamedDomainObjectContainerRegistry<?>> registries = new ArrayList<>();

	@Override
	public <S> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
		assertRegistrableType(type);
		return registry(type).register(name, type);
	}

	@Override
	public <S> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
		assertRegistrableType(type);
		return registry(type).register(name, type, action);
	}

	@Override
	public <S> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
		assertRegistrableType(type);
		return registry(type).registerIfAbsent(name, type);
	}

	@Override
	public <S> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
		assertRegistrableType(type);
		return registry(type).registerIfAbsent(name, type, action);
	}

	private <T> NamedDomainObjectContainerRegistry<T> registry(Class<T> type) {
		return registries.stream()
			.filter(it -> it.getRegistrableTypes().canRegisterType(type))
			.map(it -> (NamedDomainObjectContainerRegistry<T>) it)
			.findFirst()
			.orElseThrow(() -> unregistrableTypeException(type, "this registry", getRegistrableTypes()));
	}

	@Override
	public RegistrableTypes getRegistrableTypes() {
		return registrableTypes;
	}

	private void assertRegistrableType(Class<?> type) {
		RegistrableTypeAssertions.assertRegistrableType("this registry", getRegistrableTypes(), type);
	}

	public <T> DefaultNamedDomainObjectRegistry registerContainer(NamedDomainObjectContainerRegistry<T> registry) {
		// TODO: Assert registry is not overshadowing another
		registries.add(registry);
		return this;
	}

	private final class DefaultRegistrableTypes implements RegistrableTypes {
		@Override
		public boolean canRegisterType(Class<?> type) {
			return registries.stream().anyMatch(it -> it.getRegistrableTypes().canRegisterType(type));
		}

		@Override
		public Iterator<SupportedType> iterator() {
			return registries.stream()
				.flatMap(it -> Streams.stream(it.getRegistrableTypes()))
				.iterator();
		}
	}
}
