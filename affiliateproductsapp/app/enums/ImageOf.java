package enums;

public enum ImageOf {
	
	PROFILE("Profile"),
	MEDIA("Media"),
	BRAND("Brand"), 
	SELLER("Seller"), 
	USER("User"),
	PRODUCT("Product"),
	USERPROFILE("User_Profile"),
	POST("Media"),
	ESSENTIAL("Essential"),
	CATEGORY("Category"),
	BANNER("Banner");
	
	
	public final String type;

	ImageOf(String type) {
		this.type = type;
	}
	
}
