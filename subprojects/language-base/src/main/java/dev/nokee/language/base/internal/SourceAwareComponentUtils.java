/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.View;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public class SourceAwareComponentUtils {
	@SuppressWarnings("unchecked")
	public static FunctionalSourceSet sourceViewOf(Object target) {
		if (target instanceof SourceAwareComponent) {
			val sources = ((SourceAwareComponent<?>) target).getSources();
			if (sources instanceof FunctionalSourceSet) {
				return (FunctionalSourceSet) sources;
			} else if (sources instanceof SourceView) {
				return new FunctionalSourceSetAdapter((SourceView<LanguageSourceSet>) sources);
			} else {
				throw new UnsupportedOperationException(String.format("Unknown source type '%s'.", sources.getClass().getSimpleName()));
			}
		} else if (target instanceof FunctionalSourceSet) {
			return (FunctionalSourceSet) target;
		} else {
			val node = ModelNodes.of(target);
			if (ModelNodeUtils.hasDescendant(node, "sources")) {
				return ModelNodeUtils.get(ModelNodeUtils.getDescendant(node, "sources"), FunctionalSourceSet.class);
			}
		}
		throw new UnsupportedOperationException("Target '" + target + "' is not source aware or a language source set view itself.");
	}

	private static final class FunctionalSourceSetAdapter implements FunctionalSourceSet {
		private final SourceView<LanguageSourceSet> delegate;

		private FunctionalSourceSetAdapter(SourceView<LanguageSourceSet> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void configure(String name, Action<? super LanguageSourceSet> action) {
			named(name, LanguageSourceSet.class).configure(action);
		}

		@Override
		public <S extends LanguageSourceSet> void configure(String name, Class<S> type, Action<? super S> action) {
			named(name, type).configure(action);
		}

		@Override
		public <S extends LanguageSourceSet> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
			return ((SourceViewAdapter<LanguageSourceSet>) delegate).named(name, type);
		}

		@Override
		public void configureEach(Action<? super LanguageSourceSet> action) {
			delegate.configureEach(action);
		}

		@Override
		public <S extends LanguageSourceSet> void configureEach(Class<S> type, Action<? super S> action) {
			delegate.configureEach(type, action);
		}

		@Override
		public void configureEach(Spec<? super LanguageSourceSet> spec, Action<? super LanguageSourceSet> action) {
			delegate.configureEach(spec, action);
		}

		@Override
		public <S extends LanguageSourceSet> View<S> withType(Class<S> type) {
			return delegate.withType(type);
		}

		@Override
		public Provider<Set<LanguageSourceSet>> getElements() {
			return delegate.getElements();
		}

		@Override
		public Set<LanguageSourceSet> get() {
			return delegate.get();
		}

		@Override
		public <S> Provider<List<S>> map(Transformer<? extends S, ? super LanguageSourceSet> mapper) {
			return delegate.map(mapper);
		}

		@Override
		public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super LanguageSourceSet> mapper) {
			return delegate.flatMap(mapper);
		}

		@Override
		public Provider<List<LanguageSourceSet>> filter(Spec<? super LanguageSourceSet> spec) {
			return delegate.filter(spec);
		}

		@Override
		public void whenElementKnown(Action<? super KnownDomainObject<LanguageSourceSet>> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void whenElementKnown(@SuppressWarnings("rawtypes") Closure closure) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <S extends LanguageSourceSet> void whenElementKnown(Class<S> type, Action<? super KnownDomainObject<S>> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <S extends LanguageSourceSet> void whenElementKnown(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
			throw new UnsupportedOperationException();
		}
	}
}
