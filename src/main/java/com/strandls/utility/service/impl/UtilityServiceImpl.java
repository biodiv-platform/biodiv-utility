/**
 * 
 */
package com.strandls.utility.service.impl;

import java.util.List;

import com.google.inject.Inject;
import com.strandls.utility.dao.FlagDao;
import com.strandls.utility.dao.FollowDao;
import com.strandls.utility.pojo.Flag;
import com.strandls.utility.pojo.FlagIbp;
import com.strandls.utility.pojo.Follow;
import com.strandls.utility.service.UtilityService;

/**
 * @author Abhishek Rudra
 *
 */
public class UtilityServiceImpl implements UtilityService {

	@Inject
	private FlagDao flagDao;

	@Inject
	private FollowDao followDao;

	@Override
	public Flag fetchByFlagId(Long id) {
		Flag flag = flagDao.findById(id);
		return flag;
	}

	@Override
	public FlagIbp fetchByFlagIdIbp(Long id) {
		Flag flag = flagDao.findById(id);
		FlagIbp ibp = new FlagIbp(flag.getFlag(), flag.getNotes());
		return ibp;
	}

	@Override
	public Flag fetchByFlagObject(String objectType, Long objectId) {
		Flag flag = flagDao.findByObjectId(objectType, objectId);
		return flag;
	}

	@Override
	public List<Flag> fetchFlagByUserId(Long id) {
		List<Flag> flags = flagDao.findByUserId(id);
		return flags;
	}

	@Override
	public Follow fetchByFollowId(Long id) {
		Follow follow = followDao.findById(id);
		return follow;
	}

	@Override
	public Follow fetchByFollowObject(String objectType, Long objectId, Long authorId) {
		Follow follow = followDao.findByObject(objectType, objectId, authorId);
		return follow;
	}

	@Override
	public List<Follow> fetchFollowByUser(Long authorId) {
		List<Follow> follows = followDao.findByUser(authorId);
		return follows;
	}

}
