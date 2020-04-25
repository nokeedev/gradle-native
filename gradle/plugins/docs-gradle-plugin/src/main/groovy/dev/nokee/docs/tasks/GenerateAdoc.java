package dev.nokee.docs.tasks;

import com.google.common.collect.ImmutableSet;
import dev.nokee.docs.dsl.links.ClassLinkMetaData;
import dev.nokee.docs.dsl.source.ClassMetaDataUtil;
import dev.nokee.docs.dsl.source.model.ClassMetaData;
import dev.nokee.docs.model.ClassMetaDataRepository;
import dev.nokee.docs.model.SimpleClassMetaDataRepository;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateAdoc extends DefaultTask {
	public abstract RegularFileProperty getMetaDataFile();

	public abstract RegularFileProperty getPluginsMetaDataFile();

	public abstract DirectoryProperty getDestinationDirectory();

	public abstract RegularFileProperty getTemplateFile();

	@TaskAction
	private void generate() {
		SimpleClassMetaDataRepository<ClassMetaData> classRepository = new SimpleClassMetaDataRepository<ClassMetaData>();
		classRepository.load(getMetaDataFile().get().getAsFile());
		ClassMetaDataRepository<ClassLinkMetaData> linkRepository = new SimpleClassMetaDataRepository<ClassLinkMetaData>();
		//for every method found in class meta, create a javadoc link
		classRepository.each(new Action<ClassMetaData>() {
			@Override
			public void execute(ClassMetaData metaData) {
				linkRepository.put(metaData.getClassName(), new ClassLinkMetaData(metaData));
			}
		});


//		ClassMetaDataUtil.extractFromMetadata(getMetaDataFile().get().getAsFile(), ImmutableSet.of("**/internal/**"), classMetaData -> {
//			classMetaData.
//		});
//		ClassMetaDataUtil.extractFromMetadata(getMetaDataFile().getAsFile().get(), getExcludedPackages().get(), classMetaData -> simpleNames.put(classMetaData.getSimpleName(), classMetaData.getClassName()));
	}
}
