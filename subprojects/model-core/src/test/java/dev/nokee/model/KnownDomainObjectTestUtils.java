package dev.nokee.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

final class KnownDomainObjectTestUtils {
	public static void realize(KnownDomainObject<?> knownObject) {
		knownObject.map(noOpTransformer()).get();
	}

	public static <T> Class<T> getType(KnownDomainObjectTester<T> self) {
		for (Type genericInterface : self.getClass().getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType && KnownDomainObjectTester.class.isAssignableFrom((Class<?>) ((ParameterizedType) genericInterface).getRawType())) {
				return (Class<T>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
			}
		}
		throw new UnsupportedOperationException();
	}
}
