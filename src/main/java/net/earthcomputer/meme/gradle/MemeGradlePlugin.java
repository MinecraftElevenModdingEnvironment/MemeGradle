
package net.earthcomputer.meme.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;

import com.jfrog.bintray.gradle.BintrayExtension;
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig;

public class MemeGradlePlugin<M extends MemeExtension> implements Plugin<Project> {

	private Class<M> memeExtensionClass;
	private Jar sourcesJarTask;

	@SuppressWarnings("unchecked")
	@Inject
	public MemeGradlePlugin() {
		this((Class<M>) MemeExtension.class);
	}

	public MemeGradlePlugin(Class<M> memeExtensionClass) {
		this.memeExtensionClass = memeExtensionClass;
	}

	@Override
	public void apply(Project project) {
		applyPlugins(project);
		project.getConfigurations().create("memeConfiguration");
		addRepositories(project);

		// meme extension
		{
			try {
				project.getExtensions().add("meme",
						memeExtensionClass.getConstructor(Project.class).newInstance(project));
			} catch (Exception e) {
				throw new Error(e);
			}
		}

		addTasks(project);

		project.afterEvaluate(p -> afterEvaluate(p));

	}

	protected void applyPlugins(Project project) {
		project.getPluginManager().apply("java");
		project.getPluginManager().apply("eclipse");
		project.getPluginManager().apply("maven-publish");
		project.getPluginManager().apply("com.jfrog.bintray");
	}

	protected void addRepositories(Project project) {
		project.getRepositories().jcenter();
		project.getRepositories().maven(repo -> {
			repo.setName("memeRepo");
			repo.setUrl("https://dl.bintray.com/earthcomputer/meme/");
		});
	}

	protected void addTasks(Project project) {
		addVersionTasks(project);
		addSourcesJarTask(project);
	}

	protected void addVersionTasks(Project project) {
		project.task("bumpMajor").doFirst(task -> bumpVersion(project, "Major"));
		project.task("bumpMinor").doFirst(task -> bumpVersion(project, "Minor"));
		project.task("bumpRevision").doFirst(task -> bumpVersion(project, "Revision"));
		Task bumpVersion = project.task("bumpBuildNumber").doFirst(task -> bumpVersion(project, "BuildNumber"));
		project.getTasks().getByName("assemble").dependsOn(bumpVersion);
	}

	protected void addSourcesJarTask(Project project) {
		sourcesJarTask = project.getTasks().create("sourcesJar", Jar.class);
		sourcesJarTask.dependsOn("classes");
		sourcesJarTask.setClassifier("sources");
		SourceSetContainer sourceSetContainer = (SourceSetContainer) project.getProperties().get("sourceSets");
		sourcesJarTask.from(sourceSetContainer.getByName("main").getAllSource());
		project.getArtifacts().add("memeConfiguration", sourcesJarTask);
	}

	protected void afterEvaluate(Project project) {
		MemeExtension memeExt = getMemeExtension(project);
		setSourceCompatibility(project);
		setupMavenPublish(project, memeExt);
		setupBintray(project, memeExt);
	}

	protected void setSourceCompatibility(Project project) {
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) project.getProperties();
		properties.put("sourceCompatability", JavaVersion.VERSION_1_8);
		properties.put("targetCompatability", JavaVersion.VERSION_1_8);
	}

	protected void setupMavenPublish(Project project, MemeExtension memeExt) {
		PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
		MavenPublication memePublication = publishing.getPublications().create("memePublication",
				MavenPublication.class);
		memePublication.setArtifactId(memeExt.getArtifactId());
		memePublication.from(project.getComponents().getByName("java"));
		memePublication.artifact(sourcesJarTask);
	}

	protected void setupBintray(Project project, MemeExtension memeExt) {
		BintrayExtension bintray = project.getExtensions().getByType(BintrayExtension.class);
		bintray.setUser(project.property("bintrayUser").toString());
		bintray.setKey(project.property("bintrayKey").toString());
		bintray.setDryRun(false);
		bintray.setPublish(false);
		PackageConfig pkg = bintray.getPkg();
		pkg.setRepo("meme");
		pkg.setName(memeExt.getBintrayPackageName());
		bintray.setConfigurations("memeConfiguration");
		bintray.setPublications("memePublication");
	}

	private void bumpVersion(Project project, String versionType) {
		try {
			Properties props = new Properties();
			File propsFile = project.file("gradle.properties");
			props.load(new FileInputStream(propsFile));
			int nextBuildNum = Integer.parseInt(props.getProperty("version" + versionType)) + 1;
			props.setProperty("version" + versionType, String.valueOf(nextBuildNum));
			props.store(new FileOutputStream(propsFile), null);
		} catch (Exception e) {
			throw new RuntimeException("gradle.properties has invalid format");
		}
	}

	@SuppressWarnings("unchecked")
	public M getMemeExtension(Project project) {
		return (M) project.getExtensions().getByName("meme");
	}

}
