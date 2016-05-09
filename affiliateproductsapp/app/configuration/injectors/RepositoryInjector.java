package configuration.injectors;

import repositories.Repository;
import repositories.impl.JPARepository;

import com.google.inject.AbstractModule;

public class RepositoryInjector extends AbstractModule {

	@Override
	protected void configure() {
		// Configure dependencies in repository layer.
		bind(Repository.class).to(JPARepository.class);
	}
}
