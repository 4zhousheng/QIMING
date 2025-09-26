package com.zhousheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhousheng.domain.UserDo;
import com.zhousheng.dto.ProductSkuDto;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
public interface UserMapper extends BaseMapper<UserDo> {
    @Select("""
        SELECT
            id, username, password, nickname, create_time, update_time, avatar, money
        FROM
            `user`
        WHERE id=#{userId}
""")
    UserDo getUserDoByUserId(@Param("userId") Long userId);
    @Insert("""
        INSERT INTO
        user(id, username, password)
        VALUES
        (#{id},#{username},#{password})
""")
    int registerUser(UserDo user);

    @Update("""
        UPDATE
        user
        SET money=money-#{money}
        WHERE id = #{userId} AND money-#{money}>0
""")
    int payMoney(
           @Param("userId") Long userId,
           @Param("money") BigDecimal money);

}
