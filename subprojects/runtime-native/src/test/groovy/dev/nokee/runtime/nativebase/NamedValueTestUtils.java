/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase;

import com.google.common.reflect.TypeToken;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class NamedValueTestUtils {
	private NamedValueTestUtils() {}

	public static <T extends Named> Class<T> namedValueTypeUnderTest(NamedValueTester<T> self) {
		try {
			val createSubjectMethod = self.getClass().getMethod("createSubject", String.class);
			val returnType = createSubjectMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			return (Class<T>) resolvedReturnType.getType();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private static final int CONSTANT_MODIFIER = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
	public static Stream<Field> findAllPublicConstantFieldOf(Class<?> type) {
		return Arrays.stream(type.getDeclaredFields()).filter(isConstant());
	}

	public static Predicate<Field> fieldOf(Class<?> type) {
		return it -> type.isAssignableFrom(it.getType());
	}

	private static Predicate<Field> isConstant() {
		return it -> (CONSTANT_MODIFIER & it.getModifiers()) == CONSTANT_MODIFIER;
	}

	public static <T extends Named> CoordinateAxis<T> coordinateAxisUnderTest(NamedValueTester<T> self) {
		val type = namedValueTypeUnderTest(self);
		val fields = findAllPublicConstantFieldOf(type);
		val field = fields.filter(fieldOf(CoordinateAxis.class)).findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("No coordinate axis declared on %s.", type)));
		try {
			return (CoordinateAxis<T>) field.get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends Named> ActionUtils.Action<AttributeContainer> of(T obj) {
		return attributes -> {
			for (Field declaredField : obj.getClass().getSuperclass().getDeclaredFields()) {
				if (declaredField.getType().isAssignableFrom(Attribute.class)) {
					try {
						attributes.attribute((Attribute<T>) declaredField.get(null), obj);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
	}
}
