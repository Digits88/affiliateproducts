package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.EssentialsMedia;
import models.Product;
import models.Seller;
import models.User;
import models.UserInstagram;
import models.image.MediaParams;
import models.image.UserMediaContent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import constants.ImageServerConstants;
import controllers.response.AffiliateResponse;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import repositories.Repository;
import services.CategoryService;
import services.image.ImageService;
import utils.JsonUtils;
import utils.image.ImageUtils;
import utils.log.Log;

public class ImageController extends Controller {

	@Inject
	protected static Repository repository;

	static String DWB_URL = "dwb";

	static Logger logger = LogManager.getLogger(ImageController.class);

	/**
	 * @param id
	 * @param name
	 */
	public static void getImage(Long id, String name, boolean reload) {

		boolean imageoptimized = true;

		logger.debug(String.format("Image request %s %s %s %s ", ", id", id, ", name", name));
		logger.info(Log.message("&&& Image request... id : " + id + " | name : " + name));

		String url = Http.Request.current().url;
		if (!isValidUrl(url)) {
			return;
		}
		boolean dwb = isDwb(url);

		String filename = getFileName(id, name, dwb);

		logger.debug(String.format("Request url %s %s %s", url, ", file name", filename));
		logger.info(Log.message("&&& Request url : " + url + " | File Name : " + filename));

		File imgOutFile = new File(filename);

		if (imgOutFile.exists()) {
			//
			// File process and cash reload ..
			// setHash for reload and process validation .. Job will evaluate it
			// any actions needed ..
			String dbname = id.toString().concat("/").concat(name);
			String optimized = ImageService.getInstance().optimize(imgOutFile, dbname);

			if (!optimized.equals(imgOutFile.getAbsolutePath())) {
				logger.debug(String.format("Image not optimised %s", filename));
				logger.info(Log.message("&&& Image not optimised : " + filename));
				imageoptimized = false;
				// optimization in process will cache or return optimized
				// version ones its done ..
				imgOutFile = new File(optimized);
				if (imgOutFile == null || !imgOutFile.exists()) {
					logger.error(
							String.format("ERROR : archive optimization file %s %s", optimized, "does not exist.."));
					logger.error(Log.message("ERROR : archive optimization file : " + optimized + " does not exist.."));
					return;
				}
			} else
				logger.debug(String.format("%s %s %s", optimized, "equals", imgOutFile.getAbsolutePath()));
			//
			byte[] data = null;
			String imageCacheKey = imgOutFile.getAbsolutePath();
			if (ImageUtils.isCacheenabled() && imageoptimized) {
				logger.debug("image cache enabled..");
				data = getCachedImageData(imageCacheKey);
			} else {
				logger.info("image cache not enabled..");
			}

			try {
				if (data == null || reload) {
					logger.debug(String.format("File %s %s", imgOutFile.getAbsolutePath(), "not yet cached.."));

					data = FileUtils.readFileToByteArray(imgOutFile);
					if (ImageUtils.isCacheenabled() && imageoptimized) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s",
									imgOutFile.getAbsolutePath()));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(String.format("Trying to cache image file %s", imgOutFile.getAbsolutePath()));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}

				String extension = ImageUtils.getExtension(imgOutFile.getName());
				if (StringUtils.isBlank(extension)) {
					extension = "jpg";
				}

				logger.debug("image exstention ".concat(extension));

				Response.current().contentType = "image/" + extension;
				Response.current().setHeader("cache-control",
						"max-age=" + Play.configuration.getProperty("http.cacheControl", "3600"));

				renderBinary(new ByteArrayInputStream(data));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file", imgOutFile.getAbsolutePath(),
					"not exist.."));
		}

	}

	/**
	 * @param id
	 * @param name
	 * @param dimension
	 */
	public static void getImageDimension(Long id, String name, String dimension, boolean reload) {

		logger.debug(String.format("ImageDimention request %s %s %s %s %s %s", ", id", id, ", name", name,
				", dimension", dimension));

		String url = Http.Request.current().url;
		if (!isValidUrl(url)) {
			return;
		}
		if (!ImageUtils.isTranscodEnabled()) {
			logger.debug("Transcode not enabled redirect to image get...");
			getImage(id, name, reload);
			return;
		}
		boolean dwb = isDwb(url);

		String filename = getFileName(id, name, dwb);

		logger.debug(String.format("Request url %s %s %s", url, ", file name", filename));

		File imgOutFile = new File(filename);

		if (imgOutFile.exists()) {

			//
			byte[] data = null;
			String imageCacheKey = imgOutFile.getAbsolutePath() + dimension;
			if (ImageUtils.isCacheenabled()) {
				logger.debug("image cache enabled..");
				data = getCachedImageData(imageCacheKey);
			} else {
				logger.info("image cache not enabled..");
			}

			String extension = null;
			try {
				if (data == null || reload) {

					// get converted file here ..
					//
					String[] dim = dimension.split("x");

					logger.debug(String.format("File %s %s  Requested dimensions:[ wide %s , crop height %s]",
							imgOutFile.getAbsolutePath(), "not yet cached.. ", dim[0],
							(dim.length == 2 ? dim[1] : "not specified")));
					Long wide = null;
					Long cropHeight = null;
					if (dim != null && dim.length > 0 && dim[0] != null) {
						try {
							wide = Long.valueOf(dim[0]);
						} catch (NumberFormatException nfe) {
							logger.error(String.format("Wrong Transcode dimension wide value, ignore % image request..",
									imgOutFile.getAbsolutePath()), nfe);
							return;
						}
					}

					if (dim != null && dim.length > 1 && dim[1] != null) {
						try {
							cropHeight = Long.valueOf(dim[1]);
						} catch (NumberFormatException nfe) {
							logger.error(String.format(
									"Wrong Transcode dimension crop height value, ignore % image request..",
									imgOutFile.getAbsolutePath()), nfe);
							return;
						}
					}

					String newDimensionsFileName = ImageService.getInstance().transcode(imgOutFile.getAbsolutePath(),
							wide, cropHeight);

					logger.debug(String.format("Temp file %s for requested dimensions..", newDimensionsFileName));

					if (newDimensionsFileName == null) {
						logger.error("File transcode error ...");
						return;
					}

					File newDimensions = new File(newDimensionsFileName);
					if (!newDimensions.exists()) {
						logger.error("File transcode error , file not exist ...");
						return;
					}
					extension = ImageUtils.getExtension(newDimensions.getName());

					data = FileUtils.readFileToByteArray(newDimensions);

					if (ImageUtils.isCacheenabled()) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s", newDimensionsFileName));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(
									String.format("Trying to cache converted image file %s", newDimensionsFileName));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
					newDimensions.delete();

				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}
				if (StringUtils.isBlank(extension)) {
					extension = "jpg";
				}

				logger.debug("image exstention ".concat(extension));

				Response.current().contentType = "image/" + extension;
				Response.current().setHeader("cache-control",
						"max-age=" + Play.configuration.getProperty("http.cacheControl", "3600"));

				renderBinary(new ByteArrayInputStream(data));

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file", imgOutFile.getAbsolutePath(),
					"not exist.."));
		}
	}

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

	private static boolean isDwb(String url) {
		return url.startsWith(ImageUtils.getImagecontext().concat(DWB_URL));
	}

	public static void doNothing() {

		logger.debug(String.format("doNothing Request url %s", Http.Request.current().url));

	}

	private static boolean isValidUrl(String url) {

		if (url == null || url.trim().length() < ImageUtils.getImagecontext().length() || "/".equals(url)) {
			logger.debug(String.format("Not valid request %s", url));
			return false;
		}

		return true;

	}

	private static String getFileName(Long id, String name, boolean dwb) {
		if (!dwb)
			return ImageUtils.getDirectory().concat(id.toString()).concat(File.separator).concat(name);
		else
			return ImageUtils.getDwbDirectory().concat(id.toString()).concat(File.separator).concat(name);
	}

	private static String getFileName2(Long id, String name, boolean dwb) {
		if (!dwb)
			return ImageUtils.getDirectory().concat(id.toString()).concat(File.separator).concat(name);
		else
			return ImageUtils.getDwbDirectory().concat(id.toString()).concat(File.separator).concat(name);
	}

	/**
	 * id -- product id sid -- seller id
	 * 
	 * @throws Base64DecodingException
	 */
	// Image_Server_Approach_Two
	// #GET /getImage ImageController.getISImage
	public static void getISImage(String url, Long sid, String id, String w, String h, boolean reload) {

		try {
			logger.debug(String.format("ImageDimention request %s %s %s %s %s %s %s %s %s %s", ", url", url, ", sid",
					sid, ", id", id, ", w", w, ", h", h));

			String currentUrl = Http.Request.current().url;
			if (!isValidUrl(currentUrl)) {
				return;
			}
			// To get the product image name, first need to decode id
			String name = new String(Base64.decode(id));
			name = name.concat(".jpg");
			if (!ImageUtils.isTranscodEnabled()) {
				logger.debug("Transcode not enabled redirect to image get...");
				getImage(sid, name, reload);
				return;
			}
			boolean dwb = isDwb(currentUrl);

			String filename = getFileName(sid, name, dwb);

			logger.debug(String.format("Request url %s %s %s", url, ", file name", filename));

			File imgOutFile = new File(filename);

			if (imgOutFile.exists()) {

				//
				byte[] data = null;
				String imageCacheKey = (imgOutFile.getAbsolutePath() + w + "x" + h).replaceAll("\\s+", "");
				if (ImageUtils.isCacheenabled()) {
					logger.debug("image cache enabled..");
					data = getCachedImageData(imageCacheKey);
				} else {
					logger.info("image cache not enabled..");
				}

				String extension = null;
				if (data == null || reload) {

					// get converted file here ..
					//
					// String[] dim = dimension.split("x");

					logger.debug(String.format("File %s %s  Requested dimensions:[ wide %s , crop height %s]",
							imgOutFile.getAbsolutePath(), "not yet cached.. ", w,
							(h != null && h.trim().length() > 0 ? h : "not specified")));
					Long wide = null;
					Long cropHeight = null;
					if (w != null && w.trim().length() > 0) {
						try {
							wide = Long.valueOf(w);
						} catch (NumberFormatException nfe) {
							logger.error(String.format("Wrong Transcode dimension wide value, ignore % image request..",
									imgOutFile.getAbsolutePath()), nfe);
							return;
						}
					}

					if (h != null && h.trim().length() > 0) {
						try {
							cropHeight = Long.valueOf(h);
						} catch (NumberFormatException nfe) {
							logger.error(String.format(
									"Wrong Transcode dimension crop height value, ignore % image request..",
									imgOutFile.getAbsolutePath()), nfe);
							return;
						}
					}

					String newDimensionsFileName = ImageService.getInstance().transcode(imgOutFile.getAbsolutePath(),
							wide, cropHeight);

					logger.debug(String.format("Temp file %s for requested dimensions..", newDimensionsFileName));

					if (newDimensionsFileName == null) {
						logger.error("File transcode error ...");
						return;
					}

					File newDimensions = new File(newDimensionsFileName);
					if (!newDimensions.exists()) {
						logger.error("File transcode error , file not exist ...");
						return;
					}
					extension = ImageUtils.getExtension(newDimensions.getName());

					data = FileUtils.readFileToByteArray(newDimensions);

					if (ImageUtils.isCacheenabled()) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s", newDimensionsFileName));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(
									String.format("Trying to cache converted image file %s", newDimensionsFileName));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
					newDimensions.delete();

				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}
				if (StringUtils.isBlank(extension)) {
					extension = "jpg";
				}

				logger.debug("image exstention ".concat(extension));

				Response.current().contentType = "image/" + extension;
				Response.current().setHeader("cache-control",
						"max-age=" + Play.configuration.getProperty("http.cacheControl", "3600"));

				renderBinary(new ByteArrayInputStream(data));

			} else {
				logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file",
						imgOutFile.getAbsolutePath(), "not exist.."));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}
	}

	private static String getFileNameFromURL(Long sid, String id, String w, String h) throws Base64DecodingException {

		// directory = Play.configuration.getProperty("image.directory");
		// image.directory=C:\\Users\\lwan0\\Desktop\\image\\product\\

		// New Path should be like this:
		// C:\Users\lwan0\Desktop\image\product\18\2561_100x100.jpg

		id = new String(Base64.decode(id));

		String path = ImageUtils.getDirectory().concat(sid.toString()).concat(File.separator).concat(id.toString());
		if (w != null && h != null) {
			path = path.concat("_").concat(w).concat("x").concat(h);
		}
		path = path.concat(".jpg");
		return path;
	}

	// Image_Server_Approach_One
	public static void getDifferentImageDimension(String url, Long sid, String id, String w, String h, boolean reload)
			throws UnsupportedEncodingException, NumberFormatException, Base64DecodingException {

		logger.debug(String.format("ImageDimention request %s %s %s %s %s %s %s %s %s %s", ", url", url, ", sid", sid,
				", id", id, ", w", w, ", h", h));

		// check full url
		String fullURL = Http.Request.current().url;
		if (!isValidUrl(fullURL)) {
			return;
		}

		// check each parameter is valid

		// construct the file name
		String filename = getFileNameFromURL(sid, id, w, h);

		logger.debug(String.format("Request url %s %s %s", fullURL, ", file name", filename));

		File imgOutFile = new File(filename);

		// Check if the image is exist in our drive, if not --> check orignal
		// image url, see the else loop
		if (!imgOutFile.exists()) {

			logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file", imgOutFile.getAbsolutePath(),
					"not exist on our drive.."));
					// if the image is not on drive, then read from url directly
					// and
					// also download from image url and also generate 4 size
					// images

			// get the original url
			// String orignalURL = URLDecoder.decode(url, "UTF-8");
			Long productid = Long.valueOf(new String(Base64.decode(id)));
			Product p = repository.find(Product.class, "from Product where seller_id=? and id=?", sid, productid);

			// Image_Server_Approach_One
			// new ImageService().saveImageInFourType(p);
		}

		byte[] data = null;

		// check if it's in cache
		String imageCacheKey = imgOutFile.getAbsolutePath();
		if (ImageUtils.isCacheenabled()) {
			logger.debug("image cache enabled..");
			data = getCachedImageData(imageCacheKey);
		} else {
			logger.info("image cache not enabled..");
		}

		String extension = null;
		try {
			if (data == null || reload) {

				data = FileUtils.readFileToByteArray(imgOutFile);

				if (ImageUtils.isCacheenabled()) {
					if (data != null && reload) {
						logger.debug(String.format("Trying to reload cached image file %s", imgOutFile));
						Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
					} else {
						logger.debug(String.format("Trying to cache converted image file %s", imgOutFile));
						Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
					}
				}
			} else {
				logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
			}
			if (StringUtils.isBlank(extension)) {
				extension = "jpg";
			}

			logger.debug("image exstention ".concat(extension));

			Response.current().contentType = "image/" + extension;
			Response.current().setHeader("cache-control",
					"max-age=" + Play.configuration.getProperty("http.cacheControl", "3600"));

			renderBinary(new ByteArrayInputStream(data));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getProfileImageName(String type, String sellerImageName) {
		return ImageUtils.getDirectory().concat(type).concat(File.separator).concat(sellerImageName);
	}

	/**
	 * 
	 * 
	 * Route: GET /getImage ImageController.getISImage
	 */

	public static void getImageFromServer(String url, String t, String n, String c, boolean reload) {

		try {
			logger.debug(String.format("ProfileImage request %s %s %s %s %s %s %s %s", ", url", url, ", t", t, ", n", n,
					", c", c));

			// Need to think about if the internal url is not valid!!
			url = URLDecoder.decode(url, "UTF-8");
			String currentUrl = Http.Request.current().url;
			if (!isValidUrl(currentUrl)) {
				return;
			}
			// To get the image name, first need to decode
			String imageName = new String(Base64.decode(n));
			imageName = imageName.concat(ImageServerConstants.JPG_FORMAT);

			String filename = getProfileImageName(t, imageName);

			logger.info("Find the image for " + t + " @  " + filename);

			File imgOutFile = new File(filename);

			if (imgOutFile.exists()) {
				byte[] data = null;
				String imageCacheKey = imgOutFile.getAbsolutePath();
				if (ImageUtils.isCacheenabled()) {
					logger.debug("image cache enabled..");
					data = getCachedImageData(imageCacheKey);
				} else {
					logger.info("image cache not enabled..");
				}

				String extension = null;
				if (data == null || reload) {
					/*
					 * logger.debug(String.format(
					 * "File %s %s  Requested dimensions:[ wide %s , crop height %s]"
					 * , imgOutFile.getAbsolutePath(), "not yet cached.. ", w,
					 * (h != null && h.trim().length() > 0 ? h : "not specified"
					 * ))); Long wide = null; Long cropHeight = null; if (w !=
					 * null && w.trim().length() > 0) { try { wide =
					 * Long.valueOf(w); } catch (NumberFormatException nfe) {
					 * logger.error(String.format(
					 * "Wrong Transcode dimension wide value, ignore % image request.."
					 * , imgOutFile.getAbsolutePath()), nfe); return; } }
					 * 
					 * if (h != null && h.trim().length() > 0) { try {
					 * cropHeight = Long.valueOf(h); } catch
					 * (NumberFormatException nfe) { logger.error(String.format(
					 * "Wrong Transcode dimension crop height value, ignore % image request.."
					 * , imgOutFile.getAbsolutePath()), nfe); return; } }
					 * 
					 * String newDimensionsFileName =
					 * ImageService.getInstance().transcode(imgOutFile.
					 * getAbsolutePath(), wide, cropHeight);
					 * 
					 * logger.debug(String.format(
					 * "Temp file %s for requested dimensions..",
					 * newDimensionsFileName));
					 * 
					 * if (newDimensionsFileName == null) { logger.error(
					 * "File transcode error ..."); return; }
					 * 
					 * File newDimensions = new File(newDimensionsFileName); if
					 * (!newDimensions.exists()) { logger.error(
					 * "File transcode error , file not exist ..."); return; }
					 * extension =
					 * ImageUtils.getExtension(newDimensions.getName());
					 */

					data = FileUtils.readFileToByteArray(imgOutFile);

					if (ImageUtils.isCacheenabled()) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s", imgOutFile.getName()));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(
									String.format("Trying to cache converted image file %s", imgOutFile.getName()));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
					// newDimensions.delete();

				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}
				if (StringUtils.isBlank(extension)) {
					extension = "jpg";
				}

				logger.debug("image exstention ".concat(extension));

				Response.current().contentType = "image/" + extension;
				Response.current().setHeader("cache-control",
						"max-age=" + Play.configuration.getProperty("http.cacheControl", "3600"));

				renderBinary(new ByteArrayInputStream(data));

			} else {
				logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file",
						imgOutFile.getAbsolutePath(), "not exist.."));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}
	}

	private static String getMediaContentName(String type, String uid, String imageName) {
		return ImageUtils.getDirectory().concat(type).concat(File.separator).concat(uid).concat(File.separator)
				.concat(imageName);
	}

	private static String getExtension(String imageName) {
		String[] list = imageName.split("\\.");
		return list[list.length - 1];
	}

	public static void getMediaFromServer(String url, String t, String w, String h, String n, boolean reload) {

		try {
			logger.debug(String.format("Media Image request %s %s %s %s %s %s %s %s %s %s", ", url", url, ", t", t,
					", w", w, ", h", h, ", n", n));

			// Need to think about if the internal url is not valid!!
			url = URLDecoder.decode(url, "UTF-8");
			String currentUrl = Http.Request.current().url;
			if (!isValidUrl(currentUrl)) {
				return;
			}
			// To get the image name, first need to decode
			String imageName = new String(Base64.decode(n));
			String extension = ImageServerConstants.DOT_MARK.concat(getExtension(imageName));
			// logger.info("Image : " + imageName);
			String uid = imageName.split("_")[0];
			// logger.info("uid : " + uid);
			String dimension = w.concat(ImageServerConstants.BY_MARK).concat(h);
			// logger.info("dimension : " + dimension);
			imageName = imageName.replaceAll(extension, "").concat(ImageServerConstants.UNDER_SCORE).concat(dimension)
					.concat(extension);
			// imageName =
			// imageName.concat(ImageServerConstants.UNDER_SCORE).concat(dimension).concat(ImageServerConstants.DOT_MARK).concat(extension);

			String filename = getMediaContentName(t, uid, imageName);
			logger.info("Find the image for " + t + " @  " + filename);

			File imgOutFile = new File(filename);

			if (imgOutFile.exists()) {
				byte[] data = null;
				String imageCacheKey = imgOutFile.getAbsolutePath();
				if (ImageUtils.isCacheenabled()) {
					logger.debug("image cache enabled..");
					data = getCachedImageData(imageCacheKey);
				} else {
					logger.info("image cache not enabled..");
				}

				// String extension = null;
				if (data == null || reload) {
					data = FileUtils.readFileToByteArray(imgOutFile);

					if (ImageUtils.isCacheenabled()) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s", imgOutFile.getName()));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(
									String.format("Trying to cache converted image file %s", imgOutFile.getName()));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
					// newDimensions.delete();

				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}
				/*
				 * if (StringUtils.isBlank(extension)) { extension = "jpg"; }
				 */
				logger.debug("image exstention ".concat(extension));
				/*
				 * Response.current().contentType = "image/" + extension;
				 * Response.current().setHeader("cache-control", "max-age=" +
				 * Play.configuration.getProperty("http.cacheControl", "3600"));
				 */
				renderBinary(new ByteArrayInputStream(data));

			} else {
				logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file",
						imgOutFile.getAbsolutePath(), "not exist.."));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}
	}

	private static String getEssentialContentName(String type, String uid, String imageName) {
		return ImageUtils.getDirectory().concat(type).concat(File.separator).concat(uid).concat(File.separator)
				.concat(imageName);
	}
	
	// For Essential
	public static void getEssentialFromServer(String url, String t, String w, String h, String n, boolean reload) {

		try {
			logger.debug(String.format("Media Image request %s %s %s %s %s %s %s %s %s %s", ", url", url, ", t", t,
					", w", w, ", h", h, ", n", n));

			// Need to think about if the internal url is not valid!!
			url = URLDecoder.decode(url, "UTF-8");
			String currentUrl = Http.Request.current().url;
			if (!isValidUrl(currentUrl)) {
				return;
			}
			// To get the image name, first need to decode
			String imageName = new String(Base64.decode(n));
			String extension = ImageServerConstants.DOT_MARK.concat(getExtension(imageName));
			// logger.info("Image : " + imageName);
			String uid = imageName.split("_")[0];
			// logger.info("uid : " + uid);
			String dimension = w.concat(ImageServerConstants.BY_MARK).concat(h);
			// logger.info("dimension : " + dimension);
			imageName = imageName.replaceAll(extension, "").concat(ImageServerConstants.UNDER_SCORE).concat(dimension)
					.concat(extension);
			// imageName =
			// imageName.concat(ImageServerConstants.UNDER_SCORE).concat(dimension).concat(ImageServerConstants.DOT_MARK).concat(extension);

			String filename = getEssentialContentName(t, uid, imageName);
			logger.info("Find the image for " + t + " @  " + filename);

			File imgOutFile = new File(filename);

			if (imgOutFile.exists()) {
				byte[] data = null;
				String imageCacheKey = imgOutFile.getAbsolutePath().replaceAll("\\s+", "");
				logger.info("imageCacheKey : " + imageCacheKey);
				if (ImageUtils.isCacheenabled()) {
					logger.debug("image cache enabled..");
					data = getCachedImageData(imageCacheKey);
				} else {
					logger.info("image cache not enabled..");
				}

				// String extension = null;
				if (data == null || reload) {
					data = FileUtils.readFileToByteArray(imgOutFile);

					if (ImageUtils.isCacheenabled()) {
						if (data != null && reload) {
							logger.debug(String.format("Trying to reload cached image file %s", imgOutFile.getName()));
							Cache.replace(imageCacheKey, data, ImageUtils.getCachetime());
						} else {
							logger.debug(
									String.format("Trying to cache converted image file %s", imgOutFile.getName()));
							Cache.add(imageCacheKey, data, ImageUtils.getCachetime());
						}
					}
					// newDimensions.delete();

				} else {
					logger.debug(String.format("Found %s %s", imgOutFile.getAbsolutePath(), "in cache.."));
				}
				/*
				 * if (StringUtils.isBlank(extension)) { extension = "jpg"; }
				 */
				logger.debug("image exstention ".concat(extension));
				/*
				 * Response.current().contentType = "image/" + extension;
				 * Response.current().setHeader("cache-control", "max-age=" +
				 * Play.configuration.getProperty("http.cacheControl", "3600"));
				 */
				renderBinary(new ByteArrayInputStream(data));

			} else {
				logger.debug(String.format("Request url ignored %s %s %s %s", url, ",file",
						imgOutFile.getAbsolutePath(), "not exist.."));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}
	}

	public static void saveUserInstagramProfileImage(String key, Long id) throws Exception {
		User u = User.findById(id);
		UserInstagram user = u.getUserInstagram();
		logger.info("Start Storing User Image: " + user.getInstagramUserName());
		ImageService imageService = new ImageService();
		String localImageUrl = imageService.saveUserInstagramImage(user);
		user.setLocalInstagramProfilePicture(localImageUrl);
		repository.update(user);
		logger.info("Finish Storing User Image: " + user.getInstagramUserName());
		renderJSON("OK");
	}

	// Job For Update User Profile Picture Image
	public static void updateUserProfilePictureImage(String image, String uid) {
		ImageService imageService = new ImageService();
		String userProfilePictureUrl = imageService.updateUserProfilePicture(image, uid);
		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.USERPROFILEPICTUREURL, userProfilePictureUrl);
		String json = JsonUtils.toJson(affiliateResponse);
		// logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		logger.info(Log.message("Exiting"));
		renderJSON(json);
	}

	// Job For Update Media Content
	public static void updateMediaContent(List<String> medias, List<String> width, List<String> height, String userId) {
		for (Map.Entry<String, String[]> param : request.params.all()
				.entrySet()) {
			String paramKey = param.getKey();
			String[] paramValues = param.getValue();
			
			for (String paramValue : paramValues) {
				logger.info(paramKey + " : " +paramValue);
			}
		}
		ImageService imageService = new ImageService();
		List<UserMediaContent> responseList = imageService.updateMediaContent(medias, width, height, userId);
		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.USERMEDIACONTENTS, responseList);
		String json = JsonUtils.toJson(affiliateResponse);
		logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		// logger.info(Log.message("Exiting"));
		renderJSON(json);
	}
	
	// Job For Update Essential Content
	public static void updateEssentialContent(List<String> mediass, List<String> widths, List<String> heights, String userIds,  String bcName, String bcType) {
		for (Map.Entry<String, String[]> param : request.params.all()
				.entrySet()) {
			String paramKey = param.getKey();
			String[] paramValues = param.getValue();
			
			for (String paramValue : paramValues) {
				logger.info(paramKey + " : " +paramValue);
			}
		}
		/*logger.info("UserIds : " + userIds);
		logger.info("Widths : " + widths);
		logger.info("Heights : " + heights);*/
		ImageService imageService = new ImageService();
		List<EssentialsMedia> responseList = imageService.updateBannerOrCategoryContent(mediass, widths, heights, userIds, bcName, bcType);
		
		
		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.ESSENTIALCONTENT, responseList);
		String json = JsonUtils.toJson(affiliateResponse);
		logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		// logger.info(Log.message("Exiting"));
		renderJSON(json);
	}
}