package controllers.json;

import play.Play;

public class VersionJson {

	String buildNumber;
	String buildDate;
	Play.Mode appMode;
	String appId;

	public VersionJson appendBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
		return this;
	}

	public VersionJson appendBuildDate(String buildDate) {
		this.buildDate = buildDate;
		return this;
	}

	public VersionJson appendAppMode(Play.Mode appMode) {
		this.appMode = appMode;
		return this;
	}

	public VersionJson appendAppId(String appId) {
		this.appId = appId;
		return this;
	}
}
