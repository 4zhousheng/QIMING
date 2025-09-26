package com.zhousheng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhousheng.common.exception.InsufficientBalanceException;
import com.zhousheng.common.exception.RechargeFailException;
import com.zhousheng.domain.UserDo;
import com.zhousheng.dto.UserRegisterDto;
import com.zhousheng.vo.UserDetailVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

public interface UserService extends IService<UserDo> {
//    注册用户
    int registerUser(UserRegisterDto user);
//    根据用户名获取用户对象
    UserDo getUserById(Long userId);
//    获取用户信息
    UserDetailVo getUserInfoById(Long userId);
    boolean setUserNickName(Long userId,String nickName);
    void payMoney(Long userId, BigDecimal money) throws InsufficientBalanceException;
    String uploadAvatar(MultipartFile avatar) throws IOException;
    void setUserAvatar(Long userId,String key);
    void rechargeMoney(Long userId,Integer money) throws RechargeFailException ;
    String getUserAvatar(Long userId);
    String getNickname(Long userId);
}
