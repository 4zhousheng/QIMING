package com.zhousheng.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//这里主要展示用户个人主页的信息

@Data
public class UserDetailVo {
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
    private BigDecimal money;
}
