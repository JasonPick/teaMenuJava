package com.hellogroup.teamenu.application.dto;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Data
public class XiachufangRecipeDto implements Serializable {
    /**
     * 链接
     */
    public String url;
    /**
     * 名称
     */
    public String name;
    /**
     * 封面
     */
    public String cover;
}
