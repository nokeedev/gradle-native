package dev.nokee.model.internal.dsl;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectRegistry;
import dev.nokee.model.internal.RealizableDomainObjectRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.gradle.util.ConfigureUtil;

public class GroovyDslContainerInvoker<TYPE, T extends TYPE> extends AbstractGroovyDslInvoker<TYPE, T> implements GroovyDslInvoker<T> {
	private final DomainObjectIdentifier owner;
	private final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectRegistry<T> registry;

	public GroovyDslContainerInvoker(GroovyObject object, DomainObjectIdentifier owner, Class<T> entityType, RealizableDomainObjectRepository<TYPE> repository, DomainObjectConfigurer<TYPE> configurer, DomainObjectRegistry<T> registry) {
		super(object, owner, entityType, repository);
		this.owner = owner;
		this.configurer = configurer;
		this.registry = registry;
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type) {
		return registry.register(name, type);
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type, Closure<Void> closure) {
		return registry.register(name, type, ConfigureUtil.configureUsing(closure));
	}

	@Override
	protected <S extends T> void configure(String name, Class<S> type, Closure<Void> closure) {
		configurer.configure(owner, name, type, ConfigureUtil.configureUsing(closure));
	}
}
