package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

public interface CoordinateTester<T> {
	Coordinate<T> createSubject();

	@Test
	default void hasAxis() {
		assertThat(createSubject().getAxis().getType(), equalTo(axisType(this)));
	}

	@Test
	default void hasValue() {
		assertThat(createSubject().getValue(), isA(axisType(this)));
	}

	static Class<?> axisType(CoordinateTester<?> thiz) {
		return (Class<?>) ((ParameterizedType) thiz.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
	}
}
