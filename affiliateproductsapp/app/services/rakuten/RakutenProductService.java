package services.rakuten;

import java.util.Map;

import models.rakuten.RakutenProduct;

public interface RakutenProductService {

	void createOrUpdate(RakutenProduct rakutenProduct, Map<String, String> colorSKUMap);

	// For Rakuten Super Feed, like Walmart
	void createOrUpdate(RakutenProduct rakutenProduct, String threadName);
}
