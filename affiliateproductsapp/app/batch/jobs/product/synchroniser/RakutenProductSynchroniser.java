package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import batch.jobs.AbstractBatchJob;
import constants.RakutenConstants;
import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import services.SKProductService;
import services.impl.DefaultSKProductService;
import services.rakuten.RakutenProductService;
import utils.log.Log;

@InjectSupport
public class RakutenProductSynchroniser extends AbstractBatchJob {

	@Inject
	protected static RakutenProductService rakutenProductService;

	private static Logger logger = Logger
			.getLogger(RakutenProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public Long advertiserId;
	public File inputFile;
	public Boolean tsvBasedProcess = false;
	public List<Set<String>> productSKUlist;
	public Map<String, String> colorSKUMap;

	public RakutenProductSynchroniser(Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUlist, Map<String, String> colorSKUMap) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.tsvBasedProcess = tsvBasedProcess;
		this.productSKUlist = productSKUlist;
		this.colorSKUMap = colorSKUMap;
	}

	@Override
	public void doJob() throws Exception {
		RakutenProductCreator rakutenProductCreator = new RakutenProductCreator(
				advertiserId, inputFile);
		List<RakutenProduct> rakutenProducts = rakutenProductCreator
				.createRakutenProducts();
		syncRakutenProducts(rakutenProducts);
	}

	private void syncRakutenProducts(List<RakutenProduct> rakutenProducts) {

		Set<String> productSKUs = new HashSet<String>();
		try {
			if (rakutenProducts.size() > 0) {
				for (RakutenProduct rakutenProduct : rakutenProducts) {
					rakutenProductService.createOrUpdate(rakutenProduct, colorSKUMap);
					if (rakutenProduct.getMerchantId() == RakutenConstants.NORDSTROM_ADVERTISERID) {
						productSKUs.add(rakutenProduct.getPrimaryCategory()
								+ "-" + rakutenProduct.getManufacturerName()
								+ "-" + rakutenProduct.getPartNumber());
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.SHOESCOM_ADVERTISERID) {
						productSKUs.add(rakutenProduct.getName());
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
						String color = colorSKUMap.get(rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName() + "-" + rakutenProduct.getSku());
						String sku = rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName() + "-" + color;
						productSKUs.add(sku);
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID) {
						String name = rakutenProduct.getName().split(",")[0];
						String partName = "";
						if(rakutenProduct.getPartNumber() != null && rakutenProduct.getPartNumber().trim().length() > 0) {
							partName = "-" + rakutenProduct.getPartNumber();
						}
						String cate = rakutenProduct.getPrimaryCategory();
						productSKUs.add(cate + "-" + name + partName);
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
						productSKUs.add(rakutenProduct.getPartNumber());
					} else {
						productSKUs.add(rakutenProduct.getSku());
					}
				}

				productSKUlist.add(productSKUs);

				logger.info(Log.message("productSKUlist size : "
						+ productSKUlist.size()));
				logger.info(Log
						.message("Successfully completed creating the products for seller : "
								+ inputFile + " <---- Update is done!"));

				logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in RakutenProductSynchroniser syncRakutenProducts : "
							+ inputFile + " : " + advertiserId + e.getMessage()));
			e.printStackTrace();
		}
	}
}
