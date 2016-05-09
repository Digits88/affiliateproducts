package enums;

public enum SellerEnum {

	BANANAREPUBLIC("Banana Republic"), BLUEFLY("Bluefly"), SURLATABLE("Sur La Table"), CHICCO("Chicco"), UGG("UGG");

	public final String sellerName;

	SellerEnum(String seller) {
		this.sellerName = seller;
	}

}
