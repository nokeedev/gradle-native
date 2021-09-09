package dev.nokee.model.internal;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.NokeeExtension;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.dsl.ModelSpec;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.dsl.NodePredicate;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

// TODO: Move to internal and mostly make public
public final class ModelSpecs {
	@Deprecated
	public static <S> ModelSpec<S> and(ModelSpec<? super S> first, ModelSpec<S> second) {
		if (first.equals(second)) {
			return second;
		} else {
			return new AndModelSpec<>(first, second);
		}
	}

	/** @see #and(ModelSpec, ModelSpec) */
	@EqualsAndHashCode
	private static final class AndModelSpec<T> implements ModelSpec<T>, Predicate<ModelProjection> {
		private final ModelSpec<? super T> first;
		private final ModelSpec<T> second;

		private AndModelSpec(ModelSpec<? super T> first, ModelSpec<T> second) {
			Preconditions.checkArgument(first.getProjectionType().isAssignableFrom(second.getProjectionType()), "projection type '%s' needs to be the same or subtype of '%s'", second.getProjectionType().getCanonicalName(), first.getProjectionType().getCanonicalName());
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean test(ModelProjection subject) {
			return first.test(subject) && second.test(subject);
		}

		@Override
		public Class<T> getProjectionType() {
			return second.getProjectionType();
		}

		@Override
		public String toString() {
			return "ModelSpecs.and(" + first + ", " + second + ")";
		}
	}

	@Deprecated
	public static <T> ModelSpec<T> or(ModelSpec<T> first, ModelSpec<? super T> second) {
		if (first.equals(second)) {
			return first;
		}

		return new OrModelSpec<>(first, second);
	}

	/** @see #and(ModelSpec, ModelSpec) */
	@EqualsAndHashCode
	private static final class OrModelSpec<T> implements ModelSpec<T>, Predicate<ModelProjection> {
		private final ModelSpec<T> first;
		private final ModelSpec<? super T> second;

