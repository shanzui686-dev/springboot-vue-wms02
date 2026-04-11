package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Menu;
import com.wms.entity.User;
import com.wms.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-03-24
 */
@RestController
@RequestMapping("/menu")
public class MenuController {
    @Autowired
    private IMenuService menuService;
    @GetMapping("/list")
    public Result list(@RequestParam String roleId){
        List<Menu> allMenus = menuService.list();
        // 在内存中过滤包含 roleId 权限的菜单
        List<Menu> filteredMenus = allMenus.stream()
            .filter(menu -> menu.getMenuRight() != null && menu.getMenuRight().contains(roleId))
            .collect(Collectors.toList());
        return Result.suc(filteredMenus);
    }

}
