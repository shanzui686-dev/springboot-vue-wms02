package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.service.IMenuService;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
        @PostMapping("/save")
        public Result save(@RequestBody User user){
            return userService.save(user)?Result.suc():Result.fail();
        }
        //更新
        @PostMapping("/update")
        public Result update(@RequestBody User user){
            return userService.updateById(user)?Result.suc():Result.fail();
        }
        //删除
        @GetMapping ("/del")
        public Result del(@RequestParam String id){
            return userService.removeById(id)?Result.suc():Result.fail();
        }
        //修改
        @PostMapping("/mod")
        public boolean mod(@RequestBody User user){
            return userService.updateById(user);
        }
    //登录
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        List list = userService.lambdaQuery().eq(User::getNo, user.getNo()).eq(User::getPassword, user.getPassword()).list();
        if(list.size()>0){
            User user1 = (User) list.get(0);
            List<Menu> allMenus = menuService.list();
            // 在内存中过滤包含 roleId 权限的菜单
            List<Menu> filteredMenus = allMenus.stream()
                    .filter(menu -> menu.getMenuRight() != null && menu.getMenuRight().contains(String.valueOf(user1.getRoleId())))
                    .collect(Collectors.toList());
            HashMap res=new HashMap();
            res.put("user", user1);
            res.put("menus", filteredMenus);
           return Result.suc(res);
        }
        return Result.fail();
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
    public List<User> listPage(@RequestBody QueryPageParam query ){
        /*System.out.println(query);
        System.out.println("num==="+query.getPagenum());
        System.out.println("num==="+query.getPagesize());*/
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        System.out.println("name==="+param.get("name"));
        Page<User> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(User::getName,name);
        IPage result=userService.page(page,lambdaQueryWrapper);
        System.out.println("total="+result.getTotal());
        return result.getRecords();
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
        IPage result=userService.page(page,lambdaQueryWrapper);
        System.out.println("total="+result.getTotal());
        return Result.suc(result.getRecords(),result.getTotal());
    }

}
