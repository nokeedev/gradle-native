package dev.nokee.runtime.base.internal;

import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;

import java.lang.reflect.Field;

public interface ProvideAttributes {
	static <T extends Named> ActionUtils.Action<AttributeContainer> of(T obj) {
//		if (obj instanceof ProvideAttributes) {
//			return ((ProvideAttributes) obj)::execute;
//		}
		return attributes -> {
			// TODO: Ensure only one attribute is provided...
			for (Field declaredField : obj.getClass().getSuperclass().getDeclaredFields()) {
				if (declaredField.getType().isAssignableFrom(Attribute.class)) {
					try {
						attributes.attribute((Attribute<T>) declaredField.get(null), obj);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
	}

	static <T> ActionUtils.Action<Configuration> attributesOf(T obj) {
		if (obj instanceof ProvideAttributes) {
			return it -> {
				if (it.isCanBeResolved() && !it.isCanBeConsumed()) {
					((ProvideAttributes) obj).forResolving(it.getAttributes());
				} else if (!it.isCanBeResolved() && it.isCanBeConsumed()) {
					((ProvideAttributes) obj).forConsuming(it.getAttributes());
				} else {
					throw new UnsupportedOperationException("");
				}
			};
		}
		return ActionUtils.doNothing();
	}

	static <T extends Named> Action<AttributeContainer> whenConsuming(T obj) {
		if (obj instanceof ProvideAttributes) {
			return ((ProvideAttributes) obj)::forConsuming;
		}
		throw new UnsupportedOperationException();
	}

	static <T extends Named> Action<AttributeContainer> whenResolving(T obj) {
		if (obj instanceof ProvideAttributes) {
			return ((ProvideAttributes) obj)::forResolving;
		}
		throw new UnsupportedOperationException();
	}

	void forConsuming(AttributeContainer attributes);
	void forResolving(AttributeContainer attributes);
}
