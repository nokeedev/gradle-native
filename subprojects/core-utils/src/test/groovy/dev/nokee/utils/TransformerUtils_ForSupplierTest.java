package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.ExecuteWith;
import lombok.val;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.internal.testing.ExecuteWith.called;
import static dev.nokee.internal.testing.ExecuteWith.supplier;
import static dev.nokee.utils.TransformerUtils.forSupplier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TransformerUtils_ForSupplierTest {
	@Test
	void alwaysCallSupplier() {
		val result = ExecuteWith.executeWith(supplier(it -> {
			val subject = forSupplier(it);
			subject.transform(new Object());
			subject.transform(new Object());
		}));
		assertThat(result, called(equalTo(2)));
	}

	@Test
	void ignoresInputValue() {
		assertThat(forSupplier(ofInstance(42)).transform(55), equalTo(42));
	}

	@Test
	void acceptNullInputValues() {
		val result = assertDoesNotThrow(() -> forSupplier(ofInstance(52)).transform(null));
		assertThat(result, equalTo(52));
	}

	@Test
	void canUseForSupplierWithLambda() {
		Transformer<String, Object> transformer1 = forSupplier(() -> "foo");
		Transformer<String, ?> transformer2 = forSupplier(() -> "foo");

		assertThat(transformer1.transform(null), equalTo("foo"));
		assertThat(transformer2.transform(null), equalTo("foo"));
	}

	@Test
	void canUseForSupplierWithSupplierInstance() {
		Supplier<? extends String> supplier3 = () -> "foo";
		Transformer<String, ?> transformer3 = forSupplier(supplier3);

		Supplier<String> supplier4 = () -> "foo";
		Transformer<String, ?> transformer4 = forSupplier(supplier4);

		assertThat(transformer3.transform(null), equalTo("foo"));
		assertThat(transformer4.transform(null), equalTo("foo"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(forSupplier(ofInstance(42)), forSupplier(ofInstance(42)))
			.addEqualityGroup(forSupplier(ofInstance(52)))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(forSupplier(ofInstance(42)),
			hasToString("TransformerUtils.forSupplier(Suppliers.ofInstance(42))"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(forSupplier(ofInstance(42)), isA(TransformerUtils.Transformer.class));
	}
}
