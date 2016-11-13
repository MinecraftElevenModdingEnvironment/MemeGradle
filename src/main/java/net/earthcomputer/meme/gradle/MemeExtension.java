package net.earthcomputer.meme.gradle;

import org.gradle.api.Project;

public class MemeExtension {

	private String artifactId;
	private String bintrayPackageName;

	public MemeExtension(Project project) {
		artifactId = project.getName();
		bintrayPackageName = artifactId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getBintrayPackageName() {
		return bintrayPackageName;
	}

	public void setBintrayPackageName(String bintrayPackageName) {
		this.bintrayPackageName = bintrayPackageName;
	}

}
