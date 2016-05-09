package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import models.Product;
import models.SKSimilarProduct;
import models.cj.CJProduct;
import models.impactradius.ImpactRadiusProduct;
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;
import utils.log.Log;
import models.searskmart.SKRatedSimilarProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import batch.jobs.CreateBrand;
import batch.jobs.SyncSKProductsDetailsTSV;
import info.debatty.java.stringsimilarity.KShingling;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.StringProfile;

public class AffiliateProductUtil {

	private static Logger logger = Logger.getLogger(AffiliateProductUtil.class);

	public static List<Set<String>> permutations(final Set<String> input) {
		if (input.size() == 1) {
			return Arrays.asList(input);
		}
		final List<Set<String>> output = new ArrayList<Set<String>>();
		final Iterator<String> iter = input.iterator();
		while (iter.hasNext()) {
			final String item = iter.next();
			final Set<String> copy = new LinkedHashSet<String>(input);
			copy.remove(item);
			for (final Set<String> rest : permutations(copy)) {
				rest.add(item);
				output.add(rest);
			}
		}
		return output;
	}

	public static List<String> getInputCombinations(String keyword) {

		// Constructing the various input string combination and the
		// expressions based on that
		List<String> queryInputCombinations = new ArrayList<String>();
		List<String> updatedQueryInputCombinations = new ArrayList<String>();
		Set<String> inputStringSet = null;

		// First combination
		queryInputCombinations.add("%" + keyword + "%");

		inputStringSet = new HashSet<String>(Arrays.asList(keyword.split(" ")));
		if (inputStringSet.size() > 1) {
			List<Set<String>> keywordCombinations = AffiliateProductUtil.permutations(inputStringSet);

			if (inputStringSet.size() < 4) {
				// Second combination
				for (Set<String> inputSet : keywordCombinations) {
					String input = StringUtils.join(inputSet.toArray(), " ");
					String queryInputCombination = "%" + input + "%";
					if (!queryInputCombinations.contains(queryInputCombination)) {
						queryInputCombinations.add(queryInputCombination);
					}
				}

				// Third combination
				queryInputCombinations.add("%" + keyword.replace(" ", "%") + "%");

				// Fourth combination
				for (Set<String> inputSet : keywordCombinations) {
					String input = StringUtils.join(inputSet.toArray(), " ");
					String queryInputCombination = "%" + input.replace(" ", "%") + "%";
					if (!queryInputCombinations.contains(queryInputCombination)) {
						queryInputCombinations.add(queryInputCombination);
					}
				}
				// Skipping the other set of wildcard combination which could be
				// the substring of the keyword and its permutations.
			}

			// This combination is same third combination - Adding if not
			// entered the loop
			String queryInputCombination = "%" + keyword.replace(" ", "%") + "%";
			if (!queryInputCombinations.contains(queryInputCombination)) {
				queryInputCombinations.add(queryInputCombination);
			}

			// Fifth combination
			for (String input : inputStringSet) {
				queryInputCombinations.add("%" + input + "%");
			}
		}

		// Escaping the '
		for (String input : queryInputCombinations) {
			String modifiedInput = input.replace("'", "''");
			updatedQueryInputCombinations.add(modifiedInput);
		}
		return updatedQueryInputCombinations;
	}

