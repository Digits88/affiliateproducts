/**
 * 
 */
package utils.imagemagick.identify;

import java.io.Serializable;



/**
 * @author Greg Drogaline
 *
 */
public class ImageIdentity implements Identify , Serializable {



	/**
	 * @return the filesize
	 */
	public final long getFilesize() {
		return filesize;
	}

	/**
	 * @return the format
	 */
	public final String getFormat() {
		return format;
	}

	/**
	 * @return the image
	 */
	public final String getImage() {
		return image;
	}

	/**
	 * @return the wide
	 */
	public final Long getWide() {
		return wide;
	}

	/**
	 * @return the height
	 */
	public final Long getHeight() {
		return height;
	}

	/**
	 * @param fileSize
	 * @param format
	 * @param image
	 * @throws UnsupportedIdentity
	 */
	public ImageIdentity(long fileSize, String format, String image , Long wide , Long height) throws UnsupportedIdentity {
		super();

	
		if(format != null && SUPPORTED_FORMATS.contains(format)){
			this.format = format;	
		}else {
			throw new UnsupportedIdentity("unsupported image format ".concat(format).concat(" file ").concat(image));
		}
		this.filesize = fileSize;
		this.image = image;
		this.wide = wide;
		this.height = height;
	}

	private long filesize ;
	
	private String format;
	
	private String image;
	
	private Long wide;
	
	private Long height;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageIdentity [filesize=").append(filesize)
				.append(", format=").append(format).append(", image=")
				.append(image).append(", wide=").append(wide)
				.append(", height=").append(height).append("]");
		return builder.toString();
	}
	
}
