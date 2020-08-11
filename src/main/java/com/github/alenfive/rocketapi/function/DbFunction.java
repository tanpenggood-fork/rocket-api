package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.extend.IPagerDialect;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作函数
 */
@SuppressWarnings("DuplicatedCode")
@Component
@Slf4j
public class DbFunction implements IFunction{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ScriptParseService parseService;

    @Autowired
    private IApiPager apiPager;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UtilsFunction utilsFunction;

    private Collection<IPagerDialect> pagerDialects;

    @PostConstruct
    public void init(){
        //加载分页方言
        pagerDialects = context.getBeansOfType(IPagerDialect.class).values();
    }

    @Override
    public String getVarName() {
        return "db";
    }

    public Long count(String script,String dataSource) throws Exception {
       List<Map<String,Object>> list = find(script,dataSource);
       if (CollectionUtils.isEmpty(list))return 0L;

       Object count = list.get(0).values().toArray()[0];
       if (count == null){
           count = list.size();
       }
       return Long.valueOf(count.toString());
    }

    public Map<String,Object> findOne(String script,String dataSource) throws Exception {
        List<Map<String,Object>> list = find(script,dataSource);
        if (list.size() == 0)return null;
        return list.get(0);
    }

    public List<Map<String,Object>> find(String script,String dataSource) throws Exception {
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        List<Map<String,Object>> result = dataSourceManager.find(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        if (apiInfoContent.getIsDebug()){
            apiInfoContent.putLog("generate script:  " + sbScript);
        }
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Object insert(String script,String dataSource) throws Exception {
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Object result = dataSourceManager.insert(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        if (apiInfoContent.getIsDebug()){
            apiInfoContent.putLog("generate script:  " + sbScript);
        }
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Object remove(String script,String dataSource) throws Exception {
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Object result =  dataSourceManager.remove(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        if (apiInfoContent.getIsDebug()){
            apiInfoContent.putLog("generate script:  " + sbScript);
        }
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Long update(String script,String dataSource) throws Exception {
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Long result =  dataSourceManager.update(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        if (apiInfoContent.getIsDebug()){
            apiInfoContent.putLog("generate script:  " + sbScript);
        }
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Object pager(String script,String dataSource) throws Exception {
        Page page = Page.builder()
                .pageNo(Integer.valueOf(utilsFunction.val(apiPager.getPageNoVarName()).toString()))
                .pageSize(Integer.valueOf(utilsFunction.val(apiPager.getPageSizeVarName()).toString()))
                .build();
        String totalSql = dataSourceManager.buildCountScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,apiPager,page,pagerDialects);
        Long total = this.count(totalSql);
        List<Map<String,Object>> data = null;
        if (total > 0){
            String pageSql = dataSourceManager.buildPageScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,apiPager,page,pagerDialects);
            data = this.find(pageSql);
        }else{
            data = Collections.emptyList();
        }
        return apiPager.buildPager(total,data,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams());
    }

    public Object pager(String script) throws Exception {
        return this.pager(script,null);
    }

    public Long count(String script) throws Exception {
        return this.count(script,null);
    }

    public Map<String,Object> findOne(String script) throws Exception {
        return this.findOne(script,null);
    }

    public List<Map<String,Object>> find(String script) throws Exception {
        return this.find(script,null);
    }

    public Object insert(String script) throws Exception {
        return this.insert(script,null);
    }

    public Object remove(String script) throws Exception {
        return this.remove(script,null);
    }

    public Long update(String script) throws Exception {
        return this.update(script,null);
    }


}
