package net.earthcomputer.meme.gradle;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;

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
		project.getDependencies().add("enigmaDownload", "net.earthcomputer.meme:enigma:1.0.0.1");

		super.addTasks(project);
		Task downloadClient = project.getTasks().create("downloadClient", DownloadClient.class, task -> {
			task.setTo(new File(project.getBuildDir(), "process/rawClient.jar"));
		});
		Task downloadServer = project.getTasks().create("downloadServer", DownloadServer.class, task -> {
			task.setTo(new File(project.getBuildDir(), "process/rawServer.jar"));
		});
		Task mergeJars = project.getTasks().create("mergeJars", MergeJars.class, task -> {
			task.setClientInput(downloadClient);
			task.setServerInput(downloadServer);
			task.setOutput(new File(project.getBuildDir(), "process/obfMerged.jar"));
		});
		Task downloadEnigma = project.getTasks().create("downloadEnigma", Copy.class, task -> {
			task.from(enigmaDownloadConfig);
			task.into(new File(project.getBuildDir(), "process"));
			task.rename(".+\\.jar", "enigma.jar");
		});
		project.getTasks().create("enigma", JavaExec.class, task -> {
			task.dependsOn(mergeJars, downloadEnigma);
			task.executable(new File(project.getBuildDir(), "process/enigma.jar"));
			task.args(new File(project.getBuildDir(), "process/obfMerged.jar"), project.file("mappings.mmap"));
		});
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
