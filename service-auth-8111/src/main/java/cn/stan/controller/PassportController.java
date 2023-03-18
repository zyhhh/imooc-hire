package cn.stan.controller;

import cn.stan.common.base.BaseInfoProperties;
import cn.stan.common.result.GraceResult;
import cn.stan.common.result.ResponseStatusEnum;
import cn.stan.common.utils.IPUtil;
import cn.stan.common.utils.SMSUtil;
import cn.stan.pojo.Users;
import cn.stan.pojo.bo.RegistLoginBO;
import cn.stan.pojo.vo.UsersVO;
import cn.stan.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("passport")
public class PassportController extends BaseInfoProperties {

    @Autowired
    private SMSUtil smsUtil;

    @Autowired
    private UsersService usersService;

    @PostMapping("getSMSCode")
    public GraceResult getSMSCode(String mobile, HttpServletRequest request) {

        if (StringUtils.isBlank(mobile)) {
            return GraceResult.errorMsg("手机号为空");
        }
        // 获取用户ip地址，存入Redis，限制60s请求一次
        String ip = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE + ":" + ip, mobile);

        // 6位随机数
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        log.info("验证码: {}", code);

        // 过期时间，分钟
        int expireTime = 30;

        // 发送短信
        // smsUtil.sendSMS(phone, code, String.valueOf(expireTime));

        // 将验证码存于Redis中
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, expireTime * 60);

        return GraceResult.ok();
    }

    @PostMapping("login")
    public GraceResult login(@Valid @RequestBody RegistLoginBO registLoginBO) {

        String mobile = registLoginBO.getMobile();
        String smsCode = registLoginBO.getSmsCode();

        // 1.校验验证码
        String code = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(code) || !smsCode.equalsIgnoreCase(code)) {
            return GraceResult.error(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2.查询用户是否存在，存在则登录，不存在则注册
        Users users = usersService.queryUserByMobile(mobile);
        if (users == null) {
            users = usersService.createUsers(mobile);
        }

        // 3.保存token到redis中，4小时过期
        String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
        redis.set(REDIS_USER_TOKEN + ":" + users.getId(), uToken, 4 * 60 * 60);

        // 4.删除redis中验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 5.组装数据返回给前端
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(users, usersVO);
        usersVO.setUserToken(uToken);

        return GraceResult.ok(usersVO);
    }

    @PostMapping("login")
    public GraceResult login(@RequestParam("userId") String userId) {

        // 删除redis中的token
        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceResult.ok();
    }

}