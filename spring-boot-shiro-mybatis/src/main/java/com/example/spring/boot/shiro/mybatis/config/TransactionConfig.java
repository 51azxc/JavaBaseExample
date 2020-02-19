package com.example.spring.boot.shiro.mybatis.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Collections;

@Configuration
@Aspect
public class TransactionConfig {        //global transaction management
    private final static String AOP_POINTCUT_EXPRESSION = "execution (* com.example.spring.boot.shiro.mybatis.service.*.*(..))";

    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean
    public TransactionInterceptor txAdvice() {
        RuleBasedTransactionAttribute writeAttribute = new RuleBasedTransactionAttribute();
        writeAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        RuleBasedTransactionAttribute readAttribute = new RuleBasedTransactionAttribute();
        readAttribute.setReadOnly(true);
        readAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.addTransactionalMethod("add*", writeAttribute);
        source.addTransactionalMethod("save*", writeAttribute);
        source.addTransactionalMethod("update*", writeAttribute);
        source.addTransactionalMethod("delete*", writeAttribute);
        source.addTransactionalMethod("get*", readAttribute);
        source.addTransactionalMethod("find*", readAttribute);
        source.addTransactionalMethod("is*", readAttribute);
        source.addTransactionalMethod("count*", readAttribute);
        return new TransactionInterceptor(transactionManager, source);
    }

    @Bean
    public Advisor txAdviceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
