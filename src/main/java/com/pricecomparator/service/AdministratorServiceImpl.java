package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.mapper.AdministratorMapper;
import com.pricecomparator.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class AdministratorServiceImpl implements AdministratorService {
    @Autowired
    private AdministratorMapper administratorMapper;

    @Override
    public ApiResult getUserInfo(String userName){
        return null;
    }

    @Override
    public ApiResult deleteUser(String userName) {
        return null;
    }

}
