package models;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.GenericModel;
import play.modules.guice.InjectSupport;
import repositories.Repository;

@MappedSuperclass
@InjectSupport
public abstract class BaseEntity extends GenericModel {

	private static final long serialVersionUID = 1L;

	public abstract Long getId();
	
	@Inject
	public static Repository repository;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creation_date")
	public Date creationDate;
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="updation_date")
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
}
