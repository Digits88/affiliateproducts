package services.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import models.Brand;
import models.EssentialsMedia;
import models.Product;
import models.Seller;
import models.User;
import models.UserInstagram;
import models.image.MediaParams;
import models.image.OptimizedImage;
import models.image.UserMediaContent;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.makers.FixedSizeThumbnailMaker;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;

import play.Play;
import play.cache.Cache;
import repositories.Repository;
import utils.image.ImageUtils;
import utils.imagemagick.identify.Identify;
import utils.imagemagick.identify.ImageIdentity;
import utils.imagemagick.identify.ImageIdentityService;
import utils.log.Log;
import batch.jobs.SyncProductsDetailTSVChildDeletion;
import constants.ImageServerConstants;
import enums.ImageOf;
import enums.MediaType;

public class ImageService {
	private static Logger logger = Logger.getLogger(ImageService.class);

	@Inject
	protected static Repository repository;

	public ImageService() {
	}

	public static ImageService getInstance() {
		return new ImageService();
	}

	private static final String PREFIX = "identify_";
	private static String productImageHomeDir = Play.configuration.getProperty("image.product.home.directory");

	private static String imageOnISPrefix = Play.configuration.getProperty("image.url.is.address.prefix");
	private static String mediaOnISPrefix = Play.configuration.getProperty("media.url.is.address.prefix");
	private static String essentialOnISPrefix = Play.configuration.getProperty("essential.url.is.address.prefix");

	private static String cdnURLPrefix = Play.configuration.getProperty("image.url.cdn.address.pattern");

	private static final String JPGEXTENSION = ".jpg";
	private static final String ORIGINALSIZE = "";
	private static final String MINITHUMBNAIL = "w=100&h=100";
	private static final String MEDIUMTHUMBNAIL = "w=200&h=200";
	private static final String BIGTHUMBNAIL = "w=300&h=300";

