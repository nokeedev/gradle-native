package dev.nokee.model;

import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.util.ExceptionUtils;

public abstract class AbstractDomainObjectViewConfigureEachTester<T> extends AbstractDomainObjectViewTester<T> {
	protected final <U> U when(ThrowingSupplier<U> executable) {
		U result = null;
		element("e0", getElementType());
		element("e1", getElementType()).get();
		element("e2", getSubElementType());
		element("e3", getSubElementType()).get();
		try {
			result = executable.get();
			element("e4", getElementType());
			element("e5", getElementType()).get();
			element("e6", getSubElementType());
			element("e7", getSubElementType()).get();
		} catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
		return result;
	}
}
