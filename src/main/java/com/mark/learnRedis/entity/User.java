package com.mark.learnRedis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private Integer age;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
