package utils.shopyourway;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import play.Play;

public class FacetsUtils {

	@Nullable
	public static String getBrand(List<Map<String, String>> facets) {
		if (facets == null || facets.isEmpty()) {
			return null;
		}
		String facetKey = Play.configuration.getProperty("shopyourway.facets.key");
		String brandKey = Play.configuration.getProperty("shopyourway.facets.key.brand");
		for (Map<String, String> facet : facets) {
			if (brandKey.equals(facet.get(facetKey))) {
				String facetValue = Play.configuration.getProperty("shopyourway.facets.value");
				return facet.get(facetValue);
			}
		}
		return null;
	}
}
