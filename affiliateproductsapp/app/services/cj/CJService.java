package services.cj;

import java.util.List;

import models.cj.CJProduct;

public interface CJService {
	List<CJProduct> getProductsByAdvertiserId(String advertiserId);
}
