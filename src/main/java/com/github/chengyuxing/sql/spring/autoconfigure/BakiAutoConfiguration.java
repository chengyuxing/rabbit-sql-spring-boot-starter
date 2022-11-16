package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.nio.charset.Charset;

@Configuration
@ConditionalOnClass(Baki.class)
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({BakiProperties.class, XQLFileManagerProperties.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class BakiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    private final DataSource dataSource;
    private final BakiProperties bakiProperties;

    public BakiAutoConfiguration(DataSource dataSource, BakiProperties bakiProperties) {
        this.dataSource = dataSource;
        this.bakiProperties = bakiProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public XQLFileManager xqlFileManager() {
        XQLFileManagerProperties properties = bakiProperties.getXqlFileManager();
        if (!ObjectUtils.isEmpty(properties) && !properties.getFiles().isEmpty()) {
            XQLFileManager xqlFileManager = new XQLFileManager();
            if (!ObjectUtils.isEmpty(properties.getFiles())) {
                xqlFileManager.setFiles(properties.getFiles());
            }
            if (!ObjectUtils.isEmpty(properties.getConstants())) {
                xqlFileManager.setConstants(properties.getConstants());
            }
            if (!ObjectUtils.isEmpty(properties.getPipes())) {
                xqlFileManager.setPipes(properties.getPipes());
            }
            if (StringUtils.hasText(properties.getCharset())) {
                xqlFileManager.setCharset(Charset.forName(properties.getCharset()));
            }
            if (StringUtils.hasLength(properties.getDelimiter())) {
                xqlFileManager.setDelimiter(properties.getDelimiter());
            }
            if (bakiProperties.getNamedParamPrefix() != ' ') {
                xqlFileManager.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
            }
            xqlFileManager.setCheckModified(properties.isCheckModified());
            xqlFileManager.setCheckPeriod(properties.getCheckPeriod());
            xqlFileManager.setHighlightSql(bakiProperties.isHighlightSql());
            xqlFileManager.init();
            return xqlFileManager;
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public Baki baki() {
        SpringManagedBaki baki = new SpringManagedBaki(dataSource);
        baki.setDebugFullSql(bakiProperties.isDebugFullSql());
        baki.setCheckParameterType(bakiProperties.isCheckParameterType());
        baki.setStrictDynamicSqlArg(bakiProperties.isStrictDynamicSqlArg());
        baki.setHighlightSql(bakiProperties.isHighlightSql());
        XQLFileManager xqlFileManager = xqlFileManager();
        if (!ObjectUtils.isEmpty(xqlFileManager)) {
            baki.setXqlFileManager(xqlFileManager());
            log.debug("Baki external sql file support configured. ");
        }
        if (bakiProperties.getNamedParamPrefix() != ' ') {
            baki.setNamedParamPrefix(baki.getNamedParamPrefix());
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getPageHelpers())) {
            baki.setPageHelpers(bakiProperties.getPageHelpers());
        }
        log.info("Baki initialized (Transaction managed by Spring)");
        return baki;
    }
}
