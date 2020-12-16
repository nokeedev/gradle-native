package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.untyped;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelType_UntypedTest {
	@Test
	void untypedModelTypeIsObjectType() {
		assertThat(untyped(), equalTo(of(Object.class)));
	}

	@Test
	void untypedModelTypeIsNotParameterized() {
		assertThat(untyped().isParameterized(), equalTo(false));
	}
}
