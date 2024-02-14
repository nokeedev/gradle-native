/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.model.internal.decorators;

import dev.nokee.model.internal.ModelObject;
import org.gradle.api.NamedDomainObjectProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ObjectTypeVisitor<R> {
	private final Visitor<R> visitor;

	public ObjectTypeVisitor(Visitor<R> visitor) {
		this.visitor = visitor;
	}

	@SuppressWarnings("unchecked")
	public R visit(Type objectType) {
		if (objectType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) objectType).getRawType();
			assert rawType instanceof Class;
			if (NamedDomainObjectProvider.class.isAssignableFrom((Class<?>) rawType)) {
				assert ((ParameterizedType) objectType).getActualTypeArguments().length == 1;
				assert ((ParameterizedType) objectType).getActualTypeArguments()[0] instanceof Class;
				Class<Object> elementType = (Class<Object>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return visitor.visitElementType(elementType);
			} else if (ModelObject.class.isAssignableFrom((Class<?>) rawType)) {
				assert ((ParameterizedType) objectType).getActualTypeArguments().length == 1;
				assert ((ParameterizedType) objectType).getActualTypeArguments()[0] instanceof Class;
				Class<Object> elementType = (Class<Object>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return visitor.visitElementType(elementType);
			} else {
				throw new UnsupportedOperationException(rawType + " -- " + objectType.getTypeName());
			}
		} else {
			assert objectType instanceof Class;
			Class<Object> elementType = (Class<Object>) objectType;
			return visitor.visitElementTypeOrObjectType(elementType);
		}
	}


	public interface Visitor<R> {
		R visitElementType(Class<?> elementType);
		R visitElementTypeOrObjectType(Class<?> type);
	}
}
