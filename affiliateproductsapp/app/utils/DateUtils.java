package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static final Long SECONDS_IN_MINUTE = 60L;
	public static final Long SECONDS_IN_HOUR = 3600L;
	public static final Long SECONDS_IN_DAY = 86400L;
	public static final Long SECONDS_IN_WEEK = 604800L;
	
	/**
	 * @param timestamp
	 * @return Age
	 * 
	 */
	public static String returnAge(Date timestamp){
		String age = "";
		
		Date currentTime = new Date();
		Long timeDiff = (currentTime.getTime() - timestamp.getTime()) / 1000;
		
		if(timeDiff > SECONDS_IN_WEEK){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
			age = sdf.format(timestamp);
		}else if(timeDiff <= (SECONDS_IN_WEEK) && timeDiff > (SECONDS_IN_DAY)){
			Long ageInDays = (timeDiff / (SECONDS_IN_DAY));
			if(ageInDays == 1){
				age = ageInDays.toString() + " day ago";
			}else{
				age = ageInDays.toString() + " days ago";
			}
		}else if(timeDiff <= (SECONDS_IN_DAY) && timeDiff > (SECONDS_IN_HOUR)){
			Long ageInHours = timeDiff / (SECONDS_IN_HOUR);
			if(ageInHours == 1){
				age = ageInHours.toString() + " hour ago";
			}else{
				age = ageInHours.toString() + " hours ago";
			}				
		}else if(timeDiff <= (SECONDS_IN_HOUR) && timeDiff > SECONDS_IN_MINUTE){
			Long ageInMinutes = timeDiff / SECONDS_IN_MINUTE;
			if(ageInMinutes == 1){
				age = ageInMinutes.toString() + " minute ago";
			}else{
				age = ageInMinutes.toString() + " minutes ago";
			}
		}else if(timeDiff <= SECONDS_IN_MINUTE){
			age = "just now";
		}
		return age;
	}
	
}
