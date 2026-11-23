package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.LossReport;
import com.wms.entity.LossReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LossReportMapper extends BaseMapper<LossReport> {

    IPage<LossReportVO> selectLossPage(IPage<LossReportVO> page,
                                       @Param("lossNo") String lossNo,
                                       @Param("status") Integer status,
                                       @Param("lossType") Integer lossType,
                                       @Param("reporterId") Integer reporterId);
}
