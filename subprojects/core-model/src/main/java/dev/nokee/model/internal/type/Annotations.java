/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Annotations {
	private enum VisitorResult {
		CONTINUE, TERMINATE, SKIP_SUBTREE
	}

	private interface Visitor {
		VisitorResult visitAnnotatedElement(AnnotatedElement element);
		VisitorResult visitSuperclass(Class<?> type);
		VisitorResult visitAnnotation(Annotation annotation);
	}

	private interface SimpleVisitor extends Visitor {
		@Override
		default VisitorResult visitSuperclass(Class<?> type) {
			return VisitorResult.CONTINUE;
		}

		@Override
		default VisitorResult visitAnnotatedElement(AnnotatedElement element) {
			return VisitorResult.CONTINUE;
		}

		@Override
		default VisitorResult visitAnnotation(Annotation annotation) {
			return VisitorResult.CONTINUE;
		}
	}

	private interface AnnotationNode {
		Collection<AnnotationNode> getChildren();

		VisitorResult accept(Visitor visitor);
	}

	private static VisitorResult visitChildren(AnnotationNode self, Visitor visitor) {
		for (AnnotationNode child : self.getChildren()) {
			switch (child.accept(visitor)) {
				case CONTINUE: break;
				case TERMINATE: return VisitorResult.TERMINATE;
				case SKIP_SUBTREE: throw new UnsupportedOperationException();
				default: throw new UnsupportedOperationException();
			}
		}
		return VisitorResult.CONTINUE;
	}

	private static VisitorResult visitSubTree(AnnotationNode self, VisitorResult result, Visitor visitor) {
		switch (result) {
			case CONTINUE: return visitChildren(self, visitor);
			case TERMINATE: return VisitorResult.TERMINATE;
			case SKIP_SUBTREE: return VisitorResult.CONTINUE;
			default: throw new UnsupportedOperationException();
		}
	}

	private interface AnnotatedElementNode extends AnnotationNode {

	}

	private interface Factory {
		AnnotatedElementNode create(AnnotatedElement element);
	}

	private static final class RepeatableAnnotatedElementNode implements AnnotatedElementNode {
		private static final Factory FACTORY = new Factory() {
			@Override
			public AnnotatedElementNode create(AnnotatedElement element) {
				return new RepeatableAnnotatedElementNode(element, this);
			}
		};
		private final AnnotatedElement element;
		private final Factory factory;

		public RepeatableAnnotatedElementNode(AnnotatedElement element, Factory factory) {
			this.element = element;
			this.factory = factory;
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			ImmutableList.Builder<AnnotationNode> builder = ImmutableList.builder();

			if (element instanceof Class) {
				final Class<?> aClass = (Class<?>) element;

				// 1) support top-down semantics for inherited
				final Class<?> superclass = aClass.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					builder.add(new SuperclassAnnotationNode(aClass.getSuperclass(), factory));
				}

				// 2) search on interfaces
				builder.add(new InterfacesNode(aClass.getInterfaces(), factory));
			}

			// 3) find annotations that are directly present
			builder.add(new AnnotationsNode(AnnotationsNode.AnnotationType.Direct, element.getDeclaredAnnotations(), factory));

			// 4) find annotations that are indirectly present
			builder.add(new AnnotationsNode(AnnotationsNode.AnnotationType.Indirect, element.getAnnotations(), factory));

			return builder.build();
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitSubTree(this, visitor.visitAnnotatedElement(element), visitor);
		}
	}

	private static final class NonRepeatableAnnotatedElementNode implements AnnotatedElementNode {
		private static final Factory FACTORY = new Factory() {
			@Override
			public AnnotatedElementNode create(AnnotatedElement element) {
				return new NonRepeatableAnnotatedElementNode(element, this);
			}
		};
		private final AnnotatedElement element;
		private final Factory factory;

		public NonRepeatableAnnotatedElementNode(AnnotatedElement element, Factory factory) {
			this.element = element;
			this.factory = factory;
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			ImmutableList.Builder<AnnotationNode> builder = ImmutableList.builder();

			// Meta-present on directly present annotations?
			builder.add(new AnnotationsNode(AnnotationsNode.AnnotationType.Direct, element.getDeclaredAnnotations(), factory));

			if (element instanceof Class) {
				final Class<?> aClass = (Class<?>) element;

				// Search on interfaces
				builder.add(new InterfacesNode(aClass.getInterfaces(), factory));

				// Indirectly present?
				// Search in class hierarchy
				final Class<?> superclass = aClass.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					builder.add(new SuperclassAnnotationNode(aClass.getSuperclass(), factory));
				}
			}

			// Meta-present on indirectly present annotations?
			builder.add(new AnnotationsNode(AnnotationsNode.AnnotationType.Indirect, element.getAnnotations(), factory));

			return builder.build();
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitSubTree(this, visitor.visitAnnotatedElement(element), visitor);
		}
	}

	private static final class SuperclassAnnotationNode implements AnnotationNode {
		private final Class<?> aClass;
		private final Factory factory;

		public SuperclassAnnotationNode(Class<?> aClass, Factory factory) {
			this.aClass = aClass;
			this.factory = factory;
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			return Collections.singletonList(factory.create(aClass));
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitSubTree(this, visitor.visitSuperclass(aClass), visitor);
		}
	}

	private static final class InterfacesNode implements AnnotationNode {
		private final Class<?>[] interfaces;
		private final Factory factory;

		public InterfacesNode(Class<?>[] interfaces, Factory factory) {
			this.interfaces = interfaces;
			this.factory = factory;
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			return Stream.of(interfaces).map(factory::create).collect(Collectors.toList());
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitChildren(this, visitor);
		}
	}

	private static final class AnnotationsNode implements AnnotationNode {
		private final AnnotationType type;
		private final Annotation[] annotations;
		private final Factory factory;

		private AnnotationsNode(AnnotationType type, Annotation[] annotations, Factory factory) {
			this.type = type;
			this.annotations = annotations;
			this.factory = factory;
		}

		public enum AnnotationType {
			Direct, Indirect
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			return Stream.of(annotations).map(it -> new SingleAnnotationNode(it, factory)).collect(Collectors.toList());
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitChildren(this, visitor);
		}
	}

	private static final class SingleAnnotationNode implements AnnotationNode {
		private final Annotation annotation;
		private final Factory factory;

		private SingleAnnotationNode(Annotation annotation, Factory factory) {
			this.annotation = annotation;
			this.factory = factory;
		}

		@Override
		public Collection<AnnotationNode> getChildren() {
			return Collections.singletonList(factory.create(annotation.annotationType()));
		}

		@Override
		public VisitorResult accept(Visitor visitor) {
			return visitSubTree(this, visitor.visitAnnotation(annotation), visitor);
		}
	}


	private static final class AnnotationTree {
		private final AnnotationNode root;

		private AnnotationTree(AnnotationNode root) {
			this.root = root;
		}

		public void accept(Visitor visitor) {
			root.accept(visitor);
		}
	}


	public static <A extends Annotation> AnnotationFinder<A> forAnnotation(Class<A> annotationType) {
		Objects.requireNonNull(annotationType, "'annotationType' must not be null");

		final Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
		Function<List<A>, Visitor> resultVisitorFactory = null;
		Factory rootNodeFactory = null;
		if (repeatable == null) {
			final boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
			rootNodeFactory = new NullToEmpty(NonRepeatableAnnotatedElementNode.FACTORY);
			resultVisitorFactory = new Function<List<A>, Visitor>() {
				@Override
				public Visitor apply(List<A> result) {
					Function<A, VisitorResult> captor = it -> {
						result.add(it);
						return VisitorResult.TERMINATE;
					};
					return new DirectAnnotationVisitor<>(annotationType, captor, new InheritedVisitor(inherited, new NotInJavaLangAnnotationPackageVisitor(new VisitedVisitor(new SingleAnnotationVisitor<>(annotationType, captor)))));
				}
			};
		} else {
			final Class<? extends Annotation> containerType = repeatable.value();
			final boolean inherited = containerType.isAnnotationPresent(Inherited.class);
			rootNodeFactory = new NullToEmpty(RepeatableAnnotatedElementNode.FACTORY);
			resultVisitorFactory = new Function<List<A>, Visitor>() {
				@Override
				public Visitor apply(List<A> result) {
					Function<A, VisitorResult> captor = it -> {
						result.add(it);
						return VisitorResult.CONTINUE;
					};
					return new InheritedVisitor(inherited, new NotInJavaLangAnnotationPackageVisitor(new VisitedVisitor(new SingleAnnotationVisitor<>(annotationType, captor))));
				}
			};
		}

		return new DefaultAnnotationFinder<>(rootNodeFactory, resultVisitorFactory);
	}

	private static final class NullToEmpty implements Factory {
		private final Factory delegate;

		private NullToEmpty(Factory delegate) {
			this.delegate = delegate;
		}

		@Override
		public AnnotatedElementNode create(@Nullable AnnotatedElement element) {
			if (element == null) {
				return new AnnotatedElementNode() {
					@Override
					public Collection<AnnotationNode> getChildren() {
						return Collections.emptyList();
					}

					@Override
					public VisitorResult accept(Visitor visitor) {
						return VisitorResult.CONTINUE;
					}
				};
			}
			return delegate.create(element);
		}
	}

	public interface AnnotationFinder<A extends Annotation> {
		Result<A> findOn(@Nullable AnnotatedElement element);

		interface Result<A extends Annotation> extends Iterable<A> {
			default Stream<A> stream() {
				return Streams.stream(this);
			}
		}
	}

	private static abstract class ForwardingVisitor implements Visitor {
		private final Visitor delegate;

		protected ForwardingVisitor(Visitor delegate) {
			this.delegate = delegate;
		}

		@Override
		public VisitorResult visitAnnotatedElement(AnnotatedElement element) {
			return delegate().visitAnnotatedElement(element);
		}

		@Override
		public VisitorResult visitSuperclass(Class<?> type) {
			return delegate().visitSuperclass(type);
		}

		@Override
		public VisitorResult visitAnnotation(Annotation annotation) {
			return delegate().visitAnnotation(annotation);
		}

		protected Visitor delegate() {
			return delegate;
		}
	}

	private static final class InheritedVisitor extends ForwardingVisitor {
		private final boolean inherited;

		private InheritedVisitor(boolean inherited, Visitor delegate) {
			super(delegate);
			this.inherited = inherited;
		}

		@Override
		public VisitorResult visitSuperclass(Class<?> type) {
			return inherited ? delegate().visitSuperclass(type) : VisitorResult.SKIP_SUBTREE;
		}
	}

	private static final class NotInJavaLangAnnotationPackageVisitor extends ForwardingVisitor {
		private NotInJavaLangAnnotationPackageVisitor(Visitor delegate) {
			super(delegate);
		}

		public VisitorResult visitAnnotation(Annotation candidate) {
			final Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
			if (notInJavaLangAnnotationPackage(candidateAnnotationType)) {
				return delegate().visitAnnotation(candidate);
			}
			return VisitorResult.SKIP_SUBTREE;
		}

		private static boolean notInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
			return !annotationType.getName().startsWith("java.lang.annotation.");
		}
	}

	private static final class VisitedVisitor extends ForwardingVisitor {
		private final Set<Annotation> visited = new HashSet<>();

		private VisitedVisitor(Visitor delegate) {
			super(delegate);
		}

		@Override
		public VisitorResult visitAnnotation(Annotation candidate) {
			if (visited.add(candidate)) {
				return delegate().visitAnnotation(candidate);
			} else {
				return VisitorResult.SKIP_SUBTREE;
			}
		}
	}

	private static final class DirectAnnotationVisitor<A extends Annotation> extends ForwardingVisitor {
		private final Class<A> annotationType;
		private final Function<? super A, ? extends VisitorResult> next;

		private DirectAnnotationVisitor(Class<A> annotationType, Function<? super A, ? extends VisitorResult> next, Visitor delegate) {
			super(delegate);
			this.annotationType = annotationType;
			this.next = next;
		}

		@Override
		public VisitorResult visitAnnotatedElement(AnnotatedElement element) {
			// Directly annotated?
			final A annotation = element.getDeclaredAnnotation(annotationType);
			if (annotation != null) {
				return next.apply(annotation);
			}
			return delegate().visitAnnotatedElement(element);
		}
	}

	private static final class SingleAnnotationVisitor<A extends Annotation> implements SimpleVisitor {
		private final Class<A> annotationType;
		private final Function<? super A, ? extends VisitorResult> next;

		private SingleAnnotationVisitor(Class<A> annotationType, Function<? super A, ? extends VisitorResult> next) {
			this.annotationType = annotationType;
			this.next = next;
		}

		public VisitorResult visitAnnotation(Annotation candidate) {
			final Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
			// 1) Exact match
			if (candidateAnnotationType.equals(annotationType)) {
				return next.apply(annotationType.cast(candidate));
			}

			// 2) Continue to meta-annotation
			else {
				return VisitorResult.CONTINUE;
			}
		}
	}

	private static class DefaultAnnotationFinder<A extends Annotation> implements AnnotationFinder<A> {
		private final Factory factory;
		private final Function<List<A>, Visitor> visitorFactory;

		private DefaultAnnotationFinder(Factory factory, Function<List<A>, Visitor> visitorFactory) {
			this.factory = factory;
			this.visitorFactory = visitorFactory;
		}

		@Override
		public Result<A> findOn(@Nullable AnnotatedElement element) {
			final List<A> result = new ArrayList<>();
			new AnnotationTree(factory.create(element)).accept(visitorFactory.apply(result));
			return new Result<>(result);
		}

		private static final class Result<A extends Annotation> implements AnnotationFinder.Result<A> {
			private final Collection<A> annotations;

			public Result(Collection<A> annotations) {
				this.annotations = annotations;
			}

			@Override
			public Iterator<A> iterator() {
				return annotations.iterator();
			}
		}
	}
}
