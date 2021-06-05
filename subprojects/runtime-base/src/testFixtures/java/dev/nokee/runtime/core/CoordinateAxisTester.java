package dev.nokee.runtime.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public interface CoordinateAxisTester<T> {
	CoordinateAxis<T> createSubject();

	@Test
	default void hasType() {
		assertThat(createSubject().getType(), equalTo(valueType(this)));
	}

	static Class<?> valueType(CoordinateAxisTester<?> thiz) {
		return (Class<?>) ((ParameterizedType) thiz.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
	}

	@Test
	default void hasName() {
		assertThat(createSubject().getName(), notNullValue(String.class));
	}
}
