package dev.nokee.platform.base;

import dev.nokee.model.DomainObjectFactory;
import org.gradle.api.Action;

public interface ComponentContainer {
	<T extends Component> DomainObjectProvider<T> register(String name, Class<T> type);
	<T extends Component> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action);

	<U extends Component> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);
}
