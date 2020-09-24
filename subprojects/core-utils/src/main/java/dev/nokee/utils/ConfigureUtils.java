package dev.nokee.utils;

import org.gradle.api.internal.provider.AbstractProperty;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Describables;

import java.util.List;
import java.util.Set;

public final class ConfigureUtils {
	private ConfigureUtils() {}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(Property<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the set property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(SetProperty<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the list property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(ListProperty<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param propertyName the property name to use in the display name.
	 * @param <T> the type of the property.
	 * @return self
	 */
	@SuppressWarnings("unchecked")
	public static <T> Property<T> configureDisplayName(Property<T> self, String propertyName) {
		((AbstractProperty<T>) self).attachDisplayName(Describables.of("property '" + propertyName + "'"));
		return self;
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param propertyName the property name to use in the display name.
	 * @param <T> the type of the property.
	 * @return self
	 */
	@SuppressWarnings("unchecked")
	public static <T> SetProperty<T> configureDisplayName(SetProperty<T> self, String propertyName) {
		((AbstractProperty<Set<T>>) self).attachDisplayName(Describables.of("property '" + propertyName + "'"));
		return self;
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param propertyName the property name to use in the display name.
	 * @param <T> the type of the property.
	 * @return self
	 */
	@SuppressWarnings("unchecked")
	public static <T> ListProperty<T> configureDisplayName(ListProperty<T> self, String propertyName) {
		((AbstractProperty<List<T>>) self).attachDisplayName(Describables.of("property '" + propertyName + "'"));
		return self;
	}
}
