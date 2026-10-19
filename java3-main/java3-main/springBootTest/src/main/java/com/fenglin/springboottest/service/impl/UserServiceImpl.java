package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.UserService;
import com.fenglin.springboottest.util.CodeStore;
import com.fenglin.springboottest.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 密码：1-18 位，不含空白字符 */
    private static final Pattern PWD_RE = Pattern.compile("^\\S{1,18}$");

    private final UserMapper userMapper;
    private final CodeStore codeStore;

    public UserServiceImpl(UserMapper userMapper, CodeStore codeStore) {
        this.userMapper = userMapper;
        this.codeStore = codeStore;
    }

    @Override
    public String sendRegisterCode(String username) {
        if (username == null || username.trim().length() < 2 || username.trim().length() > 20) {
            throw new BizException("请先填写正确的用户名（2-20 个字符）");
        }
        username = username.trim();
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count != null && count > 0) {
            throw new BizException("该用户名已被使用，请直接登录");
        }
        String code = codeStore.generate(username);
        // 未接入短信/邮件服务：验证码打印到控制台，便于开发联调
        log.info("===== 注册验证码：用户 {}，验证码 {}（5 分钟内有效）=====", username, code);
        return code;
    }

    @Override
    public User register(String username, String password, String code) {
        // 1. 参数校验
        if (username == null || username.trim().length() < 2 || username.trim().length() > 20) {
            throw new BizException("用户名需为 2-20 个字符");
        }
        username = username.trim();
        if (password == null || !PWD_RE.matcher(password).matches()) {
            throw new BizException("密码需为 1-18 位，且不能包含空格");
        }
        if (code == null || !code.matches("^\\d{6}$")) {
            throw new BizException("验证码为 6 位数字");
        }

        // 2. 验证码校验（一次性，验证后失效）
        if (!codeStore.verify(username, code)) {
            throw new BizException("验证码错误或已过期，请重新获取");
        }

        // 3. 唯一性校验
        Long nameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (nameCount != null && nameCount > 0) {
            throw new BizException("该用户名已被使用");
        }

        // 4. 入库
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(password));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("新用户注册成功：{}", username);
        return user;
    }

    @Override
    public User login(String account, String password) {
        if (account == null || account.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new BizException("请填写账号和密码");
        }
        account = account.trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .last("limit 1"));
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        log.info("用户登录成功：{}", user.getUsername());
        return user;
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }
}
