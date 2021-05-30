package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.DomainObjectRegistry;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectFactory;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = false)
final class DefaultModelNodeDsl extends GroovyObjectSupport implements ModelNode {
	private final DomainObjectRegistry registry;
	@EqualsAndHashCode.Include private final dev.nokee.model.core.ModelNode delegate;
//	private final NamedDomainObjectFactory<Object> factory = null;
	@EqualsAndHashCode.Exclude private final GroovyDslSupport dslSupport;

	public DefaultModelNodeDsl(DomainObjectRegistry registry, dev.nokee.model.core.ModelNode delegate) {
		this.registry = registry;
		this.delegate = delegate;
		this.dslSupport = GroovyDslSupport.builder()
			.metaClass(getMetaClass())
			.whenInvokeMethod(this::node)
			.whenInvokeMethod(Closure.class, this::node)
			.whenInvokeMethod(Class.class, this::node)
			.whenInvokeMethod(Class.class, Closure.class, this::node)
			.whenGetProperty(this::getOrCreateChildNode)
			.build();
	}

	@Override
	public ModelNode node(Object identity) {
		return getOrCreateChildNode(identity);
	}

	@Override
	public ModelNode node(Object identity, Action<? super ModelNode> action) {
		val result = getOrCreateChildNode(identity);
		action.execute(result);
		return result;
	}

	@Override
	public <T> KnownDomainObject<T> node(Object identity, Class<T> type) {
		return getOrCreateChildNode(identity).projection(type);
	}

	@Override
	public <T> KnownDomainObject<T> node(Object identity, Class<T> type, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action) {
		val node = getOrCreateChildNode(identity);
		val knownObject = node.projection(type);
		action.accept(node, knownObject);
		return knownObject;
	}

	private ModelNode getOrCreateChildNode(Object identity) {
		// TODO: Assert identity is same or less info than selected node's identity:
		//  because if node was created with a String but trying to reference using machines.host... the identity has less information so we may be facing an out-of-order access.
		return new DefaultModelNodeDsl(registry, delegate.getChildNodes().filter(ofName(identity)).findFirst().orElseGet(() -> delegate.newChildNode(identity)));
	}

	private static Predicate<dev.nokee.model.core.ModelNode> ofName(Object identity) {
		return it -> nameOf(identity).equals(it.getName());
	}

	// TODO:
	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	@Override
	public <T> KnownDomainObject<T> projection(Class<T> type) {
		// TODO: Connect projection with known domain object
		val projection = delegate.getProjections().filter(it -> it.canBeViewedAs(type)).findFirst().orElseGet(() -> {
			val identifier = new DomainObjectIdentifier() {};
			return delegate.newProjection(builder -> builder.type(type).forProvider(registry.register(identifier, type)));
		});
		return new DefaultKnownDomainObject<>(type);
	}

	@Override
	public <T> KnownDomainObject<T> projection(Class<T> type, Action<? super T> action) {
		return new DefaultKnownDomainObject<>(type);
	}

	@Override
	public Object getProperty(String name) {
		return dslSupport.getProperty(name);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return dslSupport.invokeMethod(name, args);
	}
}
