package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.LoginDTO;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.common.JwtUtils;
import com.wms.service.IMenuService;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-03-08
 */
@RestController
@RequestMapping("/user")
@RequireRole({0}) // 只有店长能管理用户
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
   private IMenuService menuService;
    @GetMapping("/findByNo")
    public Result findByNo(@RequestParam String no){
       List list = userService.lambdaQuery().eq(User::getNo, no).list();
       return list.size()>0?Result.suc(list):Result.fail();
    }
    @GetMapping("/list")
    public List<User> list() {
        return userService.list();
    }
        //新增
        @Log("新增用户")
        @PostMapping("/save")
        public Result save(@RequestBody User user){
            // 对密码进行MD5加密
            if(user.getPassword() != null && !user.getPassword().isEmpty()){
                user.setPassword(MD5(user.getPassword()));
            }
            return userService.save(user)?Result.suc():Result.fail();
        }
        //更新
        @Log("更新用户")
        @PostMapping("/update")
        public Result update(@RequestBody User user){
            // 如果有密码字段且不为空,则进行MD5加密
            if(user.getPassword() != null && !user.getPassword().isEmpty()){
                user.setPassword(MD5(user.getPassword()));
            }
            return userService.updateById(user)?Result.suc():Result.fail();
        }
        //删除
        @Log("删除用户")
        @GetMapping ("/del")
        public Result del(@RequestParam String id){
            return userService.removeById(id)?Result.suc():Result.fail();
        }
        //修改
        @Log("修改用户")
        @PostMapping("/mod")
        public boolean mod(@RequestBody User user){
            return userService.updateById(user);
        }
    //登录（基于Session）
    @Log("用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody User user, HttpSession session){
        // 对输入的密码进行MD5加密
        String encryptedPassword = MD5(user.getPassword());
        
        // 根据账号和加密后的密码查询用户
        List list = userService.lambdaQuery()
                .eq(User::getNo, user.getNo())
                .eq(User::getPassword, encryptedPassword)
                .list();
        
        if(list.size() > 0){
            User user1 = (User) list.get(0);
            
            // 校验账号是否被注销
            if("N".equals(user1.getIsValid())){
                return Result.fail("该账号已被注销，无法登录");
            }
            
            // 将用户信息存入session
            session.setAttribute("user", user1);
            session.setAttribute("username", user1.getName());
            session.setAttribute("userId", user1.getId());
            
            // 根据用户的roleId查询该角色有权访问的菜单列表
            List<Menu> allMenus = menuService.list();
            // 在内存中过滤包含roleId权限的菜单
            List<Menu> filteredMenus = allMenus.stream()
                    .filter(menu -> menu.getMenuRight() != null && 
                            menu.getMenuRight().contains(String.valueOf(user1.getRoleId())))
                    .collect(Collectors.toList());
            
            // 组装返回数据
            HashMap res = new HashMap();
            res.put("user", user1);
            res.put("menus", filteredMenus);
            return Result.suc(res);
        }
        return Result.fail("账号或密码错误");
    }
    
    /**
     * 基于JWT的登录接口（支持7天免登录）
     * @param loginDTO 登录请求DTO（包含no, password, rememberMe）
     * @return 登录结果（包含JWT Token和用户信息）
     */
    @Log("用户JWT登录")
    @PostMapping("/jwtLogin")
    public Result jwtLogin(@RequestBody LoginDTO loginDTO){
        // 参数校验
        if(loginDTO.getNo() == null || loginDTO.getPassword() == null){
            return Result.fail("账号和密码不能为空");
        }
        
        // 对输入的密码进行MD5加密
        String encryptedPassword = MD5(loginDTO.getPassword());
        
        // 根据账号和加密后的密码查询用户
        List<User> list = userService.lambdaQuery()
                .eq(User::getNo, loginDTO.getNo())
                .eq(User::getPassword, encryptedPassword)
                .list();
        
        if(list.size() > 0){
            User user = list.get(0);
            
            // 校验账号是否被注销
            if("N".equals(user.getIsValid())){
                return Result.fail("该账号已被注销，无法登录");
            }
            
            // 生成JWT Token（根据rememberMe参数设置过期时间）
            boolean rememberMe = loginDTO.getRememberMe() != null && loginDTO.getRememberMe();
            String token = JwtUtils.generateToken(user.getId(), user.getNo(), user.getRoleId(), rememberMe);
            
            // 根据用户的roleId查询该角色有权访问的菜单列表
            List<Menu> allMenus = menuService.list();
            List<Menu> filteredMenus = allMenus.stream()
                    .filter(menu -> menu.getMenuRight() != null && 
                            menu.getMenuRight().contains(String.valueOf(user.getRoleId())))
                    .collect(Collectors.toList());
            
            // 组装返回数据
            HashMap<String, Object> res = new HashMap<>();
            res.put("token", token);
            res.put("user", user);
            res.put("menus", filteredMenus);
            res.put("rememberMe", rememberMe);
            res.put("expireTime", rememberMe ? "7天" : "12小时");
            
            return Result.suc(res);
        }
        return Result.fail("账号或密码错误");
    }
    
    /**
     * 修改密码接口
     * @param params 包含userId, oldPassword, newPassword
     * @return 修改结果
     */
    @Log("修改密码")
    @PostMapping("/updatePwd")
    public Result updatePwd(@RequestBody HashMap params){
        try {
            Integer userId = (Integer) params.get("userId");
            String oldPassword = (String) params.get("oldPassword");
            String newPassword = (String) params.get("newPassword");
            
            // 参数校验
            if(userId == null || oldPassword == null || newPassword == null){
                return Result.fail("参数不能为空");
            }
            
            if(newPassword.trim().isEmpty()){
                return Result.fail("新密码不能为空");
            }
            
            // 调用服务层修改密码
            boolean result = userService.updatePassword(userId, oldPassword, newPassword);
            
            if(result){
                return Result.suc("密码修改成功");
            } else {
                return Result.fail("密码修改失败");
            }
        } catch (RuntimeException e) {
            // 捕获服务层抛出的异常（如：原密码错误、用户不存在）
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("密码修改异常：" + e.getMessage());
        }
    }
    
    /**
     * 注销账号接口
     * @param params 包含userId
     * @return 注销结果
     */
    @Log("注销账号")
    @PostMapping("/cancel")
    public Result cancelAccount(@RequestBody HashMap params){
        try {
            Integer userId = (Integer) params.get("userId");
            
            if(userId == null){
                return Result.fail("用户ID不能为空");
            }
            
            boolean result = userService.cancelAccount(userId);
            
            if(result){
                return Result.suc("账号注销成功");
            } else {
                return Result.fail("账号注销失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("账号注销异常：" + e.getMessage());
        }
    }
    
    /**
     * 更新用户状态接口（在Y和N之间切换）
     * @param params 包含id和isValid
     * @return 更新结果
     */
    @Log("更新用户状态")
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestBody HashMap params){
        try {
            Integer id = (Integer) params.get("id");
            String isValid = (String) params.get("isValid");
            
            // 参数校验
            if(id == null || isValid == null){
                return Result.fail("参数不能为空");
            }
            
            if(!"Y".equals(isValid) && !"N".equals(isValid)){
                return Result.fail("状态值只能是Y或N");
            }
            
            // 查询用户是否存在
            User user = userService.getById(id);
            if(user == null){
                return Result.fail("用户不存在");
            }
            
            // 更新状态
            user.setIsValid(isValid);
            boolean result = userService.updateById(user);
            
            if(result){
                String statusText = "Y".equals(isValid) ? "启用" : "停用";
                return Result.suc("用户已" + statusText);
            } else {
                return Result.fail("状态更新失败");
            }
        } catch (Exception e) {
            return Result.fail("状态更新异常：" + e.getMessage());
        }
    }
        //新增或修改
        @PostMapping("/saveorMod")
        public boolean saveorMod(@RequestBody User user){
            return userService.saveOrUpdate(user);
        }
        //删除
        @GetMapping("/delete")
        public boolean  delete(Integer id){
            return userService.removeById(id);
        }
        //查询 (模糊、匹配)
        @PostMapping("/listP")
        public Result listP(@RequestBody User user){
        LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(user.getName())){
            lambdaQueryWrapper.like(User::getName,user.getName());
        }
        return Result.suc(userService.list(lambdaQueryWrapper));
        }
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query ){
        /*System.out.println(query);
        System.out.println("num==="+query.getPagenum());
        System.out.println("num==="+query.getPagesize());*/
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        String isValid=(String) param.get("isValid");
        System.out.println("name==="+param.get("name"));
        Page<User> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name)){
            lambdaQueryWrapper.like(User::getName,name);
        }
        // 如果前端传了isValid参数，则按状态过滤；否则查询全部
        if(StringUtils.isNotBlank(isValid) && !"null".equals(isValid)){
            lambdaQueryWrapper.eq(User::getIsValid, isValid);
        }
        IPage result=userService.page(page,lambdaQueryWrapper);
        System.out.println("total="+result.getTotal());
        return Result.suc(result.getRecords(),result.getTotal());
    }
    @PostMapping("/listPageC")
    public Result listPageC(@RequestBody QueryPageParam query ){
        /*System.out.println(query);
        System.out.println("num==="+query.getPagenum());
        System.out.println("num==="+query.getPagesize());*/
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        String sex=(String) param.get("sex");
        System.out.println("name==="+param.get("name"));
        String roleId=(String) param.get("roleId");
        System.out.println("sex==="+param.get("sex"));
        String isValid=(String) param.get("isValid");
        Page<User> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(User::getName,name);
        }
        if(StringUtils.isNotBlank(sex) && !"null".equals(sex)){
            // 将字符串转换为 Integer 进行查询
            lambdaQueryWrapper.eq(User::getSex,Integer.parseInt(sex));
        }
        if(StringUtils.isNotBlank(roleId) && !"null".equals(roleId)){
            // 将字符串转换为 Integer 进行查询
            lambdaQueryWrapper.eq(User::getRoleId,Integer.parseInt(roleId));
        }
        // 如果前端传了isValid参数，则按状态过滤；否则查询全部
        if(StringUtils.isNotBlank(isValid) && !"null".equals(isValid)){
            lambdaQueryWrapper.eq(User::getIsValid, isValid);
        }
        IPage result=userService.page(page,lambdaQueryWrapper);
        System.out.println("total="+result.getTotal());
        return Result.suc(result.getRecords(),result.getTotal());
    }

    /**
     * MD5加密方法
     * @param input 待加密的字符串
     * @return 加密后的32位十六进制字符串
     */
    private String MD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
