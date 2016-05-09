package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.List;
import java.util.Set;

import enums.SellerEnum;

public class ProductSynchroniserFactory {

	public static ProductSynchroniser getProductSyncrhoniser(String advertiserName, Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUList) {

		ProductSynchroniser productSynchroniser = null;
		if (tsvBasedProcess) {
			if (advertiserName.equalsIgnoreCase(SellerEnum.BANANAREPUBLIC.sellerName)) {
				productSynchroniser = new BRProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.BLUEFLY.sellerName)) {
				productSynchroniser = new BlueflyProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.SURLATABLE.sellerName)) {
				productSynchroniser = new SurLaProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.CHICCO.sellerName)) {
				productSynchroniser = new ChiccoProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.UGG.sellerName)) {
				productSynchroniser = new UGGProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else {
				productSynchroniser = new DefaultProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			}
		} else {
			if (advertiserName.equalsIgnoreCase(SellerEnum.BANANAREPUBLIC.sellerName)) {
				productSynchroniser = new BRProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.BLUEFLY.sellerName)) {
				productSynchroniser = new BlueflyProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.SURLATABLE.sellerName)) {
				productSynchroniser = new SurLaProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.CHICCO.sellerName)) {
				productSynchroniser = new ChiccoProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else if (advertiserName.equalsIgnoreCase(SellerEnum.UGG.sellerName)) {
				productSynchroniser = new UGGProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList);
			} else {
				productSynchroniser = new DefaultProductSynchroniser(advertiserId, inputFile, tsvBasedProcess,
						productSKUList); // 10 14
			}
		}
		return productSynchroniser;
	}
}
