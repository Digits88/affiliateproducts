package models.image;

import java.io.Serializable;

// import enums.MediaType;

public class UserMediaContent implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BASE64DATA = "base64Data";
	public static final String STANDARD_RESOLUTION_WIDTH = "standardResolutionWidth";
	public static final String STANDARD_RESOLUTION_HEIGHT = "standardResolutionHeight";

	// MediaType mediaType;
	String base64Data;
	String videoLowBandWidthURL;
	Long videoLowBandWidthWidth;
	Long videoLowBandWidthHeight;
	String videoLowResolutionURL;
	Long videoLowResolutionWidth;
	Long videoLowResolutionHeight;
	String videoStandardResolutionURL;
	Long videoStandardResolutionHeight;
	Long videoStandardResolutionWidth;
	String standardResolutionURL;
	Long standardResolutionWidth;
	Long standardResolutionHeight;
	String lowResolutionURL;
	Long lowResolutionWidth;
	Long lowResolutionHeight;
	String thumbnailURL;
	Long thumbnailWidth;
	Long thumbnailHeight;

	/*
	 * public MediaType getMediaType() { return mediaType; }
	 * 
	 * public void setMediaType(MediaType mediaType) { this.mediaType =
	 * mediaType; }
	 */

	public String getBase64Data() {
		return base64Data;
	}

	public void setBase64Data(String base64Data) {
		this.base64Data = base64Data;
	}

	public String getVideoLowBandWidthURL() {
		return videoLowBandWidthURL;
	}

	public void setVideoLowBandWidthURL(String videoLowBandWidthURL) {
		this.videoLowBandWidthURL = videoLowBandWidthURL;
	}

	public Long getVideoLowBandWidthWidth() {
		return videoLowBandWidthWidth;
	}

	public void setVideoLowBandWidthWidth(Long videoLowBandWidthWidth) {
		this.videoLowBandWidthWidth = videoLowBandWidthWidth;
	}

	public Long getVideoLowBandWidthHeight() {
		return videoLowBandWidthHeight;
	}

	public void setVideoLowBandWidthHeight(Long videoLowBandWidthHeight) {
		this.videoLowBandWidthHeight = videoLowBandWidthHeight;
	}

	public String getVideoLowResolutionURL() {
		return videoLowResolutionURL;
	}

	public void setVideoLowResolutionURL(String videoLowResolutionURL) {
		this.videoLowResolutionURL = videoLowResolutionURL;
	}

	public Long getVideoLowResolutionWidth() {
		return videoLowResolutionWidth;
	}

	public void setVideoLowResolutionWidth(Long videoLowResolutionWidth) {
		this.videoLowResolutionWidth = videoLowResolutionWidth;
	}

	public Long getVideoLowResolutionHeight() {
		return videoLowResolutionHeight;
	}

	public void setVideoLowResolutionHeight(Long videoLowResolutionHeight) {
		this.videoLowResolutionHeight = videoLowResolutionHeight;
	}

	public String getVideoStandardResolutionURL() {
		return videoStandardResolutionURL;
	}

	public void setVideoStandardResolutionURL(String videoStandardResolutionURL) {
		this.videoStandardResolutionURL = videoStandardResolutionURL;
	}

	public Long getVideoStandardResolutionHeight() {
		return videoStandardResolutionHeight;
	}

	public void setVideoStandardResolutionHeight(Long videoStandardResolutionHeight) {
		this.videoStandardResolutionHeight = videoStandardResolutionHeight;
	}

	public Long getVideoStandardResolutionWidth() {
		return videoStandardResolutionWidth;
	}

	public void setVideoStandardResolutionWidth(Long videoStandardResolutionWidth) {
		this.videoStandardResolutionWidth = videoStandardResolutionWidth;
	}

	public String getStandardResolutionURL() {
		return standardResolutionURL;
	}

	public void setStandardResolutionURL(String standardResolutionURL) {
		this.standardResolutionURL = standardResolutionURL;
	}

	public Long getStandardResolutionWidth() {
		return standardResolutionWidth;
	}

	public void setStandardResolutionWidth(Long standardResolutionWidth) {
		this.standardResolutionWidth = standardResolutionWidth;
	}

	public Long getStandardResolutionHeight() {
		return standardResolutionHeight;
	}

	public void setStandardResolutionHeight(Long standardResolutionHeight) {
		this.standardResolutionHeight = standardResolutionHeight;
	}

	public String getLowResolutionURL() {
		return lowResolutionURL;
	}

	public void setLowResolutionURL(String lowResolutionURL) {
		this.lowResolutionURL = lowResolutionURL;
	}

	public Long getLowResolutionWidth() {
		return lowResolutionWidth;
	}

	public void setLowResolutionWidth(Long lowResolutionWidth) {
		this.lowResolutionWidth = lowResolutionWidth;
	}

	public Long getLowResolutionHeight() {
		return lowResolutionHeight;
	}

	public void setLowResolutionHeight(Long lowResolutionHeight) {
		this.lowResolutionHeight = lowResolutionHeight;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	public Long getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(Long thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public Long getThumbnailHeight() {
		return thumbnailHeight;
	}

	public void setThumbnailHeight(Long thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	@Override
	public String toString() {
		return "UserMediaContent [base64Data= " + base64Data + ", mediaType=" // + mediaType
				+ ", standardResolutionURL=" + standardResolutionURL + ", standardResolutionWidth="
				+ standardResolutionWidth + ", standardResolutionHeight=" + standardResolutionHeight
				+ ", lowResolutionURL=" + lowResolutionURL + ", lowResolutionWidth=" + lowResolutionWidth
				+ ", lowResolutionHeight=" + lowResolutionHeight + ", thumbnailURL=" + thumbnailURL
				+ ", thumbnailWidth=" + thumbnailWidth + ", thumbnailHeight=" + thumbnailHeight + "]";
	}
}