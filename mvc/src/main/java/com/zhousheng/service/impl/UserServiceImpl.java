package com.zhousheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhousheng.common.exception.InsufficientBalanceException;
import com.zhousheng.common.exception.RechargeFailException;
import com.zhousheng.common.utils.DateUtils;
import com.zhousheng.domain.UserDo;
import com.zhousheng.dto.UserRegisterDto;
import com.zhousheng.mapper.UserMapper;
import com.zhousheng.service.CosService;
import com.zhousheng.service.UserService;
import com.zhousheng.vo.UserDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl
        extends ServiceImpl<UserMapper,UserDo>
        implements UserService {
    private final UserMapper userMapper;
    private final CosService cosService;
    private final PasswordEncoder passwordEncoder;
    UserServiceImpl(UserMapper userMapper,CosService cosService,PasswordEncoder passwordEncoder)
    {
        this.userMapper = userMapper;
        this.cosService = cosService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public int registerUser(UserRegisterDto user) {
        UserDo userDo = new UserDo();
        userDo.setUsername(user.getUsername());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        userDo.setPassword(encodedPassword);
        return userMapper.registerUser(userDo);
    }

    @Override
    public UserDo getUserById(Long userId) {
        return userMapper.getUserDoByUserId(userId);
    }

    @Override
    public UserDetailVo getUserInfoById(Long userId) {
        UserDo userDo = userMapper.getUserDoByUserId(userId);
        UserDetailVo userDetailVo = new UserDetailVo();
        userDetailVo.setAvatar(cosService.getFileUrl(userDo.getAvatar()));
        userDetailVo.setUsername(userDo.getUsername());
        userDetailVo.setNickname(userDo.getNickname());
        userDetailVo.setCreateTime(userDo.getCreateTime());
        userDetailVo.setMoney(userDo.getMoney());
        if(userDetailVo == null){
            throw new RuntimeException("没有找到用户信息");
        }
        return userDetailVo;
    }

    /**
     * 这里上传一个头像，返回临时存储路径key
     * @param avatar
     * @return
     * @throws IOException
     */
    @Override
    public String uploadAvatar(MultipartFile avatar) throws IOException {
        String currentDate = DateUtils.getCurrentDateString();
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFileName = avatar.getOriginalFilename();
        int dotIndex = originalFileName.lastIndexOf(".");
        String fileExtension = null;
        if(dotIndex >=0 && dotIndex < originalFileName.length()-1){
            fileExtension = originalFileName.substring(dotIndex);
        }else {
            throw new RuntimeException("用户上传头像名称有误");
        }
        String directory = "temp/user/avatar/" + currentDate + "/" + fileName + fileExtension;
        cosService.uploadFile(avatar,directory);
        return directory;
    }

    /**
     * 调用uploadAvatar之后，我们拿到临时存储路径key调用此方法在云端和数据库更新头像存储信息
     * @param userId
     * @param key
     */
    @Override
    public void setUserAvatar(Long userId,String key) {
        UserDo userDo = new UserDo();
        userDo.setId(userId);
        String currentDate = DateUtils.getCurrentDateString();
        String fileName = UUID.randomUUID().toString().replace("-","");
        String fileExtension =  key.substring(key.lastIndexOf("."));
        String destination = "user/avatar/" + currentDate +"/"+ fileName + fileExtension;
        cosService.moveFile(key, destination);
        userDo.setAvatar(destination);
        this.updateById(userDo);
    }

    @Override
    public boolean setUserNickName(Long userId, String nickName) {
//        UserDo userDo = new UserDo();
//        userDo.setId(userId);
//        userDo.setNickname(nickName);
//        boolean updateSuccess = this.updateById(userDo);
//        return updateSuccess;
        return this.update(
                new LambdaUpdateWrapper<UserDo>()
                        .eq(UserDo::getId, userId)
                        .set(UserDo::getNickname, nickName)
        );
    }

    @Override
    public void payMoney(Long userId, BigDecimal money) throws InsufficientBalanceException {
        if(userId == null || money == null){
            log.error("错误，无用户信息或付款信息");
            return ;
        }
        int affectedRows = userMapper.payMoney(userId, money);
        if(affectedRows==0){
            log.error("付款失败，余额不足");
            throw new InsufficientBalanceException("余额不足，无法消费");
        }
        log.info("支付成功");
    }

    @Override
    public void rechargeMoney(Long userId,Integer amount) throws RechargeFailException {
//        LambdaUpdateWrapper<UserDo> updateWrapper = new LambdaUpdateWrapper<>();
//        updateWrapper
//                .apply("money = money + {0}",money)
//                .eq(UserDo::getId,userId)
//                .ge(UserDo::getMoney,0);
//        int affectedRows = userMapper.update(updateWrapper);
//        if(affectedRows == 0){
//            throw new RechargeFailException("充值失败，账户有问题");
//        }
        LambdaUpdateWrapper<UserDo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .setSql("money = money + " + amount)   // 直接累加，支持正负
                .eq(UserDo::getId, userId)
                .ge(UserDo::getMoney, -amount);        // 确保扣款后余额不为负

        int affectedRows = userMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            throw new RechargeFailException(amount >= 0 ? "充值失败，账户有问题" : "扣款失败，余额不足");
        }
    }

    @Override
    public String getUserAvatar(Long userId) {
        UserDo userDo = userMapper.selectById(userId);
        String avatar = userDo.getAvatar();
        return cosService.getFileUrl(avatar);
    }

    @Override
    public String getNickname(Long userId) {
        UserDo userDo = userMapper.selectById(userId);
        return userDo.getNickname();
    }
}
