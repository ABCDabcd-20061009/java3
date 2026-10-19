package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.AuthException;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserContext;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.UserService;
import com.fenglin.springboottest.util.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 前端 vue-dream-login 对接接口
 * 前端约定：POST JSON；失败时读取响应体 message 展示。
 * 登录/注册成功返回 JWT，后续请求带 Authorization: Bearer &lt;token&gt;。
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /** 发送验证码：{ account: 用户名, type: "register" } */
    @PostMapping("/send-code")
    public Result<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        String account = body.get("account");
        String devCode = userService.sendRegisterCode(account);
        Map<String, String> data = new HashMap<>();
        // 未接入短信/邮件服务，开发阶段把验证码带回前端提示条展示
        data.put("devCode", devCode);
        return Result.ok("验证码已发送", data);
    }

    /** 注册：{ username, password, code } —— 注册成功直接算登录，返回 token */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        User user = userService.register(
                body.get("username"),
                body.get("password"),
                body.get("code"));
        return Result.ok("注册成功", withToken(user));
    }

    /** 登录：{ account: 用户名, password } */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("account"), body.get("password"));
        return Result.ok("登录成功", withToken(user));
    }

    /**
     * 当前登录用户信息（需登录）。
     * 前端 HomeCard 用它验证 token 是否还有效：失效返回 401 → 自动退出登录。
     */
    @PostMapping("/user-info")
    public Result<Map<String, Object>> userInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new AuthException("请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new AuthException("账号不存在或已注销");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("createTime", user.getCreateTime() == null ? "" : user.getCreateTime().toString());
        return Result.ok("ok", data);
    }

    private Map<String, Object> withToken(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("token", jwtUtil.generate(user.getId(), user.getUsername()));
        return data;
    }
}
