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

	List<String> getAllToCreate();

	List<String> getAllToLink();

	List<String> getAllToInstall();

	List<String> getAllToAssemble();

	List<String> getAllToAssembleWithInstall();

	NativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily);

	NativeProjectTasks withLinkage(String linkage);

	NativeProjectTasks getForStaticLibrary();
}
