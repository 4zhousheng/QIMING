package com.zhousheng.controller.api;

import com.zhousheng.common.exception.OrderDetailsNotFoundException;
import com.zhousheng.common.exception.RechargeFailException;
import com.zhousheng.common.exception.UserIdGetException;
import com.zhousheng.common.result.Result;
import com.zhousheng.dto.UserLoginDto;
import com.zhousheng.dto.UserRegisterDto;
import com.zhousheng.security.CustomUserDetails;
import com.zhousheng.security.JwtUtil;
import com.zhousheng.service.OrderService;
import com.zhousheng.service.UserService;
import com.zhousheng.vo.UserDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserApi {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OrderService orderService;
    private final UserDetailsService userDetailsService;
    UserApi(UserService userService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtUtil jwtUtil,
            OrderService orderService){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.orderService = orderService;
    }
    @GetMapping("/avatar/get")
    Result getUserAvatar(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        return Result.success(userService.getUserAvatar(userId));
    }
    @PostMapping("/register")
    Result register(@RequestBody UserRegisterDto userRegisterDto){
        int affectedRows = userService.registerUser(userRegisterDto);
        if(affectedRows == 0){
            return Result.fail(500,"注册失败");
        }
        return Result.success("注册成功");
    }
    @PostMapping("/login")
    Result login(@RequestBody UserLoginDto userLoginDto) throws Exception {
//        验证用户信息
        try{
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword())
        );
        }catch (BadCredentialsException e){
            return Result.fail(500,"登录失败");
        }
        // a. 重新从数据库加载用户信息 (确保获取的是最新的用户信息)
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(userLoginDto.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        return Result.success(jwt);
    }

    /**
     * 设置头像
     * @param avatar
     * @param authentication
     * @return
     */
    @PostMapping("/avatar/set")
    Result setAvatar(MultipartFile avatar,Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String key = null;
        try {
            key = userService.uploadAvatar(avatar);
        } catch (IOException e) {
            log.error("头像上传失败");
            return Result.fail(500, "头像上传失败");
        }
        if (key != null && !key.isEmpty()) {
            userService.setUserAvatar(userId, key);
        }
        return Result.success();
    }

    /**
     * 简单充值入口
     * @param authentication
     * @param money
     * @return
     */
    @PostMapping("/recharge")
    Result recharge(
            Authentication authentication,
            Integer money){
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        try {
            userService.rechargeMoney(userId,money);
        } catch (RechargeFailException e) {
            log.error("充值失败");
            return Result.fail(336,"充值失败");
        }
        return Result.success("充值成功");
    }

    /**
     * 获取 用户信息，用于用户主页展示
     * @param authentication
     * @return
     */
    @GetMapping("/info")
    Result getUserInfo(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        return Result.success(userService.getUserInfoById(userId));
    }

    /**
     * 设置用户昵称
     * @param authentication
     * @param nickName
     * @return
     */
    @PostMapping("/nickname/set")
    Result setNickName(Authentication authentication, String nickName){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        boolean success = userService.setUserNickName(userId, nickName);
        if(success){
            return Result.success();
        }else{
            return Result.fail(500,"设置头像失败");
        }
    }
    @GetMapping("/nickname/get")
    Result getNickName(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String nickname = userService.getNickname(userId);
        return Result.success(nickname);
    }
    @GetMapping("/orders")
    Result getUserOrders(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        try {
            return Result.success(orderService.getOrderByUserId(userId));
        } catch (UserIdGetException e) {
            log.error(e.getMessage());
            return Result.fail(500,"找不到用户");
        }
    }

}
