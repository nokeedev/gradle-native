package dev.gradleplugins.exemplarkit;

import java.io.File;

public final class StepExecutionContext {
	private final File sandboxDirectory;
	private File currentWorkingDirectory;
	private Step currentStep;

	public StepExecutionContext(File currentWorkingDirectory) {
		this.sandboxDirectory = currentWorkingDirectory;
		this.currentWorkingDirectory = currentWorkingDirectory;
	}

	public File getSandboxDirectory() {
		return sandboxDirectory;
	}

	public Step getCurrentStep() {
		return currentStep;
	}

	StepExecutionContext forStep(Step step) {
		this.currentStep = step;
		return this;
	}

	public void setCurrentWorkingDirectory(File currentWorkingDirectory) {
		this.currentWorkingDirectory = currentWorkingDirectory;
	}

	public File getCurrentWorkingDirectory() {
		return currentWorkingDirectory;
	}

	public StepExecutionContext currentWorkingDirectory(File currentWorkingDirectory) {
		this.currentWorkingDirectory = currentWorkingDirectory;
		return this;
	}
}
