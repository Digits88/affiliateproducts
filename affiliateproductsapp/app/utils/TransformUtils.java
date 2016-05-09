package utils;

import java.lang.reflect.Constructor;
import java.util.List;

import models.Brand;
import models.AdvertiserCategory;
import models.Product;
import models.Seller;
import models.VersionedEntity;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import controllers.json.BaseJson;
import controllers.json.BrandJson;
import controllers.json.CategoryJson;
import controllers.json.ProductJson;
import controllers.json.SellerJson;
import exceptions.ServiceException;

/**
 * This utils class contains basic transform operation.
 */
public class TransformUtils {

	private static Logger logger = Logger.getLogger(TransformUtils.class);

	public static <E extends VersionedEntity> Iterable<Long> toIds(
			Iterable<E> collection) {
		return Iterables.transform(collection, new Function<E, Long>() {
			@Override
			public Long apply(E entity) {
				return entity.getId();
			}
		});
	}

	public static BaseJson toJson(VersionedEntity entity) {
		BaseJson jsonMappingClassObject = null;
		try {
			Class jsonMappingClass = ModelToJsonMapping
					.getJsonMappingClass(entity.getClass());
			Constructor constructor = jsonMappingClass.getConstructor(entity
					.getClass());
			jsonMappingClassObject = (BaseJson) constructor.newInstance(entity);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			throw new ServiceException("err.msg.json.parse");
		}
		return jsonMappingClassObject;
	}

	public static <E extends VersionedEntity, F extends BaseJson> List<F> toJsonList(
			List<E> collection) {
		return Lists.transform(collection, new Function<E, F>() {
			@Override
			public F apply(E entity) {
				try {
					return ((F) toJson(entity));
				} catch (Exception e) {
					throw new ServiceException("err.msg.json.parse");
				}
			}
		});
	}

	enum ModelToJsonMapping {

		ProductMapping(Product.class, ProductJson.class), SellerMapping(
				Seller.class, SellerJson.class), BrandMapping(Brand.class,
				BrandJson.class), CategoryMapping(AdvertiserCategory.class,
				CategoryJson.class);

		private final Class modelClass;
		private final Class jsonMappingClass;

		ModelToJsonMapping(Class modelClass, Class jsonMappingClass) {
			this.modelClass = modelClass;
			this.jsonMappingClass = jsonMappingClass;
		}

		public static Class getJsonMappingClass(Class modelClass) {
			Class jsonMappingClass = null;
			for (ModelToJsonMapping modelToJsonMapping : ModelToJsonMapping
					.values()) {
				if (modelToJsonMapping.modelClass.equals(modelClass)) {
					jsonMappingClass = modelToJsonMapping.jsonMappingClass;
					break;
				}
			}
			return jsonMappingClass;
		}
	}
}
