/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.core;

import com.google.common.collect.Iterators;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

import static java.util.Objects.requireNonNull;

/**
 * Represent a specific projection of a model node.
 *
 * @param <T>  the projection type
 */
// TODO: This may not be needed anymore...
@EqualsAndHashCode
public final class ModelIdentifier<T> implements DomainObjectIdentifier {
    private final ModelPath path;
    private final ModelType<T> type;

    private ModelIdentifier(ModelPath path, ModelType<T> type) {
        this.path = requireNonNull(path);
        this.type = requireNonNull(type);
    }

    public static <T> ModelIdentifier<T> of(String path, Class<T> type) {
        return new ModelIdentifier<>(ModelPath.path(path), ModelType.of(type));
    }

    public static <T> ModelIdentifier<T> of(ModelPath path, ModelType<T> type) {
        return new ModelIdentifier<>(path, type);
    }

    public ModelPath getPath() {
        return path;
    }

    public ModelType<T> getType() {
        return type;
    }

	@Override
	public Iterator<Object> iterator() {
		return Iterators.forArray(this);
	}
}
