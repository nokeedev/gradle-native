package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.ExecuteWith;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.internal.testing.ExecuteWith.called;
import static dev.nokee.internal.testing.ExecuteWith.supplier;
import static dev.nokee.utils.TransformerUtils.forSupplier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
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
}