	public String saveUserInstagramImage(UserInstagram user) {
		String localImageUrl = null;
		String destinationFile = null;
		if (user == null) {
			logger.error(Log.message("##### This user is not existing in our DB #####"));
			return null;
		}

		try {
			logger.info("Start Saving image for " + user.getInstagramUserName());
			String userName = user.getInstagramUserName().replaceAll("[^a-zA-Z0-9]+", "");
			String userImageName = userName.concat(ImageServerConstants.JPG_FORMAT);

			/**
			 * Format: image_home_directory/user/userName.jpg
			 */
			destinationFile = productImageHomeDir.concat(ImageOf.USER.type).concat(File.separator)
					.concat(userImageName);

			// check if image is already downloaded.
			if (isAlreadyDownloaded(destinationFile)
					&& isSameSizeAsImageOnURL(destinationFile, user.getInstagramProfilePicture())) {
				logger.info("=====> " + userImageName + " is already downloaded!");
				return null;
			}
			String imageUrl = user.getInstagramProfilePicture();
			if (imageUrl != null && imageUrl.length() > 0) {
				boolean downloaded = downloadImageFromURL(imageUrl, destinationFile);
				if (downloaded) {
					// localImageUrl = constructuserImageUrl(imageUrl,
					// ImageOf.user.type, userName);
					localImageUrl = constructUserInstagramImageUrl(user, userName);
				} else {
					logger.error(Log.message("##### Download is Failed, please check! #####"));
					return null;
				}
			} else {
				logger.error(Log.message("##### Image Url is not Existed! Please Add Url First! #####"));
				return null;
			}

		} catch (Exception e) {
			logger.error(Log.message("##### " + user.getInstagramUserName() + " | "
					+ user.getLocalInstagramProfilePicture() + " ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish Store image on server drive : " + destinationFile);
		return localImageUrl;
	}

	private String constructUserInstagramImageUrl(UserInstagram user, String userName) {
		String res = null;
		try {
			logger.info("Start construct Local Image Url");
			// Prefix?url=CDN_URL/HighLevel_Type/user_id.jpg&t=sub_type&n=userName
			String userIDJPG = user.getId().toString().concat(ImageServerConstants.JPG_FORMAT);
			String imageUrl = cdnURLPrefix.concat(ImageOf.PROFILE.type).concat(ImageServerConstants.FORWARD_SLASH)
					.concat(userIDJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String newUserName = new String(Base64.encodeBase64(userName.getBytes()));
			res = imageOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK)
					.concat(imageUrl).concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK)
					.concat(ImageOf.USER.type).concat(ImageServerConstants.AND_MARK)
					.concat(ImageServerConstants.NAME_MARK).concat(newUserName);
			logger.info(userName + " Has Local Image URL : " + res);
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		logger.info("Finish construct Local Image Url");
		return res;
	}

	public String saveBrandImage(Brand brand) {
		String localImageUrl = null;
		String destinationFile = null;
		if (brand == null) {
			logger.error(Log.message("##### This brand is not existing in our DB #####"));
			return null;
		}

		try {
			logger.info("Start Saving image for " + brand.getName());
			String brandName = brand.getName().replaceAll("[^a-zA-Z0-9]+", "");
			String brandImageName = brandName.concat(ImageServerConstants.JPG_FORMAT);

			/**
			 * Format: image_home_directory/brand/brandName.jpg
			 */
			destinationFile = productImageHomeDir.concat(ImageOf.BRAND.type).concat(File.separator)
					.concat(brandImageName);

			// check if image is already downloaded.
			if (isAlreadyDownloaded(destinationFile) && isSameSizeAsImageOnURL(destinationFile, brand.getImageUrl())) {
				logger.info("=====> " + brandImageName + " is already downloaded!");
				return null;
			}
			String imageUrl = brand.getImageUrl();
			if (imageUrl != null && imageUrl.length() > 0) {
				boolean downloaded = downloadImageFromURL(imageUrl, destinationFile);
				if (downloaded) {
					// localImageUrl = constructbrandImageUrl(imageUrl,
					// ImageOf.brand.type, brandName);
					localImageUrl = constructBrandImageUrl(brand, brandName);
				} else {
					logger.error(Log.message("##### Download is Failed, please check! #####"));
					return null;
				}
			} else {
				logger.error(Log.message("##### Image Url is not Existed! Please Add Url First! #####"));
				return null;
			}

		} catch (Exception e) {
			logger.error(Log.message("##### " + brand.getName() + " | " + brand.getLocalImageUrl() + " ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish Store image on server drive : " + destinationFile);
		return localImageUrl;
	}

	private String constructBrandImageUrl(Brand brand, String brandName) {
		String res = null;
		try {
			logger.info("Start construct Local Image Url");
			// Prefix?url=CDN_URL/HighLevel_Type/brand_id.jpg&t=sub_type&n=brandName
			String brandIDJPG = brand.getId().toString().concat(ImageServerConstants.JPG_FORMAT);
			String imageUrl = cdnURLPrefix.concat(ImageOf.PROFILE.type).concat(ImageServerConstants.FORWARD_SLASH)
					.concat(brandIDJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String newbrandName = new String(Base64.encodeBase64(brandName.getBytes()));
			res = imageOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK)
					.concat(imageUrl).concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK)
					.concat(ImageOf.BRAND.type).concat(ImageServerConstants.AND_MARK)
					.concat(ImageServerConstants.NAME_MARK).concat(newbrandName);
			logger.info(brandName + " Has Local Image URL : " + res);
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		logger.info("Finish construct Local Image Url");
		return res;
	}

	public String saveSellerImage(Seller seller) {
		String localImageUrl = null;
		String destinationFile = null;
		if (seller == null) {
			logger.error(Log.message("##### This seller is not existing in our DB #####"));
			return null;
		}

		try {
			logger.info("Start Saving image for " + seller.getName());
			String sellerName = seller.getName().replaceAll("[^a-zA-Z0-9]+", "");
			String sellerImageName = sellerName.concat(ImageServerConstants.JPG_FORMAT);

			/**
			 * Format: image_home_directory/Seller/sellerName.jpg
			 */
			destinationFile = productImageHomeDir.concat(ImageOf.SELLER.type).concat(File.separator)
					.concat(sellerImageName);

			// check if image is already downloaded.
			/*if (isAlreadyDownloaded(destinationFile) && isSameSizeAsImageOnURL(destinationFile, seller.getImageUrl())) {
				logger.info("=====> " + sellerImageName + " is already downloaded!");
				return null;
			}*/
			String imageUrl = seller.getImageUrl();
			if (imageUrl != null && imageUrl.length() > 0) {
				boolean downloaded = downloadImageFromURL(imageUrl, destinationFile);
				if (downloaded) {
					// localImageUrl = constructSellerImageUrl(imageUrl,
					// ImageOf.SELLER.type, sellerName);
					localImageUrl = constructSellerImageUrl(seller, sellerName);
				} else {
					logger.error(Log.message("##### Download is Failed, please check! #####"));
					return null;
				}
			} else {
				logger.error(Log.message("##### Image Url is not Existed! Please Add Url First! #####"));
				return null;
			}

		} catch (Exception e) {
			logger.error(Log.message("##### " + seller.getName() + " | " + seller.getLocalImageUrl() + " ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish Store image on server drive : " + destinationFile);
		return localImageUrl;
	}

	private String constructSellerImageUrl(Seller seller, String sellerName) {
		String res = null;
		try {
			logger.info("Start construct Local Image Url");
			// Prefix?url=CDN_URL/HighLevel_Type/seller_id.jpg&t=sub_type&n=sellerName
			String sellerIDJPG = seller.getId().toString().concat(ImageServerConstants.JPG_FORMAT);
			String imageUrl = cdnURLPrefix.concat(ImageOf.PROFILE.type).concat(ImageServerConstants.FORWARD_SLASH)
					.concat(sellerIDJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String newSellerName = new String(Base64.encodeBase64(sellerName.getBytes()));
			res = imageOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK)
					.concat(imageUrl).concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK)
					.concat(ImageOf.SELLER.type).concat(ImageServerConstants.AND_MARK)
					.concat(ImageServerConstants.NAME_MARK).concat(newSellerName);
			logger.info(sellerName + " Has Local Image URL : " + res);
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		logger.info("Finish construct Local Image Url");
		return res;
	}

	private boolean isSameSizeAsImageOnURL(String destinationFile, String imageURL) {
		try {
			File file = new File(destinationFile);
			URL url = new URL(imageURL);
			int x = url.openConnection().getContentLength();
			long y = file.length();
			if (x == y) {
				return true;
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return false;
	}

	/*
	 * private String constructSellerImageUrl(String imageUrl, String type,
	 * String sellerName) { String res = null; try { logger.info(
	 * "Start construct Local Image Url"); //
	 * Prefix?url=CDN_URL/HighLevel_Type/seller_id.jpg&t=sub_type&n=sellerName
	 * imageUrl = URLEncoder.encode(imageUrl, "UTF-8"); String newSellerName =
	 * new String(Base64.encodeBase64(sellerName.getBytes())); res =
	 * imageOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(
	 * ImageServerConstants.URL_MARK)
	 * .concat(imageUrl).concat(ImageServerConstants.AND_MARK).concat(
	 * ImageServerConstants.TYPE_MARK)
	 * .concat(type).concat(ImageServerConstants.AND_MARK).concat(
	 * ImageServerConstants.NAME_MARK) .concat(newSellerName);
	 * logger.info(sellerName + " Has Local Image URL : " + res); } catch
	 * (Exception e) { logger.error(Log.message("##### " + e.getMessage() +
	 * " #####")); return res; } logger.info("Finish construct Local Image Url"
	 * ); return res; }
	 */

	// Image_Server_Approach_Two
	/*
	 * public boolean saveImage(Product p) { if (p == null) {
	 * logger.error(Log.message(
	 * "##### This product is not existing in our DB #####")); return false; }
	 * 
	 * logger.info("Start Saving Image for " + p.getName()); String seller_ID =
	 * String.valueOf(p.getSeller().getId()); String id =
	 * String.valueOf(p.getId()); String productImageName = id.concat(".jpg");
	 * 
	 * String destinationFile =
	 * productImageHomeDir.concat(seller_ID).concat(File.separator).concat(
	 * productImageName);
	 * 
	 * // check if image is already downloaded. if
	 * (isAlreadyDownloaded(destinationFile)) { logger.info(">>>>> " +
	 * productImageName + " is already downloaded! <<<<<"); return false; }
	 * String imageUrl = p.getImageURL(); try { // Image_Server_Approach_Two //
	 * save the image on drive boolean downloaded =
	 * downloadImageFromURL(imageUrl, destinationFile); // based on the result
	 * will consider add value to imageUrl_is or not if (downloaded) { String
	 * imageOnIS = constructImageOnIS(imageUrl, seller_ID, id);
	 * p.setImageOnIS(imageOnIS); logger.info(p.getName() +
	 * " sets up Image On IS		-- " + imageOnIS); } else {
	 * logger.error(Log.message("##### Download is Failed, please check! #####"
	 * )); return false; }
	 * 
	 * } catch (Exception e) { logger.error(Log.message("##### " + p.getId() +
	 * " | " + p.getImageURL() + " ##### ")); logger.error(Log.message("##### "
	 * + e.getMessage() + " #####")); e.printStackTrace(); } return true; }
	 */

	// Image_Server_Approach_One
	/*
	 * public boolean saveImageInFourType(Product p) { if (p == null) {
	 * logger.error(Log.message(
	 * "##### This product is not existing in our DB #####")); return false; }
	 * 
	 * try { String seller_ID = String.valueOf(p.getSeller().getId()); String id
	 * = String.valueOf(p.getId()); String imageUrl = p.getImageURL();
	 * 
	 * // productImageHomeDir/seller_id/product_id/ String originalImagePath =
	 * productImageHomeDir.concat(seller_ID).concat(File.separator)
	 * .concat(id.concat(JPGEXTENSION));
	 * 
	 * String miniThumbnailPath =
	 * productImageHomeDir.concat(seller_ID).concat(File.separator)
	 * .concat(id.concat("_100x100").concat(JPGEXTENSION));
	 * 
	 * String mediumThumbnailPath =
	 * productImageHomeDir.concat(seller_ID).concat(File.separator)
	 * .concat(id.concat("_200x200").concat(JPGEXTENSION));
	 * 
	 * String bigThumbnailPath =
	 * productImageHomeDir.concat(seller_ID).concat(File.separator)
	 * .concat(id.concat("_300x300").concat(JPGEXTENSION));
	 * 
	 * // Save four types if (!saveOriginalImage(p, seller_ID, id, imageUrl,
	 * originalImagePath)) { logger.error(Log.message(
	 * "##### Download Original Image is Failed !!! #####")); return false; }
	 * File origianlImage = new File(originalImagePath); if
	 * (!saveThumbnailImage(p, origianlImage, miniThumbnailPath,
	 * mediumThumbnailPath, bigThumbnailPath)) { logger.error(Log.message(
	 * "##### Create Thumbnail Has Issue !!! #####")); return false; } } catch
	 * (Exception e) { logger.error(Log.message("##### " + p.getId() + " | " +
	 * p.getImageURL() + " ##### ")); logger.error(Log.message("##### " +
	 * e.getMessage() + " #####")); e.printStackTrace(); return false; } return
	 * true; }
	 */

	// Image_Server_Approach_Two
	private String constructImageOnIS(String imageUrl, String sid, String id) {
		String qstnMark = "?";
		String andMark = "&";

		String res = null;
		try {
			// combination is prefix +
			// ?url={(Escape)imageUrl}&sid={sid}&id={(encode)id}&w={}&h={}
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			id = new String(Base64.encodeBase64(id.getBytes()));
			res = imageOnISPrefix.concat(qstnMark).concat("url=").concat(imageUrl).concat(andMark).concat("sid=")
					.concat(sid).concat(andMark).concat("id=").concat(id);
			logger.info(">>>>> " + res + " <<<<<");
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		return res;
	}

	// Image_Server_Approach_One
	private String constructImageAPIUrl(String imageUrl, String sid, String id, String imageSize) {
		String qstnMark = "?";
		String andMark = "&";

		String res = null;
		try {
			// combination is prefix +
			// ?url={(Escape)imageUrl}&sid={sid}&id={(encode)id}&w={}&h={}
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			id = new String(Base64.encodeBase64(id.getBytes()));
			res = imageOnISPrefix.concat(qstnMark).concat("url=").concat(imageUrl).concat(andMark).concat("sid=")
					.concat(sid).concat(andMark).concat("id=").concat(id);

			if (!imageSize.equals(ORIGINALSIZE)) {
				res = res.concat(andMark).concat(imageSize);
			}

			logger.info(Log.message(">>>>> " + res + " <<<<<"));
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		return res;
	}

	private boolean downloadImageFromURL(String imageURL, String destinationFile) {
		// Check if the image url is valid or not
		if (imageURL == null || imageURL.trim().length() == 0) {
			logger.error(Log.message("##### " + imageURL + " is null or empty value, please check ! #####"));
			return false;
		}
		if (!new UrlValidator().isValid(imageURL)) {
			logger.error(Log.message("##### " + imageURL + " is a valid url, please check ! #####"));
			return false;
		}
		// download image
		URL url = null;
		InputStream is = null;
		OutputStream os = null;
		try {

			File downloadFile = new File(destinationFile);

			File parentDir = downloadFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdir();
			}

			url = new URL(imageURL);
			if (!existsURLImage(url)) {
				logger.info("Image on URL : " + imageURL + " is not exited!!");
				return false;
			}
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = httpcon.getInputStream();
			os = new FileOutputStream(destinationFile);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
		} catch (IOException e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (os != null) {
					os.close();
					os = null;
				}
				// check if download if successful
				if (new File(destinationFile).length() > 0) {
					logger.info(Log.message(">>>>> Downloaded -- " + destinationFile + " <<<<<"));
				} else {
					logger.error(Log.message(
							"##### The size of download file is not same as remote file, please check !  #####"));
					return false;
				}
			} catch (IOException e) {
				logger.error(Log.message("##### " + e.getMessage() + " #####"));
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean existsURLImage(URL url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (UnknownHostException e) {
			logger.error(Log.message("The Host Is Not Available ! Please check -->  " + url.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isAlreadyDownloaded(String destinationFile) {
		File file = new File(destinationFile);
		if (file.exists() && file.length() > 0) {
			return true;
		}
		return false;
	}

	// @Transactional
	public String optimize(File imageName, String dbname) {
		//
		if (!ImageUtils.isOptimizationEnabled()) {
			logger.debug(String.format("optimization disabled .. skipping image %s", imageName));
			return imageName.getAbsolutePath();
		}
		ImageProcessor processor = ImageHelper.getProcessor(imageName.getAbsolutePath());
		String baseName = imageName.getAbsolutePath().substring(0,
				imageName.getAbsolutePath().length() - dbname.length());
		if (processor != null) {
			// String dbImageName = ImageUtils.getImageName(imageName);
			// check db records first .. find by name ..
			logger.debug(String.format("Found image processor %s %s %s %s %s", processor.getName(), "for image",
					imageName.getAbsolutePath(), "image database search name is ", dbname));

			OptimizedImage image = (OptimizedImage) Cache.get(dbname);
			if (image == null) {
				image = OptimizedImage.find("byImageName", dbname).first();
				if (image != null) {
					Cache.add(dbname, image, ImageUtils.getCachetime());
				}
			} else
				logger.debug(String.format("Found image in cache %s", dbname));

			if (image == null) {

				// copy file to temp
				String newFile = ImageUtils.getNewTempName(imageName.getAbsolutePath());
				String newDbFile = ImageUtils.getImageName(newFile);
				File to = new File(newFile);
				File from = imageName;
				if (!ImageUtils.copyFromTo(from, to)) {
					logger.error(
							String.format("Can't copy %s %s %s", from.getAbsolutePath(), "to", to.getAbsolutePath()));
					return imageName.getAbsolutePath();
				}

				// try to create oprimized record ..
				OptimizedImage oi = new OptimizedImage();

				oi.imageName = dbname;
				// oi.isPng = ImageUtils.isPng(dbImageName , );
				oi.userId = ImageUtils.getUserIdFromName(dbname);
				oi.backupName = newDbFile;

				try {
					oi.save();
					logger.debug(String.format("add image to sql cache after db save %s", oi.toString()));
					Cache.add(dbname, oi, ImageUtils.getCachetime());

				} catch (PersistenceException ce) {
					logger.error(String.format("Record with file name %s %s %s", dbname,
							"already exist ... try to lookup again.. and delete", to.getAbsolutePath()));
					image = OptimizedImage.find("byImageName", dbname).first();

					if (image != null) {
						logger.debug(String.format("add image to sql cache after retry lookup %s", oi.toString()));
						Cache.add(dbname, oi, ImageUtils.getCachetime());
					}
					to.delete();

					if (image != null && image.isReady) {
						logger.debug(String.format("Image optimized path %s", baseName.concat(image.imageName)));
						return baseName.concat(image.imageName);
					} else if (image != null && !image.isReady) {
						logger.debug(String.format("Image optimization in progress backup path %s",
								baseName.concat(image.backupName)));
						return baseName.concat(image.backupName);
					} else {
						logger.error(String.format("Didn't find a record with file name %s %s %s", dbname,
								"unknown error.. return original file name", imageName.getAbsolutePath()));
						return imageName.getAbsolutePath();
					}
				}

				// record created then optimize, update oprimized record ,
				// return same name ..
				logger.debug(String.format("Image to service to user %s %s %s", to.getAbsolutePath(),
						"while optimizing", from.getAbsolutePath()));
				if (processor.getName().equals(Identify.SUPPORTED_FORMATS.PNG.name()))
					processor.optimize(from, null);
				else if (processor.getName().equals(Identify.SUPPORTED_FORMATS.JPEG.name()))
					processor.optimize(to, from);

				logger.debug(String.format("Image %s %s", from.getAbsolutePath(), "optimized."));
				image = OptimizedImage.find("byImageName", dbname).first();
				image.isReady = true;
				image.save();
				logger.debug(String.format("replace image in sql cache after db update %s", oi.toString()));
				Cache.replace(dbname, oi);

			} else {
				if (image.isReady) {
					logger.debug(String.format("Image optimized path %s", baseName.concat(image.imageName)));
					return baseName.concat(image.imageName);
				} else if (!image.isReady) {
					logger.debug(String.format("Image optimization in progress backup path %s",
							baseName.concat(image.backupName)));
					return baseName.concat(image.backupName);
				} else {
					logger.error(String.format("null image db record .. unknown error.. return original file name %s",
							imageName));
					return imageName.getAbsolutePath();
				}
			}
		} else {
			logger.debug(String.format("Could not find image processor for image %s %s", imageName,
					"will return original input name.."));
		}
		return imageName.getAbsolutePath();
	}

	public static String transcode(String sourceFilePath, Long wide, Long cropHeight) {

		logger.debug(String.format("transcode image path %s | wide %s | crop height %s", sourceFilePath,
				(null == wide ? "null" : wide.toString()), (null == cropHeight ? "null" : cropHeight.toString())));

		//
		Object id = Cache.get(PREFIX.concat(sourceFilePath));
		if (id == null || !(id instanceof ImageIdentity)) {
			List<String> files = new ArrayList<String>();
			files.add(sourceFilePath);
			List<ImageIdentity> ids = ImageIdentityService.getInstance().getIdentityFromFileList(files);
			logger.info("###### Debug Start ######");
			if (ids != null && ids.size() > 0) {
				Cache.add(PREFIX.concat(sourceFilePath), ids.get(0), ImageUtils.getCachetime());
				logger.info("###### Debug " + ids.get(0));
				id = ids.get(0);
			} else {
				logger.info("###### Debug -- ids is null ");
			}
		}
		Long realwide = ((ImageIdentity) id).getWide();
		Long realHeight = ((ImageIdentity) id).getHeight();

		Long transcodeHeight = null;
		Long transcodeWide = null;

		if (wide != null && realwide.longValue() > wide.longValue()) {
			transcodeWide = wide;
		} else {
			transcodeWide = realwide;
		}
		logger.debug(String.format("original image [ wide %s x height %s ] transcode wide %s", realwide, realHeight,
				transcodeWide));

		if (cropHeight != null && cropHeight.longValue() > 0) {
			// calculate dimensions and apply crop requested params if needed
			// for transcodeHeight
			double dimesion = (realwide.doubleValue() / realHeight.doubleValue());

			double cropDimensions = (transcodeWide.doubleValue() / cropHeight.doubleValue());

			if (dimesion < cropDimensions) {
				transcodeHeight = cropHeight;
			}
		}
		return JPGProcessor.getImageProcessor().transcode(new File(sourceFilePath), transcodeWide, transcodeHeight);
	}

	public String updateUserProfilePicture(String image, String uid) {
		String userProfilePictureUrl = null;
		if (image == null || image.trim().length() == 0) {
			logger.error(Log.message("##### " + image + " is null or empty value, please check ! #####"));
			return userProfilePictureUrl;
		}

		if (uid == null || uid.length() == 0) {
			logger.error(Log.message("##### " + uid + " is null or empty value, please check ! #####"));
			return userProfilePictureUrl;
		}

		String destinationFile = "";

		try {
			logger.info("Start Saving image for " + uid);
			String userProfilePictureName = uid.concat(ImageServerConstants.JPG_FORMAT);

			/**
			 * Format: image_home_directory/User_Profile/uid.jpg
			 */
			destinationFile = productImageHomeDir.concat(ImageOf.USERPROFILE.type).concat(File.separator)
					.concat(userProfilePictureName);
			logger.info("Save User Profile Picture of " + uid + " @ " + destinationFile);

			boolean downloaded = storeImageToDestination(image, destinationFile);
			if (downloaded) {
				userProfilePictureUrl = constructUserProfilePictureUrl(uid);
				File imgOutFile = new File(destinationFile);
				if (imgOutFile.exists()) {
					byte[] data = null;
					String imageCacheKey = imgOutFile.getAbsolutePath();
					if (ImageUtils.isCacheenabled()) {
						logger.debug("Check User Profile Picture in Cache");
						data = getCachedImageData(imageCacheKey);
						if (data != null && data.length > 0) {
							data = FileUtils.readFileToByteArray(imgOutFile);
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
							logger.info("Replace User Profile Picture in Cache : " + destinationFile);
						}
					} else {
						logger.info("User Profile Picture in Cache is not enabled..");
					}
				}
			} else {
				logger.error(Log.message("##### Download is Failed, please check! #####"));
				return null;
			}
		} catch (Exception e) {
			logger.error(Log.message("##### " + uid + " | Save User Profile Picture is failed !! ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish Saving User Profile Picture on server drive : " + destinationFile);
		return userProfilePictureUrl;
	}

	private String constructUserProfilePictureUrl(String uid) {
		String res = null;
		try {
			logger.info("==> Start construct User Profile Picture Url <== ");
			// Prefix?url=CDN_URL/HighLevel_Type/user_id.jpg&t=sub_type&n=imageName
			String userIDJPG = uid.concat(ImageServerConstants.JPG_FORMAT);
			String imageUrl = cdnURLPrefix.concat(ImageOf.PROFILE.type).concat(ImageServerConstants.FORWARD_SLASH)
					.concat(userIDJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String imageName = new String(Base64.encodeBase64(uid.getBytes()));
			String createTime = String.valueOf(System.currentTimeMillis());
			res = imageOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK)
					.concat(imageUrl).concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK)
					.concat(ImageOf.USERPROFILE.type).concat(ImageServerConstants.AND_MARK)
					.concat(ImageServerConstants.NAME_MARK).concat(imageName).concat(ImageServerConstants.AND_MARK)
					.concat(ImageServerConstants.TIME_MARK).concat(createTime);
			logger.info(uid + " Has Local Image URL : " + res);
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		logger.info("Finish construct User Profile Picture Url");
		return res;
	}

	// Store User Profile Picture into Drive
	private boolean storeImageToDestination(String image, String destinationFile) {
		logger.info("Argument image : "+image.length());
		// Check if the image url is valid or not
		if (image == null || image.trim().length() == 0) {
			logger.error(Log.message("##### " + image + " is null or empty value, please check ! #####"));
			return false;
		}

		// download image
		OutputStream os = null;
		try {
			File downloadFile = new File(destinationFile);

			File parentDir = downloadFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdir();
			}

			String imageDataBytes = image.substring(image.indexOf(",") + 1);
			logger.info("imageDataBytes : "+imageDataBytes.length());
			
			byte[] imageByte = Base64.decodeBase64(imageDataBytes.getBytes());
			os = new FileOutputStream(destinationFile);
			os.write(imageByte);
			os.flush();
		} catch (IOException e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
					os = null;
				}
				// check if download if successful
				if (new File(destinationFile).length() > 0) {
					logger.info(">>>>> Downloaded -- " + destinationFile + " <<<<<");
				} else {
					logger.info("##### The size of download file is not same as remote file, please check !  #####");
					return false;
				}
			} catch (IOException e) {
				logger.error(Log.message("##### " + e.getMessage() + " #####"));
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private String findExtensionFromImageData(String imageData) {
		String extension = null;
		if (StringUtils.isNotBlank(imageData)) {
			if (imageData.startsWith("data:image/jpg")) {
				extension = "jpg";
			} else if (imageData.startsWith("data:image/jpeg")) {
				extension = "jpeg";
			} else if (imageData.startsWith("data:image/gif")) {
				extension = "gif";
			} else if (imageData.startsWith("data:image/png")) {
				extension = "png";
			}
		}
		return extension;
	}
	
	// Save Stand Resolution Image
	private void saveStandResolutionImage(UserMediaContent userMediaContent, String uid, String imageName, Long standWidth, Long standHeight, String originalImagePath, String extension) {
		try {
			// initially need to get origianl dimensions, to compare with std res dimensions.
			Long standardResolutionWidth = standWidth;
			Long standardResolutionHeight = standHeight;

			String dimentions = standardResolutionWidth.toString().concat(ImageServerConstants.BY_MARK).concat(standardResolutionHeight.toString());

			if (standardResolutionWidth.longValue() >= ImageServerConstants.STD_WIDTH.longValue() && standardResolutionHeight.longValue() >= ImageServerConstants.STD_HEIGHT.longValue()) {
				dimentions = ImageServerConstants.STD_DIMENSION;
				standardResolutionWidth = ImageServerConstants.STD_WIDTH;
				standardResolutionHeight = ImageServerConstants.STD_HEIGHT;
			}

			dimentions = ImageServerConstants.UNDER_SCORE.concat(dimentions);

			String standResolutionImage = originalImagePath.replaceAll(extension, dimentions).concat(extension);
			Thumbnails.of(new File(originalImagePath)).size(standardResolutionWidth.intValue(), standardResolutionHeight.intValue()).toFile(new File(standResolutionImage));

			String standardResolutionURL = constructMediaUrl(uid, imageName, standardResolutionWidth.toString(), standardResolutionHeight.toString(), extension);

			userMediaContent.setStandardResolutionURL(standardResolutionURL);
			userMediaContent.setStandardResolutionWidth(standardResolutionWidth);
			userMediaContent.setStandardResolutionHeight(standardResolutionHeight);
			
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}

	// Save Low Resolution Image
	private void saveLowResolutionImage(UserMediaContent userMediaContent, String uid, String imageName, Long standWidth, Long standHeight, String originalImagePath, String extension) {
		try {
			// initially need to get origianl dimensions, to compare with low res dimensions.
			Long lowResolutionWidth = standWidth;
			Long lowResolutionHeight = standHeight;

			String dimentions = lowResolutionWidth.toString().concat(ImageServerConstants.BY_MARK).concat(lowResolutionHeight.toString());

			if (lowResolutionWidth.longValue() >= ImageServerConstants.LOW_WIDTH.longValue() && lowResolutionHeight.longValue() >= ImageServerConstants.LOW_HEIGHT.longValue()) {
				dimentions = ImageServerConstants.LOW_DIMENSION;
				lowResolutionWidth = ImageServerConstants.LOW_WIDTH;
				lowResolutionHeight = ImageServerConstants.LOW_HEIGHT;
			}

			dimentions = ImageServerConstants.UNDER_SCORE.concat(dimentions);

			String lowResolutionImage = originalImagePath.replaceAll(extension, dimentions).concat(extension);
			Thumbnails.of(new File(originalImagePath)).size(lowResolutionWidth.intValue(), lowResolutionHeight.intValue()).toFile(new File(lowResolutionImage));

			String lowResolutionURL = constructMediaUrl(uid, imageName, lowResolutionWidth.toString(), lowResolutionHeight.toString(), extension);

			userMediaContent.setLowResolutionURL(lowResolutionURL);
			userMediaContent.setLowResolutionWidth(lowResolutionWidth);
			userMediaContent.setLowResolutionHeight(lowResolutionHeight);
			
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
	
	// Save Thumbnail Resolution Image
	private void saveThumbnailResolutionImage(UserMediaContent userMediaContent, String uid, String imageName, Long standWidth, Long standHeight, String originalImagePath, String extension) {
		try {
			// initially need to get origianl dimensions, to compare with thumbnail res dimensions.
			Long thumbnailResolutionWidth = standWidth;
			Long thumbnailResolutionHeight = standHeight;

			String dimentions = thumbnailResolutionWidth.toString().concat(ImageServerConstants.BY_MARK).concat(thumbnailResolutionHeight.toString());

			if (thumbnailResolutionWidth.longValue() >= ImageServerConstants.THN_WIDTH.longValue() && thumbnailResolutionHeight.longValue() >= ImageServerConstants.THN_HEIGHT.longValue()) {
				dimentions = ImageServerConstants.THN_DIMENSION;
				thumbnailResolutionWidth = ImageServerConstants.THN_WIDTH;
				thumbnailResolutionHeight = ImageServerConstants.THN_HEIGHT;
			}

			dimentions = ImageServerConstants.UNDER_SCORE.concat(dimentions);

			String thumbnailResolutionImage = originalImagePath.replaceAll(extension, dimentions).concat(extension);
			Thumbnails.of(new File(originalImagePath)).size(thumbnailResolutionWidth.intValue(), thumbnailResolutionHeight.intValue()).toFile(new File(thumbnailResolutionImage));

			String thumbnailResolutionURL = constructMediaUrl(uid, imageName, thumbnailResolutionWidth.toString(), thumbnailResolutionHeight.toString(), extension);

			userMediaContent.setThumbnailURL(thumbnailResolutionURL);
			userMediaContent.setThumbnailWidth(thumbnailResolutionWidth);
			userMediaContent.setThumbnailHeight(thumbnailResolutionHeight);
			
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
	
	/**
	 * Store Media Content
	 */
	public List<UserMediaContent> updateMediaContent(List<String> medias, List<String> w, List<String> h, String uid) {
		List<UserMediaContent> responseList = new ArrayList<UserMediaContent>();
		if (medias == null || medias.size() == 0) {
			logger.error(Log.message("##### Images List is null or empty value, please check ! #####"));
			return responseList;
		}

		if (uid == null || uid.length() == 0) {
			logger.error(Log.message("##### " + uid + " is null or empty value, please check ! #####"));
			return responseList;
		}
		try {

			for (int i = 0; i < medias.size(); i++) {
				
				String imageData = medias.get(i);
				Long standWidth =  Long.parseLong(w.get(i));
				Long standHeight = Long.parseLong(h.get(i));
				
				String extension = ImageServerConstants.DOT_MARK.concat((findExtensionFromImageData(imageData)));
				
				// String imageName = uid.concat(ImageServerConstants.UNDER_SCORE).concat(getTimeStamp()).concat(ImageServerConstants.UNDER_SCORE).concat(ImageServerConstants.STD_DIMENSION);
				String imageName = uid.concat(ImageServerConstants.UNDER_SCORE).concat(getTimeStamp());
				
				// %prodnew.image.product.home.directory : /home/auto/syunus/image_host/
				// Destination : /home/auto/syunus/image_host/Media/{uid}/imageName.format
				String originalImagePath = productImageHomeDir.concat(ImageOf.POST.type).concat(File.separator).concat(uid).concat(File.separator).concat(imageName).concat(extension);

				boolean downloaded = storeImageToDestination(imageData, originalImagePath);
				if (downloaded) {
					// Create new User Media Content Object to host the info
					UserMediaContent mediaContent = new UserMediaContent();
					
					// Save Standard Res Image:
					saveStandResolutionImage(mediaContent, uid, imageName, standWidth, standHeight, originalImagePath, extension);
					
					// Save Low Res Image:
					saveLowResolutionImage(mediaContent, uid, imageName, standWidth, standHeight, originalImagePath, extension);
					
					// Save Thumbnail Res Image:
					saveThumbnailResolutionImage(mediaContent, uid, imageName, standWidth, standHeight, originalImagePath, extension);
					
					responseList.add(mediaContent);
					// new File(originalImagePath).delete();
					logger.info("Deleted Origianl Image ==> " + originalImagePath);
				} else {
					logger.error(Log.message("##### Store Media Content is Failed, please check! #####"));
					return responseList;
				}
			} 
		} catch (Exception e) {
			logger.error(Log.message("##### " + uid + " | Store Media Content is failed !! ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish Saving Media Content on server drive for user ==> " + uid);
		return responseList;
	}
	
	private String constructMediaUrl(String uid, String imageName, String w, String h, String extension) {
		String res = null;
		try {
			// imageName = {uid}_{timestamp}
			logger.info("==> Start construct Media Content Image Url with size" + w + " X " + h + " <== ");
			// Prefix?url=CDN_URL/HighLevel_Type/user_id.jpg&t=sub_type&n=imageName
			String imageNameJPG = imageName.concat(extension);
			
			// cdnURLPrefix : https://cdn.fountit.com/
			String imageUrl = cdnURLPrefix.concat(ImageOf.MEDIA.type).concat(ImageServerConstants.FORWARD_SLASH).concat(imageNameJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String imageNameNew = new String(Base64.encodeBase64(imageNameJPG.getBytes()));
			
			// imageOnISPrefix : https://affiliate.searshc.com/affiliate/getImage
			// https://affiliate.searshc.com/affiliate/getImage?url=CDN_URL/Media/uid_983719837193.jpg&t=Post&W=150&H=150&n=imageName
			res = mediaOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK).concat(imageUrl)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK).concat(ImageOf.POST.type)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.WIDTH_MARK).concat(w)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.HEIGHT_MARK).concat(h)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.NAME_MARK).concat(imageNameNew);
			logger.info(uid + " Has Deimensions : " + w + " X " + h +" Local Image URL : " + res);
		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			return res;
		}
		return res;
	}
	
	private String getTimeStamp() {
		String DateToStr = null;
		try {
			Date curDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmssSSS");
			DateToStr = format.format(curDate);
   		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
		}
		return DateToStr;
	}

	/*private String saveStandardImage(String image, String destinationFile) {
		String standardResolutionURL = null;
		try { // save the image on drive
			boolean downloaded = storeImageToDestination(image, destinationFile);
			// based on the result will consider add value to imageUrl_is or not
			if (downloaded) {
				standardResolutionURL = constructImageAPIUrl(imageUrl, seller_id, id, ORIGINALSIZE);
			} else {
				logger.error(Log.message("##### Download is Failed, please check! #####"));
				return false;
			}
		} catch (Exception e) {
			logger.error(Log.message("##### " + p.getId() + " | " + p.getImageURL() + " ##### "));
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			e.printStackTrace();
			return false;
		}
		return true;
	}*/

	// Image_Server_Approach_One

	/*private boolean saveThumbnailImage(Product p, File originalImage, String miniThumbnailPath,
			String mediumThumbnailPath, String bigThumbnailPath) {
		if (!originalImage.exists()) {
			logger.error(Log.message("##### This Origianl Image is not existing !! #####"));
			return false;
		}
		try { // create mini thumbnail String miniURL =
			constructImageAPIUrl(p.getImageURL(), String.valueOf(p.getSeller().getId()), String.valueOf(p.getId()),
					MINITHUMBNAIL);
			Thumbnails.of(originalImage).size(100, 100).outputFormat("jpg").toFile(new File(miniThumbnailPath));
			p.setImageMiniThumbnail(miniURL);

			// create medium thumbnail String mediumURL =
			constructImageAPIUrl(p.getImageURL(), String.valueOf(p.getSeller().getId()), String.valueOf(p.getId()),
					MEDIUMTHUMBNAIL);
			Thumbnails.of(originalImage).size(200, 200).outputFormat("jpg").toFile(new File(mediumThumbnailPath));
			p.setImageMediumThumbnail(mediumURL);

			// create big thumbnail String bigURL =
			constructImageAPIUrl(p.getImageURL(), String.valueOf(p.getSeller().getId()), String.valueOf(p.getId()),
					BIGTHUMBNAIL);
			Thumbnails.of(originalImage).size(300, 300).outputFormat("jpg").toFile(new File(bigThumbnailPath));
			p.setImageBigThumbnail(bigURL);

		} catch (Exception e) {
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
			e.printStackTrace();
			return false;
		}
		return true;
	}*/
	
	

	private static byte[] getCachedImageData(String imageCacheKey) {
		Object data = Cache.get(imageCacheKey);
		if (data != null) {
			if (data instanceof byte[]) {
				if (((byte[]) data).length == 0) {
					data = null;
				}
			} else {
				Cache.delete(imageCacheKey);
				data = null;
			}
		}
		return (byte[]) data;
	}
	
	/**
	 * Store Banner Content
	 */
	public List<EssentialsMedia> updateBannerOrCategoryContent(List<String> medias, List<String> w, List<String> h, String uid, String bannerOrCategoryName, String bannerOrCategoryType) {
		List<EssentialsMedia> responseList = new ArrayList<EssentialsMedia>();
		if (medias == null || medias.size() == 0) {
			logger.error(Log.message("##### Images List is null or empty value, please check ! #####"));
			return responseList;
		}

		if (uid == null || uid.length() == 0) {
			logger.error(Log.message("##### " + uid + " is null or empty value, please check ! #####"));
			return responseList;
		}
		
		
		try {

			for (int i = 0; i < medias.size(); i++) {
				
				String imageData = medias.get(i);
				logger.info("Width : " + w.get(i));
				Long standWidth =  Long.parseLong(w.get(i));
				Long standHeight = Long.parseLong(h.get(i));
								
				String extension = ImageServerConstants.DOT_MARK.concat((findExtensionFromImageData(imageData)));
				
				String imageName = uid.concat(ImageServerConstants.UNDER_SCORE).concat(bannerOrCategoryName.replaceAll("\\s+", "")).concat(ImageServerConstants.UNDER_SCORE).concat(getTimeStamp());
				
				// %prodnew.image.product.home.directory : /home/auto/syunus/image_host/
				// Destination : /home/auto/syunus/image_host/Banner(or Category)/{uid}/imageName.format
				String originalImagePath = "";
				if (bannerOrCategoryType.equalsIgnoreCase(ImageServerConstants.BANNER_TYPE)) {
					originalImagePath = productImageHomeDir.concat(ImageOf.BANNER.type).concat(File.separator).concat(uid).concat(File.separator).concat(imageName).concat(extension);
				} else {
					originalImagePath = productImageHomeDir.concat(ImageOf.CATEGORY.type).concat(File.separator).concat(uid).concat(File.separator).concat(imageName).concat(extension);
				}
				boolean downloaded = storeImageToDestination(imageData, originalImagePath);
				if (downloaded) {
					// Create new User Essential Media Object to host the info
					EssentialsMedia essentialsMedia = new EssentialsMedia();
					essentialsMedia.setMediaType(MediaType.IMAGE);
					
					// Save Standard Res Image:
					saveEssentialStandardResolutionImage(essentialsMedia, uid, imageName, standWidth, standHeight, originalImagePath, extension, bannerOrCategoryType);
					
					// Save Low Res Image only for Banner
					if (bannerOrCategoryType.equalsIgnoreCase(ImageServerConstants.BANNER_TYPE)) {
						saveEssentialLowResolutionImage(essentialsMedia, uid, imageName, ImageServerConstants.ESSENTIAL_BANNER_LOWWIDTH, ImageServerConstants.ESSENTIAL_BANNER_LOWHEIGHT, originalImagePath, extension, bannerOrCategoryType);
					}
					
					responseList.add(essentialsMedia);
					new File(originalImagePath).delete();
					logger.info("Deleted Origianl Image ==> " + originalImagePath);
				} else {
					logger.error(Log.message("##### Store Esential Content is Failed, please check! #####"));
					return responseList;
				}
				logger.info("Finish Saving Essential Content on server drive for user ==> " + uid);
			} 
		} catch (Exception e) {
			logger.error(Log.message("##### " + uid + " | Store Essential Content is failed !! ##### "));
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		return responseList;
	}
	
	// Save Stand Resolution Image
	private void saveEssentialStandardResolutionImage(EssentialsMedia essentialsMedia, String uid, String imageName, long standWidth, long standHeight, String originalImagePath, String extension, String bannerOrCategoryType) {
		try {
			// initially need to get origianl dimensions, to compare with std res dimensions.
			long standardResolutionWidth = standWidth;
			long standardResolutionHeight = standHeight;

			String dimentions = String.valueOf(standardResolutionWidth).concat(ImageServerConstants.BY_MARK).concat(String.valueOf(standardResolutionHeight));

			dimentions = ImageServerConstants.UNDER_SCORE.concat(dimentions);

			String standResolutionImage = originalImagePath.replaceAll(extension, dimentions).concat(extension);
			Thumbnails.of(new File(originalImagePath)).size(Ints.checkedCast(standardResolutionWidth), Ints.checkedCast(standardResolutionHeight)).keepAspectRatio(false).toFile(new File(standResolutionImage));
			
			String standardResolutionURL = constructBannerOrCategoryUrl(uid, imageName, String.valueOf(standardResolutionWidth),  String.valueOf(standardResolutionHeight), extension, bannerOrCategoryType);

			essentialsMedia.setStandardResolutionURL(standardResolutionURL);
			essentialsMedia.setStandardResolutionWidth(standardResolutionWidth);
			essentialsMedia.setStandardResolutionHeight(standardResolutionHeight);
			
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
	
	// Save Low Resolution Image
	private void saveEssentialLowResolutionImage(EssentialsMedia essentialsMedia, String uid, String imageName, long lowWidth, long lowHeight, String originalImagePath, String extension, String bannerOrCategoryType) {
		try {
			// initially need to get origianl dimensions, to compare with std res dimensions.
			long lowResolutionWidth = lowWidth;
			long lowResolutionHeight = lowHeight;

			String dimentions = String.valueOf(lowResolutionWidth).concat(ImageServerConstants.BY_MARK).concat(String.valueOf(lowResolutionHeight));

			dimentions = ImageServerConstants.UNDER_SCORE.concat(dimentions);

			String lowResolutionImage = originalImagePath.replaceAll(extension, dimentions).concat(extension);
			Thumbnails.of(new File(originalImagePath)).size(Ints.checkedCast(lowResolutionWidth), Ints.checkedCast(lowResolutionHeight)).toFile(new File(lowResolutionImage));

			String lowResolutionURL = constructBannerOrCategoryUrl(uid, imageName, String.valueOf(lowResolutionWidth),  String.valueOf(lowResolutionHeight), extension, bannerOrCategoryType);

			essentialsMedia.setLowResolutionURL(lowResolutionURL);
			essentialsMedia.setLowResolutionWidth(lowResolutionWidth);
			essentialsMedia.setLowResolutionHeight(lowResolutionHeight);
			
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
		
	private String constructBannerOrCategoryUrl(String uid, String imageName, String w, String h, String extension, String bannerOrCategoryType) {
		String res = null;
		String essType = null;
		try {
			if (bannerOrCategoryType.equalsIgnoreCase(ImageServerConstants.BANNER_TYPE)) {
				essType = ImageOf.BANNER.type;
			} else {
				essType = ImageOf.CATEGORY.type;
			} 
			// imageName = {uid}_{timestamp}
			logger.info("==> Start construct Essential Content Image Url with size" + w + " X " + h + " <== ");
			// Prefix?url=CDN_URL/HighLevel_Type/user_id.jpg&t=sub_type&n=imageName
			String imageNameJPG = imageName.concat(extension);
			
			// cdnURLPrefix : https://cdn.fountit.com/
			String imageUrl = cdnURLPrefix.concat(ImageOf.ESSENTIAL.type).concat(ImageServerConstants.FORWARD_SLASH).concat(imageNameJPG);
			imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
			String imageNameNew = new String(Base64.encodeBase64(imageNameJPG.getBytes()));
			
			// imageOnISPrefix : https://affiliate.searshc.com/affiliate/getImage
			// https://affiliate.searshc.com/affiliate/getImage?url=CDN_URL/Media/uid_983719837193.jpg&t=Post&W=150&H=150&n=imageName
			res = essentialOnISPrefix.concat(ImageServerConstants.QUESTION_MARK).concat(ImageServerConstants.URL_MARK).concat(imageUrl)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.TYPE_MARK).concat(essType)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.WIDTH_MARK).concat(w)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.HEIGHT_MARK).concat(h)
					.concat(ImageServerConstants.AND_MARK).concat(ImageServerConstants.NAME_MARK).concat(imageNameNew);
			logger.info(uid + " Has Deimensions : " + w + " X " + h +" Local Image URL : " + res);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Log.message("##### " + e.getMessage() + " #####"));
		}
		return res;
	}

}
