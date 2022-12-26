package com.soku.rebotcorner.mapper;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soku.rebotcorner.pojo.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
  @Select("" +
    "select id, create_time as time, result, reason, bot_ids as botIds, user_ids as userIds " +
    "from record " +
    "where game_id = #{game_id} " +
    "order by create_time desc " +
    "limit #{from}, #{count};"
  )
  List<JSONObject> getBaseRecordByGameId(@Param("game_id") Integer gameId, @Param("from") Integer from, @Param("count") Integer count);

  @Select("" +
    "select record_json " +
    "from record " +
    "where id = #{id};"
  )
  String getRecordJson(@Param("id") Integer id);
}
