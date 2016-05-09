package utils.searskmart;

import java.util.Comparator;

import models.SKSimilarProduct;
import models.searskmart.SKRatedSimilarProduct;

public class SimilarProductComparator implements Comparator<SKSimilarProduct> {

	@Override
	public int compare(SKSimilarProduct o1, SKSimilarProduct o2) {
		if (o1 != null && o2 != null) {
			// To arrange in ascending order, the condition is reversed
			return o2.getSimilarity().compareTo(o1.getSimilarity());
		} else {
			// This should never happen
			return 0;
		}
	}

}
