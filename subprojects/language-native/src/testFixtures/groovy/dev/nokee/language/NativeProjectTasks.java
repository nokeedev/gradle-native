package dev.nokee.language;

import java.util.List;

public interface NativeProjectTasks {
	String getBinary();

	List<String> getAllToBinary();

	String getCompile();

	String getObjects();

	String getLink();

	String getCreate();

	String getInstall();

	String getAssemble();

	List<String> getAllToObjects();

	List<String> getAllToLifecycleObjects();

	List<String> getAllToCreate();

	List<String> getAllToLink();

	List<String> getAllToLinkOrCreate();

	List<String> getAllToInstall();

	List<String> getAllToLifecycleAssemble();

	List<String> getAllToAssemble();

	List<String> getAllToAssembleWithInstall();

	// TODO: split to another interface for test suites
	List<String> getAllToTest();

	NativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily);

	NativeProjectTasks withLinkage(String linkage);

	NativeProjectTasks withProjectPath(String projectPath);

	NativeProjectTasks withComponentName(String componentName);

	NativeProjectTasks getForStaticLibrary();
}
