/*
 * Copyright 2022 the original author or authors.
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
package nokeebuild.signing;

import org.gradle.api.Action;

final class Once<T> implements Action<T> {
	private final Action<? super T> delegate;
	private boolean alreadyExecuted = false;

	Once(Action<? super T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(T t) {
		if (!alreadyExecuted) {
			try {
				delegate.execute(t);
			} finally {
				alreadyExecuted = true;
			}
		}
	}
}
