package com.hellogroup.teamenu.application.dto;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class XiachufangPageSearchResp implements Serializable {
    /**
     * 分页信息
     * "Page": [
     *         {
     *             "next": "/search/?keyword=%E9%B8%A1%E8%9B%8B&cat=1001&page=2"
     *         }
     *     ],
     */
    private List<Map<String, String>> Page;

    private List<XiachufangRecipeDto> Recipe;



}
