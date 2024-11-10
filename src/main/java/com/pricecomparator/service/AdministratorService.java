package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.mapper.AdministratorMapper;
import com.pricecomparator.utils.*;

public interface AdministratorService {

    // 获取账户信息
    ApiResult getUserInfo(String userName);

    // 删除账户
    ApiResult deleteUser(String userName);
}
