package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeRequest;
import dev.nokee.ide.base.internal.IdeRequestAction;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;

// TODO: Converge XcodeIdeRequest, XcodeIdeBridge and XcodeIdePropertyAdapter. All three have overlapping responsibilities.
//  Specifically for XcodeIdeBridge, we may want to attach the product sync task directly to the XcodeIde* model an convert the lifecycle task type to Task.
//  It would make the bridge task more dummy and open for further customization of the Xcode delegation by allowing configuring the bridge task.
// TODO: XcodeIdeRequest should convert the action string/null to an XcodeIdeAction enum
public abstract class VisualStudioIdeRequest implements IdeRequest {
	private final String taskName;
	private final VisualStudioIdePropertyAdapter properties;

	@Inject
	public VisualStudioIdeRequest(String taskName) {
		this.taskName = taskName;
		this.properties = getObjects().newInstance(VisualStudioIdePropertyAdapter.class);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();


	@Override
	public String getTaskName() {
		return taskName;
	}

	@Override
	public IdeRequestAction getAction() {
		return properties.getAction().map(IdeRequestAction::valueOf).get();
	}

	public String getProjectName() {
		return properties.getProjectName().get();
	}

	public String getConfiguration() {
		return properties.getConfiguration().get();
	}

	public String getPlatformName() {
		return properties.getPlatformName().get();
	}

	public Directory getOutputDirectory() {
		return getObjects().directoryProperty().fileProvider(properties.getOutputDirectory().map(File::new)).get();
	}

	public String getGradleIdeProjectName() {
		return properties.getGradleIdeProjectName().get();
	}
}
