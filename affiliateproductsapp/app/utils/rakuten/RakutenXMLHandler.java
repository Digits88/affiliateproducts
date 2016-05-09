package utils.rakuten;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

import models.rakuten.RakutenProduct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import batch.jobs.SyncRakutenProductsDetails;
import utils.log.Log;
import utils.rakuten.classfromxsd.Discount;
import utils.rakuten.classfromxsd.Merchandiser;
import utils.rakuten.classfromxsd.Price;
import utils.rakuten.classfromxsd.Product;
import utils.rakuten.classfromxsd.URL;



public class RakutenXMLHandler {
	
	private static Logger logger = Logger
			.getLogger(RakutenXMLHandler.class);
	
	public Merchandiser getProductListFromSingleXML(File input) {
		Merchandiser merchandiser = null;
		if(!input.exists()) {
			logger.error(Log
					.message("Exiting the process as no such folder exists : "
							+ input.getAbsolutePath()));
		} else {
			JAXBContext jaxbContext = null;
			Unmarshaller jaxbUnmarshaller = null;
			try {
				jaxbContext = JAXBContext.newInstance(Merchandiser.class);
				jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				merchandiser = (Merchandiser) jaxbUnmarshaller.unmarshal(input);
			} catch (JAXBException e) {
				logger.error(Log
						.message("Issues in RakutenXMLHandler getProductListFromSingleXML function : " + e.getMessage() ));
				e.printStackTrace();
			}
		}
		return merchandiser;
	}

	public static void main(String[] args) throws Exception {
		String xmlPath = "C:\\Users\\lwan0\\Desktop\\tmp\\Rakuten\\Best Buy\\testtest.xml";
				
		JAXBContext jaxbContext = JAXBContext.newInstance(Merchandiser.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Merchandiser m = (Merchandiser) jaxbUnmarshaller.unmarshal(new File(xmlPath));
		
		List<Product> ps = m.getProduct();
		System.out.println(ps.size());
		for(Product  p : ps) {
			System.out.println(p.getSkuNumber());
			
			System.out.println(p.getBrand());
			System.out.println(p.getUpc());
			
			System.out.println(p.getBrand());
			
			System.out.println(p.getCategory().getPrimary());
			
			System.out.println(p.getDescription().getLong());
			
			System.out.println(p.getURL().getProduct());
				
			}
		}	
}
