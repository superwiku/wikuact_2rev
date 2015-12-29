/**
 * Copyright 2014 Meruvian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meruvian.yama.social.linkedin;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.meruvian.yama.core.commons.FileInfo;
import org.meruvian.yama.core.user.User;
import org.meruvian.yama.social.core.AbstractSocialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfile;

/**
 * @author Dian Aditya
 *
 */
public class LinkedInService extends AbstractSocialService<LinkedIn> {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public LinkedInService(OAuth2ConnectionFactory<LinkedIn> linkedIn) {
		super(linkedIn);
	}

	@Override
	public User createUser(Connection<?> connection) {
		LinkedIn linkedIn = (LinkedIn) connection.getApi();
		LinkedInProfile profile = linkedIn.profileOperations().getUserProfile();
		
		String randomUsername = RandomStringUtils.randomAlphanumeric(6);
		
		User user = new User();
		user.setUsername(StringUtils.join(profile.getFirstName(), profile.getLastName(), randomUsername));
		user.getName().setFirst(profile.getFirstName());
		user.getName().setLast(profile.getLastName());
		user.setEmail(profile.getEmailAddress());
		
		if (StringUtils.isBlank(profile.getEmailAddress())) {
			user.setEmail(StringUtils.join(profile.getId(), "@linkedin.com"));
		}
		
		user.setPassword(RandomStringUtils.randomAlphanumeric(8));
		
		FileInfo fileInfo = new FileInfo();
		try {
			Resource resource = linkedIn.restOperations()
					.getForObject(profile.getProfilePictureUrl(), Resource.class);
			fileInfo.setDataBlob(resource.getInputStream());
			user.setFileInfo(fileInfo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return user;
	}

}
