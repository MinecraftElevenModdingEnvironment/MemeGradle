package net.earthcomputer.meme.gradle;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;

import com.google.common.collect.ImmutableMap;

import net.earthcomputer.meme.gradle.tasks.DownloadClient;
import net.earthcomputer.meme.gradle.tasks.DownloadServer;
import net.earthcomputer.meme.gradle.tasks.MergeJars;

public class MemeGradleMemesanePlugin extends MemeGradlePlugin<MemeExtension> {

	@Override
	protected void applyPlugins(Project project) {
		project.getPluginManager().apply("maven-publish");
		project.getPluginManager().apply("com.jfrog.bintray");
	}

	@Override
	protected void addTasks(Project project) {
		Configuration enigmaDownloadConfig = project.getConfigurations().create("enigmaDownload");
		project.getRepositories().maven(repo -> {
			repo.setName("Cuchaz Custom Repository");
			repo.setUrl("http://maven.cuchazinteractive.com");
		});
		project.getDependencies().add("enigmaDownload", "net.earthcomputer.meme:enigma:1.0.0.9-meme");

		super.addTasks(project);
		Task downloadClient = project.getTasks().create("downloadClient", DownloadClient.class, task -> {
			task.setTo(new File(project.getBuildDir(), "process/rawClient.jar"));
		});
		Task downloadServer = project.getTasks().create("downloadServer", DownloadServer.class, task -> {
			task.setTo(new File(project.getBuildDir(), "process/rawServer.jar"));
		});
		Task mergeJars = project.getTasks().create("mergeJars", MergeJars.class, task -> {
			task.dependsOn(downloadClient, downloadServer);
			task.setClientInput(new File(project.getBuildDir(), "process/rawClient.jar"));
			task.setServerInput(new File(project.getBuildDir(), "process/rawServer.jar"));
			task.setOutput(new File(project.getBuildDir(), "process/obfMerged.jar"));
		});
		Task downloadEnigma = project.getTasks().create("downloadEnigma", Copy.class, task -> {
			task.from(enigmaDownloadConfig);
			task.into(new File(project.getBuildDir(), "process"));
		});
		project.getTasks().create("enigma", JavaExec.class, task -> {
			task.dependsOn(mergeJars, downloadEnigma);
			task.classpath(
					project.fileTree(ImmutableMap.of("dir", project.getBuildDir() + "/process", "include", "*.jar")));
			task.setMain("net.earthcomputer.meme.enigma.Main");
			task.setStandardOutput(System.out);
			task.setErrorOutput(System.err);
			task.args(new File(project.getBuildDir(), "process/obfMerged.jar"), project.file("mappings.mmap"));
		});
	}

	@Override
	protected void addVersionTasks(Project project) {
		project.task("bumpMajor").doFirst(task -> bumpVersion(project, "Major"));
		project.task("bumpMinor").doFirst(task -> bumpVersion(project, "Minor"));
		project.task("bumpRevision").doFirst(task -> bumpVersion(project, "Revision"));
		project.task("bumpBuildNumber").doFirst(task -> bumpVersion(project, "BuildNumber"));
	}

	@Override
	protected void addSourcesJarTask(Project project) {
	}

	@Override
	protected void setupMavenPublish(Project project, MemeExtension memeExt) {
		PublishingExtension publishing = project.getExtensions().findByType(PublishingExtension.class);
		MavenPublication memePublication = publishing.getPublications().create("memePublication",
				MavenPublication.class);
		memePublication.setArtifactId(memeExt.getArtifactId());
		memePublication.artifact("mappings.mmap");
	}

}
