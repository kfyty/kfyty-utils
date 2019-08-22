package com.kfyty.generate.info;

import lombok.Data;

import java.util.List;

/**
 * 功能描述: 数据库信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 11:18
 * @since JDK 1.8
 */
@Data
public class AbstractDataBaseInfo {
    protected String dataBaseName;
    protected String tableName;
    protected String tableComment;
    protected List<? extends AbstractTableInfo> tableInfos;
}