package dev.nokee.model.internal.dsl;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectConfigurer;
import dev.nokee.model.internal.RealizableDomainObjectRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.gradle.util.ConfigureUtil;

public class GroovyDslViewInvoker<TYPE, T extends TYPE> extends AbstractGroovyDslInvoker<TYPE, T> implements GroovyDslInvoker<T> {
	private final DomainObjectIdentifier owner;
	private final DomainObjectConfigurer<TYPE> configurer;

	public GroovyDslViewInvoker(GroovyObject object, DomainObjectIdentifier owner, Class<T> entityType, RealizableDomainObjectRepository<TYPE> repository, DomainObjectConfigurer<TYPE> configurer) {
		super(object, owner, entityType, repository);
		this.owner = owner;
		this.configurer = configurer;
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type) {
		return forwardMissingMethodInvocation(name, new Object[]{type});
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type, Closure<Void> closure) {
		return forwardMissingMethodInvocation(name, new Object[]{type, closure});
	}

	@Override
	protected <S extends T> void configure(String name, Class<S> type, Closure<Void> closure) {
		configurer.configure(owner, name, type, ConfigureUtil.configureUsing(closure));
	}
}
