package controllers.response;

import java.util.HashMap;
import java.util.Map;

public class AffiliateResponse {

	public enum Key {
		INIT_DATA, REDIRECT_TO_LOGIN, USER, USERS, MEDIA, MEDIAS, VISUALTAG, VISUALTAGS, PRIVATE_CHAT_REQUEST, ORDER, STATUS, MESSAGE, PRODUCT, PRODUCTS, COUNT, FEED, ODCCATEGORIES, ODCSERVICEREQUEST, SELLERS, IMAGE_URL, CATEGORY, USERPROFILEPICTUREURL, USERMEDIACONTENTS, PRIME_RELATED_PRODUCT, SK_SAME_PRODUCT, SK_SIMILAR_PRODUCT, ESSENTIALCONTENT 
	}

	private Map<String, Object> payload;

	public AffiliateResponse() {
		payload = new HashMap<String, Object>();
	}

	public AffiliateResponse add(Key key, Object value) {
		this.payload.put(key.name(), value);
		return this;
	}
}
