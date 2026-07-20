package com.aiart.platform.mapper;

import com.aiart.platform.entity.TagCombinationStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface TagCombinationStatMapper extends BaseMapper<TagCombinationStat> {
    @Insert("""
            INSERT INTO tag_combination_stat (id, tag_a_id, tag_b_id, usage_count, last_used_at)
            VALUES (#{id}, #{tagAId}, #{tagBId}, 1, #{usedAt})
            ON DUPLICATE KEY UPDATE usage_count = usage_count + 1, last_used_at = VALUES(last_used_at)
            """)
    void increment(@Param("id") Long id,
                   @Param("tagAId") Long tagAId,
                   @Param("tagBId") Long tagBId,
                   @Param("usedAt") LocalDateTime usedAt);
}
