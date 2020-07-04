package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeRequest;
import dev.nokee.ide.base.internal.IdeRequestAction;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.io.File;

// TODO: Converge XcodeIdeRequest, XcodeIdeBridge and XcodeIdePropertyAdapter. All three have overlapping responsibilities.
//  Specifically for XcodeIdeBridge, we may want to attach the product sync task directly to the XcodeIde* model an convert the lifecycle task type to Task.
//  It would make the bridge task more dummy and open for further customization of the Xcode delegation by allowing configuring the bridge task.
// TODO: XcodeIdeRequest should convert the action string/null to an XcodeIdeAction enum
public abstract class XcodeIdeRequest implements IdeRequest {
	private final XcodeIdePropertyAdapter properties;
	private final String taskName;

	@Inject
	public XcodeIdeRequest(String taskName) {
		this.taskName = taskName;
		this.properties = getObjects().newInstance(XcodeIdePropertyAdapter.class);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	public String getTaskName() {
		return taskName;
	}

	public IdeRequestAction getAction() {
		return properties.getAction().map(it -> {
			if (it.isEmpty()) {
				return IdeRequestAction.BUILD;
			}
			return IdeRequestAction.valueOf(it);
		}).get();
	}

	public String getProjectName() {
		return properties.getProjectName().get();
	}

	public String getTargetName() {
		return properties.getTargetName().get();
	}

	public String getConfiguration() {
		return properties.getConfiguration().get();
	}

	public Directory getBuiltProductsDirectory() {
		return getObjects().directoryProperty().fileValue(new File(properties.getBuiltProductsDir().get())).get();
	}
}
