package dto;

import java.math.BigDecimal;


public class Location {

	private BigDecimal minLatitude;
	private BigDecimal maxLatitude;
	private BigDecimal minLongitude;
	private BigDecimal maxLongitude;
	public BigDecimal getMinLatitude() {
		return minLatitude;
	}
	public void setMinLatitude(BigDecimal minLatitude) {
		this.minLatitude = minLatitude;
	}
	public BigDecimal getMaxLatitude() {
		return maxLatitude;
	}
	public void setMaxLatitude(BigDecimal maxLatitude) {
		this.maxLatitude = maxLatitude;
	}
	public BigDecimal getMinLongitude() {
		return minLongitude;
	}
	public void setMinLongitude(BigDecimal minLongitude) {
		this.minLongitude = minLongitude;
	}
	public BigDecimal getMaxLongitude() {
		return maxLongitude;
	}
	public void setMaxLongitude(BigDecimal maxLongitude) {
		this.maxLongitude = maxLongitude;
	}

	

}
