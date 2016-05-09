package configuration.injectors;

import services.CacheService;
import services.CategoryService;
import services.EmailService;
import services.FileService;
import services.LogService;
import services.ProductService;
import services.SKProductService;
import services.SKSimiliarProductService;
import services.cj.CJService;
import services.cj.impl.CJFileService;
import services.cj.impl.DefaultCJService;
import services.impactradius.ImpactRadiusProductService;
import services.impl.DatabaseLogService;
import services.impl.DefaultCacheService;
import services.impl.DefaultCategoryService;
import services.impl.DefaultEmailService;
import services.impl.DefaultImpactRadiusProductService;
import services.impl.DefaultProductService;
import services.impl.DefaultRakutenProductService;
import services.impl.DefaultSKProductService;
import services.rakuten.RakutenProductService;
import services.sk.impl.DefaultSKSimilarProductService;

import com.google.inject.AbstractModule;

public class ServiceInjector extends AbstractModule {

	@Override
	protected void configure() {
		// Configure dependencies in service layer.
		bind(CacheService.class).to(DefaultCacheService.class);
		bind(ProductService.class).to(DefaultProductService.class);
		bind(CategoryService.class).to(DefaultCategoryService.class);
		bind(LogService.class).to(DatabaseLogService.class);
		bind(CJService.class).to(DefaultCJService.class);
		
		// for SKProductService
		bind(SKProductService.class).to(DefaultSKProductService.class);
		
		// For CJ ProductService
		bind(FileService.class).to(CJFileService.class);
		
		// for RakutenProductService
		bind(RakutenProductService.class).to(DefaultRakutenProductService.class);
		
		// for ImpactRadiusProductService
		bind(ImpactRadiusProductService.class).to(DefaultImpactRadiusProductService.class);

		// for ImpactRadiusProductService
		bind(EmailService.class).to(DefaultEmailService.class);
		
		// For SK Similiar Product
		bind(SKSimiliarProductService.class).to(DefaultSKSimilarProductService.class);

	}
}
   