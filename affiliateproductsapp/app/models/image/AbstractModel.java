package models.image;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.JPA;
import play.db.jpa.Model;

/**
 * Copy from makeapp project .. 
 *
 */
@MappedSuperclass
public abstract class AbstractModel extends Model
{
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date creationDate;
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="UPDATION_DATE")
	public Date updationDate;
    
	@PrePersist
	@PreUpdate
	public void setDates()
	{
	    Date now = new Date();
	    
	    if (creationDate == null)
	        creationDate = now;
	    updationDate = now;
	}
	
	public static Query getQuery(String query, int recordServed, int recordsPerPage) {
		
		Query jpaQuery = JPA.em().createQuery(query);
		jpaQuery.setFirstResult(recordServed);
		jpaQuery.setMaxResults(recordsPerPage);
		
		return jpaQuery;
	}
	
	public static Query getNativeQuery(Class clazz, String query, int recordServed, int recordsPerPage) {
		
		Query jpaQuery = JPA.em().createNativeQuery(query, clazz);
		jpaQuery.setFirstResult(recordServed);
		jpaQuery.setMaxResults(recordsPerPage);
		
		return jpaQuery;
	}

}