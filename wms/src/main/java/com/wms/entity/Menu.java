package com.wms.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * <p>
 * 
 * </p>
 *
 * @author wms
 * @since 2026-03-24
 */
@Schema(name = "Menu", description = "")
@TableName("menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("menuCode")
    @Schema(description = "菜单编码")
    private String menuCode;

    @TableField("menuName")
    @Schema(description = "菜单名字")
    private String menuName;
    
    @TableField("menuLevel")
    @Schema(description = "菜单级别")
    private String menuLevel;
    
    @TableField("menuParentCode")
    @Schema(description = "菜单的父 code")
    private String menuParentCode;
    
    @TableField("menuClick")
    @Schema(description = "点击触发的函数")
    private String menuClick;
    
    @TableField("menuRight")
    @Schema(description = "权限 0 超级管理员，1 表示管理员，2 表示普通用户，可以用逗号组合使用")
    private String menuRight;
    
    @TableField("menuComponent")
    private String menuComponent;
    
    @TableField("menuIcon")
    private String menuIcon;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuLevel() {
        return menuLevel;
    }

    public void setMenuLevel(String menuLevel) {
        this.menuLevel = menuLevel;
    }

    public String getMenuParentCode() {
        return menuParentCode;
    }

    public void setMenuParentCode(String menuParentCode) {
        this.menuParentCode = menuParentCode;
    }

    public String getMenuClick() {
        return menuClick;
    }

    public void setMenuClick(String menuClick) {
        this.menuClick = menuClick;
    }

    public String getMenuRight() {
        return menuRight;
    }

    public void setMenuRight(String menuRight) {
        this.menuRight = menuRight;
    }

    public String getMenuComponent() {
        return menuComponent;
    }

    public void setMenuComponent(String menuComponent) {
        this.menuComponent = menuComponent;
    }

    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }

    @Override
    public String toString() {
        return "Menu{" +
            "id = " + id +
            ", menuCode = " + menuCode +
            ", menuName = " + menuName +
            ", menuLevel = " + menuLevel +
            ", menuParentCode = " + menuParentCode +
            ", menuClick = " + menuClick +
            ", menuRight = " + menuRight +
            ", menuComponent = " + menuComponent +
            ", menuIcon = " + menuIcon +
        "}";
    }
}
