package models.shopyourway;

import java.io.Serializable;

import com.google.common.base.Objects;

public class SywUser implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String CACHE_PREFIX = SywUser.class.getSimpleName();

	private Long id;
	private String name;
	private String profileUrl;
	private String imageUrl;
	private String email;

	public SywUser() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", getId())
				.add("name", getName())
				.add("profileUrl", getProfileUrl())
				.toString();
	}
}