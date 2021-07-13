package dev.nokee.model.core;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.Cast.uncheckedCast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelSpecTest implements ModelSpecTester<Object> {
	@Test
	void throwsExceptionIfAndSpecProjectionTypeIsNotAssignableToFirstSpec() {
		val ex = assertThrows(IllegalArgumentException.class,
			() -> specOf(String.class).and(uncheckedCast("forcing compile compatibility", specOf(Object.class))));
		assertThat(ex.getMessage(), is("projection type 'java.lang.Object' needs to be the same or subtype of 'java.lang.String'"));
	}

	@Test
	void canCreateAndSpecWhenProjectionTypeIsAssignableToFirstSpec() {
		val spec = assertDoesNotThrow(() -> specOf(Object.class).and(specOf(String.class)));
		assertThat(spec, isA(ModelSpec.class));
	}

	@Test
	void andSpecHasSecondSpecProjectionType() {
		assertThat(specOf(Object.class).and(specOf(String.class)).getProjectionType(), is(String.class));
	}

	@Test
	void canCreateOrSpecWhenProjectionTypeIsNoAssignableToFirstSpec() {
		val spec = assertDoesNotThrow(() -> specOf(String.class).or(specOf(Object.class)));
		assertThat(spec, isA(ModelSpec.class));
	}

	private static final ModelProjection TRUE_OBJECT = Mockito.mock(ModelProjection.class);
	private static final ModelProjection FALSE_OBJECT = Mockito.mock(ModelProjection.class);

	@Override
	public ModelSpec<Object> createSubject() {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection projection) {
				return projection == TRUE_OBJECT;
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}

	@Override
	public ModelProjection trueObject() {
		return TRUE_OBJECT;
	}

	@Override
	public ModelProjection falseObject() {
		return FALSE_OBJECT;
	}

	@Override
	public ModelSpec<Object> alwaysTrueSpec() {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection projection) {
				return true;
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}

	@Override
	public ModelSpec<Object> alwaysFalseSpec() {
		return new ModelSpec<Object>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection projection) {
				return false;
			}

			@Override
			public Class<Object> getProjectionType() {
				return Object.class;
			}
		};
	}

	private static <T> ModelSpec<T> specOf(Class<T> type) {
		return new ModelSpec<T>() {
			@Override
			public boolean isSatisfiedBy(ModelProjection node) {
				return true;
			}

			@Override
			public Class<T> getProjectionType() {
				return type;
			}

			@Override
			public String toString() {
				return "test spec for '" + type.getCanonicalName() + "'";
			}
		};
	}
}
