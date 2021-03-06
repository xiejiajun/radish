package org.sam.shen.scheduing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.AgentGroup;

@Mapper
public interface AgentGroupMapper {
	
	AgentGroup findAgentGroupById(Long id);

	void saveAgentGroup(AgentGroup agentGroup);
	
	List<AgentGroup> queryAgentGroup();

    List<AgentGroup> queryUserAgentGroup(Long userId);

	List<AgentGroup> queryAgentGroupByName(@Param("groupName") String groupName);
	
	Long countAgentGroup(@Param("userId") Long userId);
	
	void upgradeAgentGroup(AgentGroup agentGroup);
	
	void deleteAgentGroup(Long id);
}
