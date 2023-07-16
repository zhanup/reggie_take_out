package com.itheima.reggie.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user User
     * @param session HttpSession
     * @return Result
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (Strings.isEmpty(phone)) {
            return Result.error("短信发送失败");
        }
        try {
            // 生成4位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            // 调用阿里云提供的短信服务API完成短信发送
            SMSUtils.sendMessage(phone, code);
            // 需要将生成的验证码保存到Session
            session.setAttribute(phone, code);
            return Result.success("手机验证码短信发送成功");
        } catch (Exception exp) {
            log.error("短信发送失败", exp);
            return Result.error("短信发送失败");
        }
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody JSONObject jsonObject, HttpSession session) {
        // 获取手机号
        String phone = jsonObject.getString("phone");

        // 获取验证码
        String code = jsonObject.getString("code");

        // 从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            // 如果能够比对成功，说明登录成功

            // 判断当前手机对应的用户是否为新用户，如果是新用户就自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return Result.success(user);
        }
        return Result.error("登录失败");
    }

}
