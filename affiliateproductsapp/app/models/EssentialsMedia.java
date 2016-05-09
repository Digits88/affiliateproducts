package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import enums.MediaType;

@Entity
@Table(name = "lifestyle_essentials_media")
public class EssentialsMedia extends VersionedEntity {
	
	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = EssentialsMedia.class
			.getSimpleName();
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "media_type")
	private MediaType mediaType;
	
	@Column(name = "std_res_url")
	private String standardResolutionURL;

	@Column(name = "std_res_width")
	private Long standardResolutionWidth;

	@Column(name = "std_res_height")
	private Long standardResolutionHeight;

	@Column(name = "low_res_url")
	private String lowResolutionURL;

	@Column(name = "low_res_width")
	private Long lowResolutionWidth;

	@Column(name = "low_res_height")
	private Long lowResolutionHeight;

	@Column(name = "thumbnail_url")
	private String thumbnailURL;

	@Column(name = "thumbnail_width")
	private Long thumbnailWidth;

	@Column(name = "thumbnail_height")
	private Long thumbnailHeight;
	
	@Override
	public Long getId() {
		return id;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
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
		return "EssentialsMedia [id=" + id + ", mediaType=" + mediaType
				+ ", standardResolutionURL=" + standardResolutionURL
				+ ", standardResolutionWidth=" + standardResolutionWidth
				+ ", standardResolutionHeight=" + standardResolutionHeight
				+ ", lowResolutionURL=" + lowResolutionURL
				+ ", lowResolutionWidth=" + lowResolutionWidth
				+ ", lowResolutionHeight=" + lowResolutionHeight
				+ ", thumbnailURL=" + thumbnailURL + ", thumbnailWidth="
				+ thumbnailWidth + ", thumbnailHeight=" + thumbnailHeight + "]";
	}

}
