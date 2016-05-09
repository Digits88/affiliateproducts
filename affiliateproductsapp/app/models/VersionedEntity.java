package models;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class VersionedEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * This field is for optimistic locks.
	 */
	@Version
	@Column(name = "version", nullable = false)
	private Long version;
}
