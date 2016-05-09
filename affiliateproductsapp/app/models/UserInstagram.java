package models;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;

@Entity
@Table(name = "lifestyle_user_instagram")
public class UserInstagram extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = UserInstagram.class
			.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", unique = true)
	public User user;

	@Column(name = "instagram_user_id")
	private String instagramUserId;

	@Column(name = "instagram_access_token")
	private String instagramAccessToken;

	@Column(name = "instagram_user_name")
	public String instagramUserName;

	@Column(name = "instagram_bio")
	private String instagramBio;

	@Column(name = "instagram_website")
	private String instagramWebsite;

	@Column(name = "instagram_profile_picture")
	private String instagramProfilePicture;

	@Column(name = "instagram_full_name")
	private String instagramFullName;
	
	@Column(name = "local_instagram_profile_picture")
	private String localInstagramProfilePicture;

	public String getLocalInstagramProfilePicture() {
		return localInstagramProfilePicture;
	}

	public void setLocalInstagramProfilePicture(String localInstagramProfilePicture) {
		this.localInstagramProfilePicture = localInstagramProfilePicture;
	}


	@Override
	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getInstagramUserId() {
		return instagramUserId;
	}

	public void setInstagramUserId(String instagramUserId) {
		this.instagramUserId = instagramUserId;
	}

	public String getInstagramAccessToken() {
		return instagramAccessToken;
	}

	public void setInstagramAccessToken(String instagramAccessToken) {
		this.instagramAccessToken = instagramAccessToken;
	}

	public String getInstagramUserName() {
		return instagramUserName;
	}

	public void setInstagramUserName(String instagramUserName) {
		this.instagramUserName = instagramUserName;
	}

	public String getInstagramBio() {
		return instagramBio;
	}

	public void setInstagramBio(String instagramBio) {
		this.instagramBio = instagramBio;
	}

	public String getInstagramWebsite() {
		return instagramWebsite;
	}

	public void setInstagramWebsite(String instagramWebsite) {
		this.instagramWebsite = instagramWebsite;
	}

	public String getInstagramProfilePicture() {
		return instagramProfilePicture;
	}

	public void setInstagramProfilePicture(String instagramProfilePicture) {
		this.instagramProfilePicture = instagramProfilePicture;
	}

	public String getInstagramFullName() {
		return instagramFullName;
	}

	public void setInstagramFullName(String instagramFullName) {
		this.instagramFullName = instagramFullName;
	}

	public UserInstagram(User user, String instagramUserId,
			String instagramAccessToken, String instagramUserName,
			String instagramBio, String instagramWebsite,
			String instagramProfilePicture, String instagramFullName) {
		super();
		this.user = user;
		this.instagramUserId = instagramUserId;
		this.instagramAccessToken = instagramAccessToken;
		this.instagramUserName = instagramUserName;
		this.instagramBio = instagramBio;
		this.instagramWebsite = instagramWebsite;
		this.instagramProfilePicture = instagramProfilePicture;
		this.instagramFullName = instagramFullName;
	}

	@Override
	public String toString() {
		return "UserInstagram [id=" + id + ", user=" + user.getId()
				+ ", instagramUserId=" + instagramUserId
				+ ", instagramAccessToken=" + instagramAccessToken
				+ ", instagramUserName=" + instagramUserName
				+ ", instagramBio=" + instagramBio + ", instagramWebsite="
				+ instagramWebsite + ", instagramProfilePicture="
				+ instagramProfilePicture + ", instagramFullName="
				+ instagramFullName + "]";
	}

}
