package net.earthcomputer.meme.gradle;

import org.gradle.api.Project;

public class MemesaneExtension extends MemeExtension {

	private String enigmaVersion;
	
	public MemesaneExtension(Project project) {
		super(project);
	}

	public String getEnigmaVersion() {
		return enigmaVersion;
	}

	public void setEnigmaVersion(String enigmaVersion) {
		this.enigmaVersion = enigmaVersion;
	}

}
