package utils.impactradius;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import utils.impactradius.classfromxsd.Products;
import utils.impactradius.classfromxsd.Products.Product;
import utils.log.Log;

public class ImpactRadiusXMLHandler {
	private static Logger logger = Logger
			.getLogger(ImpactRadiusXMLHandler.class);

	public Products getProductListFromSingleXML(File input) {
		Products products = null;
		if (!input.exists()) {
			logger.error(Log
					.message("Exiting the process as no such folder exists : "
							+ input.getAbsolutePath()));
		} else {
			JAXBContext jaxbContext = null;
			Unmarshaller jaxbUnmarshaller = null;
			try {
				jaxbContext = JAXBContext.newInstance(Products.class);
				jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				products = (Products) jaxbUnmarshaller.unmarshal(input);
			} catch (JAXBException e) {
				logger.error(Log
						.message("Issues in RakutenXMLHandler getProductListFromSingleXML function : "
								+ e.getMessage()));
				e.printStackTrace();
			}
		}
		return products;
	}

	public static void main(String[] args) throws Exception {
		String xmlPath = "C:\\Users\\lwan0\\Desktop\\tmp\\cj\\tmp\\Diaperscom-Product-Catalog_IR_1.xml";
		
		/*
		 * JAXBContext jaxbContext = JAXBContext.newInstance(Products.class);
		 * Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		 * Products ps = (Products) jaxbUnmarshaller.unmarshal(new
		 * File(xmlPath));
		 */

		Products ps = new ImpactRadiusXMLHandler()
				.getProductListFromSingleXML(new File(xmlPath));

		List<Product> pss = ps.getProduct();
		System.out.println(pss.size());
		for (Product p : pss) {
			System.out.println(p.getUniqueMerchantSKU());

			System.out.println(p.getManufacturer());
			System.out.println(p.getUPC());

			System.out.println(p.getCategory());

			System.out.println(p.getProductDescription());

			System.out.println(p.getProductURL());

			System.out.println(p.getImageURL());

			System.out
					.println("-----------------------------------------------------------------------------------------");

		}
		System.out.println(pss.size());
	}
}
