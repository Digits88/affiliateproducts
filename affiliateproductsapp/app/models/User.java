package models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.libs.Codec;

@Entity
@Table(name = "LIFESTYLE_USERS")
@NamedQueries({ @NamedQuery(name = "JPQL_GET_USER_IDS", query = "SELECT u.id FROM User u order by u.id") })
public class User extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = User.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Required
	@Column(name = "email")
	private String email;

	@Required
	@Column(name = "password")
	private String password;

	@Column(name = "display_name")
	public String displayName;

	@Column(name = "profile_picture")
	private String profilePicture;
	
	@OneToOne(mappedBy="user")
	public UserInstagram userInstagram;

	public UserInstagram getUserInstagram() {
		return userInstagram;
	}

	public void setUserInstagram(UserInstagram userInstagram) {
		this.userInstagram = userInstagram;
	}

	public static boolean exists(String email) {
		return byEmail(email) != null;
	}

	public static User byEmail(String email) {
		return User.find("byEmail", email).first();
	}

	public static User byResetPasswordCode(String resetPasswordCode) {
		return User.find("byResetPasswordCode", resetPasswordCode).first();
	}

	public static User byConfirmRegistrationCode(String confirmRegistrationCode) {
		return User.find("byConfirmRegistrationCode", confirmRegistrationCode).first();
	}

	public static User byPassword(String password) {
		return User.find("byPassword", password).first();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", password=" + password + ", displayName=" + displayName
				+ ", profilePicture=" + profilePicture + "]";
	}

}
