package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.SalesReturnDetail;
import com.wms.mapper.SalesReturnDetailMapper;
import com.wms.service.ISalesReturnDetailService;
import org.springframework.stereotype.Service;

/**
 * 退货明细服务实现类
 */
@Service
public class SalesReturnDetailServiceImpl extends ServiceImpl<SalesReturnDetailMapper, SalesReturnDetail> implements ISalesReturnDetailService {

}
