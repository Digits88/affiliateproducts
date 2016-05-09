package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtils {

	public static final BigDecimal minimumPriceValue = new BigDecimal(0);
	public static final BigDecimal maximumPriceValue = new BigDecimal(10000000);

	public static void main(String[] args) {
		BigDecimal regularPrice = new BigDecimal(11.0);
		BigDecimal salePrice = new BigDecimal(9.0);
		System.out.println(getSale(regularPrice, salePrice));
	}

	public static Integer getSale(BigDecimal regularPrice, BigDecimal salePrice) {
		Integer saleInPercentage = 0;
		if (regularPrice != null && salePrice != null
				&& regularPrice.subtract(salePrice).intValue() > 0) {
			BigDecimal diff = regularPrice.subtract(salePrice);
			BigDecimal perc = diff
					.divide(regularPrice, 4, RoundingMode.HALF_UP);
			BigDecimal sale = perc.multiply(new BigDecimal(100));
			saleInPercentage = Integer.valueOf(sale.intValue());
		}
		return saleInPercentage;
	}
}
