//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.04 at 06:00:51 PM CST 
//


package utils.rakuten.classfromxsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="product" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}productImage"/>
 *         &lt;element ref="{}buy"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "product",
    "productImage",
    "buy"
})
@XmlRootElement(name = "URL")
public class URL {

    @XmlElement(required = true)
    protected String product;
    @XmlElement(required = true)
    protected String productImage;
    @XmlElement(required = true)
    protected String buy;

    /**
     * Gets the value of the product property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProduct() {
        return product;
    }

    /**
     * Sets the value of the product property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProduct(String value) {
        this.product = value;
    }

    /**
     * Gets the value of the productImage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProductImage() {
        return productImage;
    }

    /**
     * Sets the value of the productImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProductImage(String value) {
        this.productImage = value;
    }

    /**
     * Gets the value of the buy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuy() {
        return buy;
    }

    /**
     * Sets the value of the buy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuy(String value) {
        this.buy = value;
    }

}