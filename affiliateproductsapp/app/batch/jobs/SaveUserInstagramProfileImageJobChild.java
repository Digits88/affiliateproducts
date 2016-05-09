package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.Brand;
import models.User;
import models.UserInstagram;
import repositories.Repository;
import services.image.ImageService;
import utils.log.Log;

public class SaveUserInstagramProfileImageJobChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SaveUserInstagramProfileImageJobChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<UserInstagram> userList;

	@Transient
	private ImageService imageService = new ImageService();

	public SaveUserInstagramProfileImageJobChild(List<UserInstagram> userList) {
		super();
		this.userList = userList;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			for (UserInstagram user : userList) {
				String localImageUrl = imageService.saveUserInstagramImage(user);
				if (localImageUrl != null) {
					UserInstagram u = repository.find(UserInstagram.class, "from UserInstagram where id=?",
							user.getId());
					u.setLocalInstagramProfilePicture(localImageUrl);
					repository.update(u);
					logger.info("Finish Storing User Image: " + u.getInstagramUserName());
				} else {
					logger.error(Log.message("User : " + user.getInstagramUserName()
							+ " Cannot be downloaded profile image, Please Check Original URL First! "));
				}
				logger.info("----------------------------------------------------------------------");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
