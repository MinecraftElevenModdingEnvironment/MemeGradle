
package net.earthcomputer.meme.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
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
	public MemeGradlePlugin() {
		this((Class<M>) MemeExtension.class);
	}

	public MemeGradlePlugin(Class<M> memeExtensionClass) {
		this.memeExtensionClass = memeExtensionClass;
	}

	@Override
	public void apply(Project project) {
		applyPlugins(project);
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
		project.getBuildscript().getRepositories().jcenter();
		project.getBuildscript().getDependencies().add("classpath",
				"com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.2");

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
		// version tasks
		project.task("bumpMajor").doFirst(task -> bumpVersion(project, "Major"));
		project.task("bumpMinor").doFirst(task -> bumpVersion(project, "Minor"));
		project.task("bumpRevision").doFirst(task -> bumpVersion(project, "Revision"));
		Task bumpVersion = project.task("bumpBuildNumber").doFirst(task -> bumpVersion(project, "BuildNumber"));
		project.getTasks().getByName("assemble").dependsOn(bumpVersion);
		
		// sources jar task
		sourcesJarTask = project.getTasks().create("sourcesJar", Jar.class);
		sourcesJarTask.dependsOn("classes");
		sourcesJarTask.setClassifier("sources");
		sourcesJarTask.from(
				project.getExtensions().findByType(SourceSetContainer.class).getByName("main").getAllSource());
		project.getArtifacts().add("sources", sourcesJarTask);
	}

	protected void afterEvaluate(Project project) {
		MemeExtension memeExt = getMemeExtension(project);
		MavenPublication memePublication;

		// meme configuration
		{
			project.getConfigurations().create("memeConfiguration");
		}
		// source compatability
		{
			JavaPluginConvention javaPluginConvention = project.getConvention().findByType(JavaPluginConvention.class);
			javaPluginConvention.setSourceCompatibility(JavaVersion.VERSION_1_8);
			javaPluginConvention.setTargetCompatibility(JavaVersion.VERSION_1_8);
		}
		// maven publish
		{
			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
			memePublication = publishing.getPublications().create("memePublication", MavenPublication.class);
			memePublication.setArtifactId(memeExt.getArtifactId());
			memePublication.from(project.getComponents().getByName("java"));
			memePublication.artifact(sourcesJarTask);
		}
		// bintray
		{
			BintrayExtension bintray = project.getExtensions().getByType(BintrayExtension.class);
			bintray.setUser("bintrayUser");
			bintray.setKey("bintrayKey");
			bintray.setDryRun(false);
			bintray.setPublish(false);
			PackageConfig pkg = bintray.getPkg();
			pkg.setRepo("meme");
			pkg.setName(memeExt.getBintrayPackageName());
			bintray.setConfigurations("memeConfiguration");
			bintray.setPublications("memePublication");
		}
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