	public static boolean isUpdateNeeded(Product p, CJProduct cjProduct) {

		try {
			if (cjProduct.getInStock() != null && cjProduct.getInStock().booleanValue() == true) {
				if (p.getInStock() != null && p.getInStock().booleanValue() == false) {
					return true;
				}
			}

			if (cjProduct.getPrice() == null) {
				if (p.getPrice() == null) {
					// do nothing
				} else {
					return true;
				}
			} else if (cjProduct.getPrice() != null) {
				if (p.getPrice() != null) {
					if (!cjProduct.getPrice().equals(p.getPrice())) {
						return true;
					} else {
						// do nothing
					}
				} else {
					return true;
				}
			}

			if (cjProduct.getSalePrice() == null) {
				if (p.getSalePrice() == null) {
					// do nothing
				} else {
					return true;
				}
			} else if (cjProduct.getSalePrice() != null) {
				if (p.getSalePrice() != null) {
					if (!cjProduct.getSalePrice().equals(p.getSalePrice())) {
						return true;
					} else {
						// do nothing
					}
				} else {
					return true;
				}
			}

			if (cjProduct.getBuyURL() != null && !cjProduct.getBuyURL().equals(p.getBuyURL())) {
				return true;
			}

			if (cjProduct.getImageURL() != null && !cjProduct.getImageURL().equals(p.getImageURL())) {
				return true;
			}

			if (cjProduct.getManufacturerName() != null && cjProduct.getManufacturerName().length() > 0) {
				if (p.getBrand() == null) {
					return true;
				} else {
					if (!cjProduct.getManufacturerName().equals(p.getBrand().getName())) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(Log.message(e.getMessage()));
		}
		return false;
	}

	// Sears/Kmart changes
	public static boolean isUpdateNeededForSKProduct(Product p, SearsKmartProduct skProduct) {

		if (skProduct.getInStock() != null && skProduct.getInStock().booleanValue() == true) {
			if (p.getInStock() != null && p.getInStock().booleanValue() == false) {
				return true;
			}
		}

		if (skProduct.getRegularPrice() == null) {
			if (p.getPrice() == null) {
				// do nothing
			} else {
				return true;
			}
		} else if (skProduct.getRegularPrice() != null) {
			if (p.getPrice() != null) {
				if (!skProduct.getRegularPrice().equals(p.getPrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}

		if (skProduct.getSellingPrice() == null) {
			if (p.getSalePrice() != null) {
				// do nothing
			} else {
				return true;
			}
		} else if (skProduct.getSellingPrice() != null) {
			if (p.getSalePrice() != null) {
				if (!skProduct.getSellingPrice().equals(p.getSalePrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}
		return false;
	}

	// Rakuten Feed
	public static boolean isUpdateNeededForRakutenProduct(Product p, RakutenProduct rakutenProduct) {

		if (rakutenProduct.getAvailability() != null && rakutenProduct.getAvailability().booleanValue() == true) {
			if (p.getInStock() != null && p.getInStock().booleanValue() == false) {
				return true;
			}
		}

		if (rakutenProduct.getRetailPrice() == null) {
			if (p.getPrice() == null) {
				// do nothing
			} else {
				return true;
			}
		} else if (rakutenProduct.getRetailPrice() != null) {
			if (p.getPrice() != null) {
				if (!rakutenProduct.getRetailPrice().equals(p.getPrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}

		if (rakutenProduct.getSalePrice() == null) {
			if (p.getSalePrice() != null) {
				// do nothing
			} else {
				return true;
			}
		} else if (rakutenProduct.getSalePrice() != null) {
			if (p.getSalePrice() != null) {
				if (!rakutenProduct.getSalePrice().equals(p.getSalePrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Impact Radius Feed
	 */
	public static boolean isUpdateNeededForImpactRadiusProductPrice(Product p,
			ImpactRadiusProduct impactRadiusProduct) {

		if (impactRadiusProduct.isStockAvailability() && impactRadiusProduct.isStockAvailability() == true) {
			if (p.getInStock() != null && p.getInStock().booleanValue() == false) {
				return true;
			}
		}

		if (impactRadiusProduct.getOriginalPrice() == null) {
			if (p.getPrice() == null) {
				// do nothing
			} else {
				return true;
			}
		} else if (impactRadiusProduct.getOriginalPrice() != null) {
			if (p.getPrice() != null) {
				if (!impactRadiusProduct.getOriginalPrice().equals(p.getPrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}

		if (impactRadiusProduct.getCurrentPrice() == null) {
			if (p.getSalePrice() != null) {
				// do nothing
			} else {
				return true;
			}
		} else if (impactRadiusProduct.getCurrentPrice() != null) {
			if (p.getSalePrice() != null) {
				if (!impactRadiusProduct.getCurrentPrice().equals(p.getSalePrice())) {
					return true;
				} else {
					// do nothing
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public static boolean isUpdateNeededForImpactRadiusProductLink(Product p, ImpactRadiusProduct impactRadiusProduct) {
		// Add the new check based on buy_url
		if (impactRadiusProduct.getProductURL() != null) {
			if (!p.getBuyURL().equals(impactRadiusProduct.getProductURL())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isUpdateNeededForImpactRadiusProductCategory(Product p,
			ImpactRadiusProduct impactRadiusProduct) {
		// Add the new check based on Category
		if (impactRadiusProduct.getCategory() != null) {
			if (p.getAdvertiserCategory() == null) {
				return true;
			} else {
				if (!p.getAdvertiserCategory().equals(impactRadiusProduct.getCategory())) {
					return true;
				}
			}
		}
		return false;
	}

	// Calculate similarity with SK product and other Product
	public static SKSimilarProduct calculateSimilarityWithSKAndOther(Product skProduct, Product otherProduct) {
		SKSimilarProduct similarProduct = null;
		try {
			if (skProduct == null) {
				logger.error(Log.message("Invaid SK Product"));
				return similarProduct;
			}

			if (otherProduct == null) {
				logger.error(Log.message("Invaid Other Product"));
				return similarProduct;
			}
			/*
			 * NGram ngram = new NGram(); BigDecimal similarRate = new
			 * BigDecimal(ngram.distance(skProduct.getName(),
			 * otherProduct.getName()));
			 */

			KShingling ks = new KShingling(2);

			// For cosine similarity I need the profile of strings
			StringProfile profile1 = ks.getProfile(skProduct.getName());
			StringProfile profile2 = ks.getProfile(otherProduct.getName());

			BigDecimal similarRate = new BigDecimal(profile1.cosineSimilarity(profile2));

			// SKSimilarProduct skSimilarProduct = new
			// SKSimilarProduct(skProduct, otherProduct);
			similarProduct = new SKSimilarProduct(skProduct, otherProduct, similarRate);
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return similarProduct;
	};
}
