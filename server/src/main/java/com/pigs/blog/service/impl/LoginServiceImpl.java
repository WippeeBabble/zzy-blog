package com.pigs.blog.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.pigs.blog.common.Constants;
import com.pigs.blog.common.RoleEnum;
import com.pigs.blog.exception.ErrorCodeEnum;
import com.pigs.blog.common.ResultResponse;
import com.pigs.blog.contract.entity.LoginUser;
import com.pigs.blog.contract.request.RegistryRequest;
import com.pigs.blog.contract.response.LoginUserDataResponse;
import com.pigs.blog.mapper.UserInfoMapper;
import com.pigs.blog.mapper.UserMapper;
import com.pigs.blog.mapper.ext.UserMapperExt;
import com.pigs.blog.model.User;
import com.pigs.blog.model.UserExample;
import com.pigs.blog.model.UserInfo;
import com.pigs.blog.model.UserInfoExample;
import com.pigs.blog.service.LoginService;
import com.pigs.blog.utils.JwtUtil;
import com.pigs.blog.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserMapperExt userMapperExt;

    @Override
    public ResultResponse login(User user) {

        //??????UsernamePasswordAuthenticationToken????????????????????????
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getAccount(), user.getPassword());

        //AuthenticationManager???????????????authenticationToken ??????????????????
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        //????????????????????????????????????????????????
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("????????????");
        }

        //???????????????????????????user??????jwt  jwt??????ResponseResult ??????

        //?????????????????????????????????????????????????????????
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();

        Map<String, String> map = doLogin(loginUser);
        return ResultResponse.success(map);

    }

    @Override
    public ResultResponse logout() {
        //???SecurityContextHolder??????userId
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userid = loginUser.getUser().getId();

        //??????userId??????redis?????????????????????
        redisCache.deleteObject(Constants.REDIS_TOKEN_PREFIX + userid);
        return ResultResponse.success(null);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResultResponse registry(RegistryRequest request) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andAccountEqualTo(request.getAccount());
        List<User> users = userMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(users)) {
            logger.error("account:{" + request.getAccount() + "} is exist when registry");
            return ResultResponse.fail(10005, "account:{" + request.getAccount() + "} is exist when registry");
        }

        User user = new User();
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        BeanUtils.copyProperties(request, user);
        user.setRole(RoleEnum.VISITOR.getRole());
        userMapper.insertSelective(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(request.getNickName());
        userInfo.setAccount(request.getAccount());
        userInfo.setGithubUrl(request.getGithubUrl());
        userInfo.setImgUrl(request.getImgUrl());
        userInfoMapper.insertSelective(userInfo);

        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        Map<String, String> map = doLogin(loginUser);
        return ResultResponse.success(map);
    }

    @Override
    public ResultResponse getGitHubUserInfo(String key) {
        Object value = redisCache.getCacheObject(key);

        //1.??????redis?????????????????????github??????????????????
        if (value == null) {
            return ResultResponse.fail(ErrorCodeEnum.GITHUB_LOGIN_DATA_IS_EXPIRE.getCode(), ErrorCodeEnum.GITHUB_LOGIN_DATA_IS_EXPIRE.getMsg());
        }

        // ??????redis????????????????????????GitHubID
        String userInfo = value.toString();
        JSONObject jsonObject = JSONObject.parseObject(userInfo);
        Object id = jsonObject.get("id");
        long githubId = Long.parseLong(id.toString());

        // ??????GithubID????????????????????????
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andGithubIdEqualTo(githubId);
        List<User> users = userMapper.selectByExample(userExample);

        //2. ?????????????????????????????????GitHub?????????????????????????????????????????????
        if (CollectionUtils.isEmpty(users)) {
            return ResultResponse.success(userInfo);

        } else {

            //3. ???????????????????????????????????????????????????token
            User user = CollectionUtils.firstElement(users);

            LoginUser loginUser = new LoginUser();
            loginUser.setUser(user);
            Map<String, String> map = doLogin(loginUser);
            return ResultResponse.success(map);

        }
    }


    private Map<String, String> doLogin(LoginUser loginUser){
        User user = loginUser.getUser();
        String userId = user.getId().toString();

        //?????????????????????redis
        redisCache.setCacheObject(Constants.REDIS_TOKEN_PREFIX + userId, loginUser);

        //??????token???????????????
        String jwt = JwtUtil.createJWT(userId);
        Map<String, String> map = new HashMap<>();

        //???????????????userInfo??????
        UserInfoExample example = new UserInfoExample();
        UserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andAccountEqualTo(user.getAccount());
        List<UserInfo> userInfos = userInfoMapper.selectByExample(example);

        LoginUserDataResponse userData = new LoginUserDataResponse();
        BeanUtils.copyProperties(user,userData);

        userData.setRole(Arrays.asList(user.getRole().split(",")));

        if(!CollectionUtils.isEmpty(userInfos)){
            BeanUtils.copyProperties(CollectionUtils.firstElement(userInfos),userData);
            map.put("user_data", JSONObject.toJSONString(userData));
        }
        map.put("token", jwt);
        return map;
    }
}
