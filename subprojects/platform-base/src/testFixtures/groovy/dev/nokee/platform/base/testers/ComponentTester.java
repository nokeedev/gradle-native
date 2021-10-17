package dev.nokee.platform.base.testers;

import dev.nokee.platform.base.Component;

public interface ComponentTester<T extends Component> {
	T subject();
}
