package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.core.ModelProjection;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

@Subject(UnmanagedInstanceModelProjection.class)
class UnmanagedInstanceModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(UnmanagedInstanceModelProjection.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val instance = new Object();
		new EqualsTester()
			.addEqualityGroup(UnmanagedInstanceModelProjection.of(instance), UnmanagedInstanceModelProjection.of(instance))
			.addEqualityGroup(UnmanagedInstanceModelProjection.of(new Object()))
			.testEquals();
	}

	@Override
	protected ModelProjection createSubject(Class<?> type) {
		try {
			return UnmanagedInstanceModelProjection.of(type.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			return ExceptionUtils.rethrow(e);
		}
	}
}
