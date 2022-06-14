import dev.nokee.nvm.NokeeVersionManagementRepositoryExtension
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ExtensionAware

fun RepositoryHandler.nokee(): MavenArtifactRepository {
    return ExtensionAware::class.java.cast(this).extensions.getByType(NokeeVersionManagementRepositoryExtension::class.java).nokee()
}
