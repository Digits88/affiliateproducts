package batch.jobs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Product;
import models.Seller;

import org.apache.log4j.Logger;

import play.Play;
import repositories.Repository;
import utils.log.Log;

/**
 * 
 * Class to generate the product feed sheet from our Local DB for the platform
 * team usage
 * 
 */
@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class CreateProductFeedAsTSV extends AbstractBatchJob {

	@Transient
	public List<Long> sellerIds = null;

	private static Logger logger = Logger
			.getLogger(CreateProductFeedAsTSV.class);

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		try {
			List<Seller> sellerList = Seller.findAll();
			for (Seller seller : sellerList) {
				List<Product> products = Product.find("bySeller", seller)
						.fetch();
				logger.info(Log.message("Generating the feed for seller : "
						+ seller.getName() + " : " + products.size()));
				generateProductFeed(seller);
			}

			logger.info(Log
					.message("Successfully created the product feed for all the sellers..."));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Log
					.message("Exception occurred in creating the product feed job : "
							+ e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

	private void generateProductFeed(Seller seller) throws IOException {
		FileWriter writer = new FileWriter(
				Play.configuration
						.getProperty("affiliate.cj.product.feed.output.location")
						+ seller.getName() + ".txt");
		// writer.append("id\tseller_id\tcategory_id\tdescription\timage_url\tin_stock\tbrand_id\tname\tprice\tsale_price");
		writer.append("PROGRAMNAME\tPROGRAMURL\tCATALOGNAME\tLASTUPDATED\tNAME\tKEYWORDS\tDESCRIPTION\tSKU\tMANUFACTURER\tMANUFACTURERID\tUPC\tISBN\tCURRENCY\tSALEPRICE\tPRICE\tRETAILPRICE\tFROMPRICE\tBUYURL\tIMPRESSIONURL\tIMAGEURL\tADVERTISERCATEGORY\tTHIRDPARTYID\tTHIRDPARTYCATEGORY\tAUTHOR\tARTIST\tTITLE\tPUBLISHER\tLABEL\tFORMAT\tSPECIAL\tGIFT\tPROMOTIONALTEXT\tSTARTDATE\tENDDATE\tOFFLINE\tONLINE\tINSTOCK\tCONDITION\tWARRANTY\tSTANDARDSHIPPINGCOST");
		writer.append("\n");
		List<Product> products = Product.find("bySeller", seller).fetch();
		for (Product product : products) {
			//PROGRAMNAME
			if (product.getSeller() != null) {
				writer.append(product.getSeller().getName().toString());
			}
			writer.append("\t");
			
			//PROGRAMURL
			writer.append("\t");
			
			//CATALOGNAME
			writer.append("\t");
			
			//LASTUPDATED
			writer.append("\t");
			
			//NAME
			if (product.getName() != null) {
				writer.append(product.getName());
			}
			writer.append("\t");
			
			//KEYWORDS
			writer.append("\t");
			
			//DESCRIPTION
			if (product.getDescription() != null) {
				writer.append(product.getDescription());
			}
			writer.append("\t");
			
			//SKU
			if (product.getSku() != null) {
				writer.append(product.getSku());
			}
			writer.append("\t");
			
			//MANUFACTURER
			if (product.getBrand() != null) {
				writer.append(product.getBrand().getName());
			}
			writer.append("\t");
			
			//MANUFACTURERID
			writer.append("\t");
			
			//UPC
			writer.append("\t");
			
			//ISBN
			writer.append("\t");
			
			//CURRENCY
			writer.append("\t");
			
			//SALEPRICE
			if (product.getSalePrice() != null) {
				writer.append(product.getSalePrice().toString());
			}
			writer.append("\t");			
			
			//PRICE
			if (product.getPrice() != null) {
				writer.append(product.getPrice().toString());
			}
			writer.append("\t");
			
			//RETAILPRICE
			if (product.getRetailPrice() != null) {
				writer.append(product.getRetailPrice().toString());
			}
			writer.append("\t");
			
			//FROMPRICE
			writer.append("\t");
			
			//BUYURL
			if (product.getBuyURL() != null) {
				writer.append(product.getBuyURL().toString());
			}
			writer.append("\t");
			
			//IMPRESSIONURL
			writer.append("\t");
			
			//IMAGEURL
			if (product.getImageURL() != null) {
				writer.append(product.getImageURL());
			}
			writer.append("\t");
			
			//ADVERTISERCATEGORY
			if (product.getCategory() != null) {
				writer.append(product.getCategory().getName());
			}
			writer.append("\t");
			
			//THIRDPARTYID
			writer.append("\t");
			
			//THIRDPARTYCATEGORY
			writer.append("\t");
			
			//AUTHOR
			writer.append("\t");
			
			//ARTIST
			writer.append("\t");
			
			//TITLE
			writer.append("\t");
			
			//PUBLISHER
			writer.append("\t");
			
			//LABEL
			writer.append("\t");
			
			//FORMAT
			writer.append("\t");
			
			//SPECIAL
			writer.append("\t");
			
			 //GIFT
			 writer.append("\t");
			
			//PROMOTIONALTEXT
			writer.append("\t");
			
			//STARTDATE
			writer.append("\t");
			
			//ENDDATE
			writer.append("\t");
			
			//OFFLINE
			writer.append("\t");
			
			//ONLINE
			writer.append("\t");
			
			//INSTOCK
			if (product.getInStock() != null) {
				if(product.getInStock() == Boolean.TRUE){
					writer.append("yes");
				}else{
					writer.append("no");
				}				
			}
			writer.append("\t");
			
			//CONDITION
			writer.append("\t");
			
			//WARRANTY
			writer.append("\t");
			
			//STANDARDSHIPPINGCOST
			writer.append("\t");
			
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}

}