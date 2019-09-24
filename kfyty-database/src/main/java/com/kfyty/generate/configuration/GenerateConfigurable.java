package com.kfyty.generate.configuration;

import com.kfyty.support.configuration.Configuration;
import com.kfyty.generate.configuration.annotation.DataBase;
import com.kfyty.generate.configuration.annotation.DataBaseMapper;
import com.kfyty.generate.configuration.annotation.FilePath;
import com.kfyty.generate.configuration.annotation.GenerateTemplate;
import com.kfyty.generate.configuration.annotation.BasePackage;
import com.kfyty.generate.configuration.annotation.Table;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 功能描述: 生成配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:12
 * @since JDK 1.8
 */
@Data
@Slf4j
public class GenerateConfigurable extends Configuration {

    private DataSource dataSource;

    private Integer currentGenerateTemplateCursor;

    private List<AbstractGenerateTemplate> generateTemplateList;

    private Class<? extends AbstractDataBaseMapper> dataBaseMapper;

    private String dataBaseName;

    private Set<String> tables;

    private Pattern tablePattern;

    private String queryTableSql;

    private String basePackage;

    private String filePath;

    public GenerateConfigurable() {
        this.currentGenerateTemplateCursor = -1;
        this.generateTemplateList = new ArrayList<>();
    }

    public GenerateConfigurable(GenerateConfiguration configuration) {
        this();
        this.initGenerateConfigurable(configuration);
    }

    public boolean hasGenerateTemplate() {
        return currentGenerateTemplateCursor < generateTemplateList.size() - 1;
    }

    public AbstractGenerateTemplate getCurrentGenerateTemplate() {
        return generateTemplateList.get(currentGenerateTemplateCursor);
    }

    public AbstractGenerateTemplate getNextGenerateTemplate() {
        currentGenerateTemplateCursor++;
        return getCurrentGenerateTemplate();
    }

    public void refreshGenerateConfiguration(GenerateConfiguration configuration) {
        this.initGenerateConfigurable(configuration);
    }

    public void refreshGenerateTemplate(AbstractGenerateTemplate generateTemplate) {
        Optional.ofNullable(generateTemplate).orElseThrow(() -> new NullPointerException("generate template is null !"));
        this.generateTemplateList.add(currentGenerateTemplateCursor + 1, generateTemplate);
    }

    @Override
    public void autoConfigurationAfterCheck() {
        Optional.ofNullable(this.dataSource).orElseThrow(() -> new NullPointerException("data source is null !"));
        Optional.ofNullable(this.dataBaseMapper).orElseThrow(() -> new NullPointerException("data base mapper is null !"));
        this.generateTemplateList = Optional.ofNullable(generateTemplateList).filter(e -> !e.isEmpty()).map(e -> e.stream().distinct().collect(Collectors.toList())).orElseThrow(() -> new NullPointerException("generate template is null !"));
    }

    private void initGenerateConfigurable(GenerateConfiguration configuration) {
        this.initGenerateConfigurableFromAnnotation(configuration);
        this.initGenerateConfigurableFromReturnValue(configuration);
    }

    private void initGenerateConfigurableFromAnnotation(GenerateConfiguration configuration) {
        Class<? extends GenerateConfiguration> configurationClass = configuration.getClass();
        this.dataBaseMapper = dataBaseMapper != null ? dataBaseMapper : Optional.ofNullable(configurationClass.getAnnotation(DataBaseMapper.class)).map(DataBaseMapper::value).orElse(null);
        this.dataBaseName = Optional.ofNullable(configurationClass.getAnnotation(DataBase.class)).map(DataBase::value).orElse(null);
        this.tables = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.value()[0])).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.tablePattern = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.pattern())).map(e -> Pattern.compile(e.pattern())).orElse(Pattern.compile("([\\s\\S]*)"));
        this.queryTableSql = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.basePackage = Optional.ofNullable(configurationClass.getAnnotation(BasePackage.class)).map(BasePackage::value).orElse(null);
        this.filePath = Optional.ofNullable(configurationClass.getAnnotation(FilePath.class)).map(FilePath::value).orElse(null);
        List<AbstractGenerateTemplate> generateTemplateList = Optional.ofNullable(configurationClass.getAnnotation(GenerateTemplate.class)).map(e -> Arrays.stream(e.value()).distinct().map(clazz -> {
            try {
                return (AbstractGenerateTemplate) clazz.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList())).orElse(null);
        if(!CommonUtil.empty(generateTemplateList)) {
            this.generateTemplateList.addAll(generateTemplateList);
        }
    }

    private void initGenerateConfigurableFromReturnValue(GenerateConfiguration configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        if(this.dataBaseMapper == null) {
            if(configuration.dataBaseMapping() != null) {
                this.dataBaseMapper = configuration.dataBaseMapping();
            } else if(!isAutoConfiguration()) {
                throw new NullPointerException("data base mapper is null !");
            }
        }
        if(CommonUtil.empty(this.generateTemplateList)) {
            if(configuration.getGenerateTemplate() != null) {
                this.generateTemplateList.addAll(Arrays.stream(configuration.getGenerateTemplate()).distinct().collect(Collectors.toList()));
            } else if(!isAutoConfiguration()) {
                throw new NullPointerException("generate template is null !");
            }
        }
        if(CommonUtil.empty(this.dataBaseName)) {
            this.dataBaseName = Optional.ofNullable(configuration.dataBaseName()).orElseThrow(() -> new NullPointerException("data base name is null !"));
        }
        if(CommonUtil.empty(this.tables)) {
            this.tables = Optional.ofNullable(configuration.table()).map(e -> new HashSet<>(Arrays.asList(e))).orElse(null);
        }
        if(CommonUtil.empty(this.basePackage)) {
            this.basePackage = Optional.ofNullable(configuration.basePackage()).orElse("");
        }
        if(CommonUtil.empty(this.filePath)) {
            this.filePath = Optional.ofNullable(configuration.filePath()).orElse("");
        }
    }
}