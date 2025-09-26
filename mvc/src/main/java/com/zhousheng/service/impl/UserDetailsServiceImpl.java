package com.zhousheng.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhousheng.domain.UserDo;
import com.zhousheng.mapper.UserMapper;
import com.zhousheng.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // 用于权限列表
import java.util.Collections;

//这段代码纯复制粘贴，Spring Security用来验证用户信息的手段
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper; // 注入你的 UserMapper

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDo userDo = userMapper.selectOne(
                new QueryWrapper<UserDo>().eq("username", username)
        );
        if (userDo == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }


        // 返回我们自定义的 CustomUserDetails，并把 userId 传进去！
        return new CustomUserDetails(
                userDo.getId(),
                userDo.getUsername(),
                userDo.getPassword(),
//                new ArrayList<>() // 权限列表
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))

        );
    }
}
