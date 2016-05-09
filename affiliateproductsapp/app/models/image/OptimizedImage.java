package models.image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.data.validation.Required;


import com.google.gson.annotations.Expose;



@Entity
@Table(name="OPTIMIZED_IMAGE")
public class OptimizedImage extends AbstractModel {

	 /* `user_id` BIGINT(20) NOT NULL , 
	  `image_name` varchar(128) NOT NULL,
	  `backup_name` varchar(128) DEFAULT NULL,
	  `is_ready` tinyint(1) NOT NULL DEFAULT '0',
	  `creation_date` datetime NOT NULL,
	  `updation_date` datetime NOT NULL,*/
	  
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OptimizedImage [id=");
		builder.append(id);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", imageName=");
		builder.append(imageName);
		builder.append(", backupName=");
		builder.append(backupName);
		builder.append(", isReady=");
		builder.append(isReady);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", updationDate=");
		builder.append(updationDate);
		builder.append("]");
		return builder.toString();
	}

	@Expose
	@Required
	@Column(name="user_id",nullable=false)
	public long userId;
	
	@Expose
	@Column(name="image_name" , unique=true ,nullable=false)
	@Required
	public String imageName;
	
	@Expose
	@Column(name="backup_name")
	@Required
	public String backupName;
		
	@Expose
	@Column(name="is_ready")
	public boolean isReady ;
	
	
	/**
	 * This methods checks that equality of makes.
	 * @param OptimizedImage
	 * @return true or false;
	 */
	public boolean equals(Object obj) {
		
		boolean equal = false;
		OptimizedImage make = null;
		
		if (obj instanceof OptimizedImage) {
			make = (OptimizedImage) obj;
		}
		
		if (!(this == null || make == null)) {
			
			if (this.hashCode() == make.hashCode() && this.id.equals(make.id)) {
				equal = true;
			}
		}
		
		return equal;
	}
	
	/* (non-Javadoc)
	 * @see play.db.jpa.JPABase#hashCode()
	 */
	public int hashCode() {

		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.id != null) {
			stringBuilder.append(this.id);
		}
		return stringBuilder.toString().hashCode();

	}
		
}