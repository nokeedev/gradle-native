/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;

/**
 * A component with variants.
 *
 * @param <T> type of the component dependencies
 * @since 0.4
 */
public interface VariantAwareComponent<T extends Variant> {
	/**
	 * Configure the variants of this component.
	 *
	 * @return a {@link ComponentVariants}, never null.
	 */
	VariantView<T> getVariants();

	/**
	 * Configures the component variants using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 */
	void variants(Action<? super VariantView<T>> action);

	/** @see #variants(Action) */
	void variants(@ClosureParams(value = SimpleType.class, options = "dev.nokee.platform.base.VariantView") @DelegatesTo(value = VariantView.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}
