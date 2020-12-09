package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelType_SupertypeTest {
	//region class
	@Test
	void canGetSupertypeOfClassExtendingAnother() {
		assertThat(of(Concrete.class).getSupertype(), optionalWithValue(equalTo(of(Base.class))));
	}

	@Test
	void canGetSupertypeOfClassExtendingOnlyObject() {
		assertThat(of(Base.class).getSupertype(), optionalWithValue(equalTo(of(Object.class))));
	}

	@Test
	void canSupertypeOfObjectClassIsAbsent() {
		assertThat(of(Object.class).getSupertype(), emptyOptional());
	}

	static class Base {}

	static class Concrete extends Base {}
	//endregion

	//region interface
	@Test
	void canSupertypeOfInterfaceIsAbsent() {
		assertThat(of(MyList.class).getSupertype(), emptyOptional());
	}

	interface MyList {}
	//endregion

	//region generic class
	@Test
	void canGetGenericSupertypeOfSpecificParameterizedType() {
		assertThat(of(StringList.class).getSupertype(),
			optionalWithValue(equalTo(of(new TypeOf<BaseList<String>>() {}))));
	}

	@Test
	void canGetGenericSupertypeOfGenericParameterizedType() {
		assertThat(of(new TypeOf<BaseList<Integer>>() {}).getSupertype(),
			optionalWithValue(equalTo(of(new TypeOf<BaseIterable<Integer>>() {}))));
	}

	static class BaseIterable<T> {}

	static class BaseList<T> extends BaseIterable<T> {}

	static class StringList extends BaseList<String> {}
	//endregion

	//region generic interface
	@Test
	void canGenericSupertypeOfParameterizedTypeIsAbsent() {
		assertThat(of(new TypeOf<IList<String>>() {}).getSupertype(), emptyOptional());
	}

	interface ICollection<T> {}

	interface IList<T> extends ICollection<T> {}
	//endregion
}
