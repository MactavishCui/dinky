package org.dinky.data.model.udf;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("dinky_lineage")
@ApiModel(value = "Lineage", description = "Table Lineage")
public class Lineage extends Model<Lineage> {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "ID", dataType = "Integer", notes = "Unique identifier for the lineage")
    private Integer id;

    @ApiModelProperty(value = "Task Id", dataType = "Integer", notes = "Task identifier for the lineage")
    private Integer taskId;

    @ApiModelProperty(value = "Source Table Identifier", dataType = "String", notes = "Source task identifier for the lineage: catalog.database.table")
    private String sourceTableId;

    @ApiModelProperty(value = "Target Table Identifier", dataType = "String", notes = "Target task identifier for the lineage: catalog.database.table")
    private String targetTableId;

    @ApiModelProperty(value = "ColumnName", dataType = "String", notes = "ColumnName")
    private String columnName;
}