		private OrModelSpec(ModelSpec<T> first, ModelSpec<? super T> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean test(ModelProjection subject) {
			return first.test(subject) || second.test(subject);
		}

		@Override
		public Class<T> getProjectionType() {
			return first.getProjectionType(); // TODO: Projection type should be richer than just a class
		}

		@Override
		public String toString() {
			return "ModelSpecs.or(" + first + ", " + second + ")";
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> ProjectionOfSpec<T> projectionOf(Class<T> type) {
		return new ProjectionOfSpec<>(type, subtypeOf(TypeToken.of(type)));
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> ProjectionOfSpec<T> projectionOf(TypeToken<T> type) {
		return new ProjectionOfSpec<>((Class<T>) type.getRawType(), subtypeOf(type));
	}

	/** @see #projectionOf(Class) */
	public static final class ProjectionOfSpec<T> implements ModelSpec<T>, NodePredicate<T>, Function<ModelProjection, Iterable<TypeAwareModelProjection<T>>>, NokeeRuleFactory<T> {
		private final Class<T> rawType;
		private final Predicate<? super Type> predicate;

		ProjectionOfSpec(Class<T> rawType, Predicate<? super Type> predicate) {
			this.rawType = rawType;
			this.predicate = predicate;
		}

		@Override
		public Iterable<TypeAwareModelProjection<T>> apply(ModelProjection subject) {
			if (test(subject)) {
				return ImmutableList.of((TypeAwareModelProjection<T>) subject);
			} else {
				return ImmutableList.of();
			}
		}

		@Override
		public boolean test(ModelProjection subject) {
			return predicate.test(subject.getType());
		}

		@Override
		public Class<T> getProjectionType() {
			return rawType;
		}

		@SafeVarargs
		public final ProjectionOfSpec<T> withTypeParametersOf(Predicate<? super Type>... typeParameters) {
			return new ProjectionOfSpec<T>(rawType, new SubtypeWithParametersPredicate(rawType, typeParametersOf(typeParameters)));
		}

		@Override
		public String toString() {
			return "ModelSpecs.projectionOf(" + predicate + ")";
		}

		@Override
		public Action<NokeeExtension> apply(Consumer<? super TypeAwareModelProjection<T>> action) {
			return nokee -> nokee.getModelRegistry().allProjections().flatMap(this).forEach(action);
		}

		@Override
		@Deprecated
		public Action<NokeeExtension> apply(BiConsumer<? super dev.nokee.model.dsl.ModelNode, ? super KnownDomainObject<T>> action) {
			return nokee -> nokee.getModel().all(this, action);
		}
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the type being tested is assignable to (is a subtype of) {@code type}.
	 *
	 * Example:
	 * <pre>{@code
	 * List<Class<?>> classes = Arrays.asList(
	 *     Object.class, String.class, Number.class, Long.class);
	 * return Iterables.filter(classes, subtypeOf(Number.class));
	 * }</pre>
	 *
	 * The code above returns an iterable containing {@code Number.class} and {@code Long.class}.
	 *
	 * @param type  a super type to test for, must not be null
	 * @return a predicate testing is a subtype of {@code type}, never null
	 */
	@SuppressWarnings("UnstableApiUsage")
	public static Predicate<Type> subtypeOf(Type type) {
		return SUBTYPE_PREDICATE_CACHE.getUnchecked(TypeToken.of(type));
	}

	@SuppressWarnings("UnstableApiUsage")
	private static final LoadingCache<TypeToken<?>, Predicate<Type>> SUBTYPE_PREDICATE_CACHE = CacheBuilder.newBuilder()
		.maximumSize(1000)
		.build(
			new CacheLoader<TypeToken<?>, Predicate<Type>>() {
				public Predicate<Type> load(TypeToken<?> type) {
					return new SubtypeOfPredicate(type);
				}
			});

	@SuppressWarnings("UnstableApiUsage")
	public static Predicate<Type> subtypeOf(TypeToken<?> type) {
		return SUBTYPE_PREDICATE_CACHE.getUnchecked(type);
	}

	@EqualsAndHashCode
	@SuppressWarnings("UnstableApiUsage")
	private static final class SubtypeOfPredicate implements Predicate<Type> {
		@EqualsAndHashCode.Exclude private final Map<Type, Boolean> cache = new HashMap<>();
		private final TypeToken<?> type;

		public SubtypeOfPredicate(TypeToken<?> type) {
			this.type = type;
		}

		@Override
		public boolean test(Type aType) {
			return cache.computeIfAbsent(aType, t -> type.isSupertypeOf(aType));
		}

		@Override
		public String toString() {
			return "subtypeOf(" + type + ")";
		}
	}

	@SafeVarargs
	public static Predicate<ParameterizedType> typeParametersOf(Predicate<? super Type>... typePredicates) {
		return new TypeParametersOfPredicate(ImmutableList.copyOf(typePredicates));
	}

	@EqualsAndHashCode
	private static final class TypeParametersOfPredicate implements Predicate<ParameterizedType> {
		private final List<Predicate<? super Type>> typeParameterPredicates;

		private TypeParametersOfPredicate(List<Predicate<? super Type>> typeParameterPredicates) {
			this.typeParameterPredicates = typeParameterPredicates;
		}

		@Override
		public boolean test(ParameterizedType type) {
			if (type.getActualTypeArguments().length == typeParameterPredicates.size()) {
				for (int i = 0; i < typeParameterPredicates.size(); ++i) {
					if (!typeParameterPredicates.get(i).test(type.getActualTypeArguments()[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "typeParametersOf(" + typeParameterPredicates.stream().map(Objects::toString).collect(joining(", ")) + ")";
		}
	}

	@SuppressWarnings({"rawtypes", "UnstableApiUsage"})
	private static final LoadingCache<TypeToken, Class> RAW_TYPES = CacheBuilder.newBuilder()
		.maximumSize(1000)
		.build(
			new CacheLoader<TypeToken, Class>() {
				public Class load(TypeToken type) {
					return type.getRawType();
				}
			});

	@SuppressWarnings("UnstableApiUsage")
	private static final class SubtypeWithParametersPredicate implements Predicate<Type> {
		@SuppressWarnings("rawtypes")
		private static final LoadingCache<Type, Set<TypeToken>> TYPE_SET_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
				new CacheLoader<Type, Set<TypeToken>>() {
					public Set<TypeToken> load(Type type) {
						TypeToken.TypeSet set = TypeToken.of(type).getTypes();
						Set<TypeToken> result = set;
						return result;
					}
				});
		private final Map<Type, Boolean> cache = new HashMap<>();
		private final Class<?> rawType;
		private final Predicate<? super ParameterizedType> predicate;

		private SubtypeWithParametersPredicate(Class<?> rawType, Predicate<? super ParameterizedType> predicate) {
			this.rawType = rawType;
			this.predicate = predicate;
		}

		@Override
		public boolean test(Type type) {
			return cache.computeIfAbsent(type,
				t -> TYPE_SET_CACHE.getUnchecked(type).stream()
					.filter(this::hasRawType)
					.anyMatch(this::hasTypeParameters));
		}

		private boolean hasRawType(@SuppressWarnings("rawtypes") TypeToken it) {
			return RAW_TYPES.getUnchecked(it).equals(rawType);
		}

		private boolean hasTypeParameters(@SuppressWarnings("rawtypes") TypeToken it) {
			val type = it.getType();
			if (type instanceof ParameterizedType) {
				return predicate.test((ParameterizedType) type);
			}
			return false;
		}

		@Override
		public String toString() {
			return "subtypeOf(" + rawType.getName() + ").and(" + predicate + ")";
		}
	}

	@Deprecated
	public interface NokeeRuleFactory<T> {
		@Deprecated
		Action<NokeeExtension> apply(Consumer<? super TypeAwareModelProjection<T>> action);
		@Deprecated
		Action<NokeeExtension> apply(BiConsumer<? super dev.nokee.model.dsl.ModelNode, ? super KnownDomainObject<T>> action);
	}

	public static AnyTypeModelSpec alwaysTrue() {
		return AlwaysTrueSpec.INSTANCE;
	}

	@Deprecated
	public static abstract class AnyTypeModelSpec implements ModelSpec<Object> {
		@SuppressWarnings("unchecked")
		public final <T> ModelSpec<T> withNarrowedType() {
			return (ModelSpec<T>) this;
		}

		@Override
		public final Class<Object> getProjectionType() {
			return Object.class; // TODO: Projection type should be richer than just a class
		}
	}

	@Deprecated
	private static final class AlwaysTrueSpec extends AnyTypeModelSpec {
		private static final AlwaysTrueSpec INSTANCE = new AlwaysTrueSpec();

		@Override
		public boolean test(ModelProjection node) {
			return true;
		}

		@Override
		public String toString() {
			return ""; // empty string on purpose
		}
	};

	@Deprecated
	public static AnyTypeModelSpec isSelf(ModelNode node) {
		return new IsSelfSpec(node);
	}

	/** @see #isSelf(ModelNode) */
	@EqualsAndHashCode(callSuper = false)
	private static final class IsSelfSpec extends AnyTypeModelSpec {
		private final ModelNode node;

		private IsSelfSpec(ModelNode node) {
			this.node = node;
		}

		@Override
		public boolean test(ModelProjection subject) {
			return subject.getOwner().equals(node);
		}
	}

	@Deprecated
	public static AnyTypeModelSpec withAncestor(ModelNode node) {
		return new WithAncestorSpec(node);
	}

	/** @see #withAncestor(ModelNode) */
	@EqualsAndHashCode(callSuper = false)
	private static final class WithAncestorSpec extends AnyTypeModelSpec {
		private final ModelNode node;

		private WithAncestorSpec(ModelNode node) {
			this.node = node;
		}

		@Override
		public boolean test(ModelProjection subject) {
			Optional<ModelNode> parent = subject.getOwner().getParent();
			while (parent.isPresent()) {
				if (parent.get().equals(node)) {
					return true;
				}
				parent = parent.get().getParent();
			}
			return false;
		}
	}

	@Deprecated
	public static AnyTypeModelSpec withParent(ModelNode node) {
		return new WithParentSpec(node);
	}

	/** @see #withParent(ModelNode) */
	@EqualsAndHashCode(callSuper = false)
	private static final class WithParentSpec extends AnyTypeModelSpec {
		private final ModelNode node;

		private WithParentSpec(ModelNode node) {
			this.node = node;
		}

		@Override
		public boolean test(ModelProjection subject) {
			return subject.getOwner().getParent().map(it -> it.equals(node)).orElse(false);
		}
	}
}
