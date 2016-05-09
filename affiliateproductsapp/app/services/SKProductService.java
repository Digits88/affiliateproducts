package services;

import models.Product;
import models.searskmart.SearsKmartProduct;

public interface SKProductService {

	void createOrUpdateSK(SearsKmartProduct skProduct);
}
