package com.pigs.blog.contract.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@ApiModel
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ArticlesUpdateRequest {
    @ApiModelProperty("作者")
    private String author;//作者

    @ApiModelProperty("标题")
    private String title;//标题

    @ApiModelProperty("分类")
    private String groupName;//分类

    @ApiModelProperty("状态 状态 draft-草稿, published-已发布, deleted-被删除")
    private String status;//状态 状态 draft-草稿, published-已发布, deleted-被删除

    @ApiModelProperty("多个tag,逗号分割")
    private String tags;//多个tag,逗号分割

    @ApiModelProperty("文章内容")
    private String articlesText;//文章内容

    @ApiModelProperty(hidden = true)
    //@NotBlank(message = "imgUrl cannot be blank")
    private String imgUrl;
}