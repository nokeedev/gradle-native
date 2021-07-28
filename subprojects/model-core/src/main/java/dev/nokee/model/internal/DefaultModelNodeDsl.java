package dev.nokee.model.internal;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.dsl.NodePredicate;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.streams.ModelStream;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;

@EqualsAndHashCode(callSuper = false)
final class DefaultModelNodeDsl extends GroovyObjectSupport implements ModelNode {
	private final NamedDomainObjectRegistry registry;
	@EqualsAndHashCode.Include private final dev.nokee.model.core.ModelNode delegate;
	@EqualsAndHashCode.Exclude private final ModelNodeFactory factory;
	@EqualsAndHashCode.Exclude private final ModelStream<ModelProjection> stream;
	@EqualsAndHashCode.Exclude private final ObjectFactory objectFactory;
	@EqualsAndHashCode.Exclude private final GroovyDslSupport dslSupport;

	public DefaultModelNodeDsl(NamedDomainObjectRegistry registry, dev.nokee.model.core.ModelNode delegate, ModelNodeFactory factory, ModelStream<ModelProjection> stream, ObjectFactory objectFactory) {
		this.registry = registry;
		this.delegate = delegate;
		this.factory = factory;
		this.stream = stream;
		this.objectFactory = objectFactory;
		this.dslSupport = GroovyDslSupport.builder()
			.metaClass(getMetaClass())
			.whenInvokeMethod(this::node)
			.whenInvokeMethod(Closure.class, this::node)
			.whenInvokeMethod(Action.class, this::node)
			.whenInvokeMethod(Class.class, this::node)
			.whenInvokeMethod(Class.class, Closure.class, this::node)
			.whenInvokeMethod(Class.class, Action.class, this::node)
			.whenInvokeMethod(Class.class, BiConsumer.class, this::node)
			.whenGetProperty(identity -> findChildNode(identity).map(it -> it))
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
	public <T> KnownDomainObject<T> node(Object identity, Class<T> type, Action<? super T> action) {
		return getOrCreateChildNode(identity).projection(type, action);
	}

	@Override
	public <T> KnownDomainObject<T> node(Object identity, Class<T> type, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action) {
		val node = getOrCreateChildNode(identity);
		val knownObject = node.projection(type);
		action.accept(node, knownObject);
		return knownObject;
	}

	@Override
	public <T> KnownDomainObject<T> node(Object identity, Class<T> type, @SuppressWarnings("rawtypes") Closure closure) {
		return node(identity, type, new ClosureBackedBiAction<>(closure));
	}

	private ModelNode getOrCreateChildNode(Object identity) {
		// TODO: Assert identity is same or less info than selected node's identity:
		//  because if node was created with a String but trying to reference using machines.host... the identity has less information so we may be facing an out-of-order access.
		return factory.create(delegate.getChildNodes().filter(ofName(identity)).findFirst().orElseGet(() -> delegate.newChildNode(identity)));
	}

	private Optional<ModelNode> findChildNode(Object identity) {
		return delegate.getChildNodes().filter(ofName(identity)).findFirst().map(factory::create);
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
		val projection = delegate.getProjections().filter(projectionOf(type)).map(it -> (TypeAwareModelProjection<T>) it).findFirst().orElseGet(() -> {
			return delegate.newProjection(builder -> {
				var previous = delegate.getParent();
				String name = "";
				while (previous.isPresent()) {
					name = previous.get().getIdentity().toString() + StringUtils.capitalize(name);
					previous = previous.get().getParent();
				}

				if (Task.class.isAssignableFrom(type)) {
					name = delegate.getIdentity().toString() + StringUtils.capitalize(name);
				} else {
					name = name + StringUtils.capitalize(delegate.getIdentity().toString());
				}

				return builder.type(type).forProvider(registry.registerIfAbsent(StringUtils.uncapitalize(name), type));
			});
		});
		return new DefaultKnownDomainObject<>(type, projection);
	}

	@Override
	public <T> KnownDomainObject<T> projection(Class<T> type, Action<? super T> action) {
		val projection = delegate.getProjections().filter(projectionOf(type)).map(it -> (TypeAwareModelProjection<T>) it).findFirst().orElseGet(() -> {
			return delegate.newProjection(builder -> {
				var previous = delegate.getParent();
				String name = "";
				while (previous.isPresent()) {
					name = previous.get().getIdentity().toString() + StringUtils.capitalize(name);
					previous = previous.get().getParent();
				}

				if (Task.class.isAssignableFrom(type)) {
					name = delegate.getIdentity().toString() + StringUtils.capitalize(name);
				} else {
					name = name + StringUtils.capitalize(delegate.getIdentity().toString());
				}

				return builder.type(type).forProvider(registry.registerIfAbsent(StringUtils.uncapitalize(name), type));
			});
		});
		projection.whenRealized(action);
		return new DefaultKnownDomainObject<>(type, projection);
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

	@Override
	public <T> void all(NodePredicate<? super T> spec, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action) {
		stream.filter(spec.scope(delegate)).forEach(it -> {
			action.accept(factory.create(it.getOwner()), new DefaultKnownDomainObject<>((Class<T>) it.getType(), it));
		});
	}

	@Override
	public <T> void all(NodePredicate<T> spec, Class<? extends BiConsumer<? super ModelNode, ? super KnownDomainObject<T>>> rule) {
		all(spec, objectFactory.newInstance(rule));
	}

	@Override
	public <T> void all(NodePredicate<T> spec, @SuppressWarnings("rawtypes") Closure closure) {
		all(spec, new ClosureBackedBiAction<>(closure));
	}

	@Override
	public <T> Provider<? extends Iterable<T>> all(NodePredicate<T> predicate) {
		val spec = predicate.scope(delegate);
		return stream.filter(spec).map(it -> it.get(spec.getProjectionType())).collect(Collectors.toList());
	}
}
