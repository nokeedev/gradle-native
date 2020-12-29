package dev.nokee.platform.base;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.BaseComponent;
import org.gradle.api.provider.Property;

public interface BaseNameAwareComponent extends Component {
	default Property<String> getBaseName() {
		return ModelNodes.of(this).get(BaseComponent.class).getBaseName();
	}
}
