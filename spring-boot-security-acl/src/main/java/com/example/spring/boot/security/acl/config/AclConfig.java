package com.example.spring.boot.security.acl.config;

import com.example.spring.boot.security.acl.domain.Post;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.*;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.intercept.AfterInvocationManager;
import org.springframework.security.access.intercept.AfterInvocationProviderManager;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.acls.AclEntryVoter;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.afterinvocation.AclEntryAfterInvocationCollectionFilteringProvider;
import org.springframework.security.acls.afterinvocation.AclEntryAfterInvocationProvider;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableAutoConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AclConfig extends GlobalMethodSecurityConfiguration {
    private final DataSource dataSource;

    public AclConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return defaultMethodSecurityExpressionHandler();
    }
/*
    @Override
    protected AfterInvocationManager afterInvocationManager() {
        AfterInvocationProviderManager manager = new AfterInvocationProviderManager();
        List<AfterInvocationProvider> afterInvocationProviders = new ArrayList<>();
        AclEntryAfterInvocationCollectionFilteringProvider collectionProvider =
                new AclEntryAfterInvocationCollectionFilteringProvider(aclService(),
                    Arrays.asList(BasePermission.ADMINISTRATION, BasePermission.READ));
        afterInvocationProviders.add(afterAclRead());
        afterInvocationProviders.add(collectionProvider);
        manager.setProviders(afterInvocationProviders);
        return manager;
    }

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        AffirmativeBased manager = (AffirmativeBased)super.accessDecisionManager();
        List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList<>(manager.getDecisionVoters());
        decisionVoters.add(aclEntryVoter());
        return new AffirmativeBased(decisionVoters);
    }

    @Bean
    public AclEntryVoter aclEntryVoter() {
        AclEntryVoter voter = new AclEntryVoter(aclService(), "VOTE_ACL_POST_DELETE",
                new Permission[]{ BasePermission.ADMINISTRATION, BasePermission.DELETE });
        voter.setProcessDomainObjectClass(Post.class);
        return voter;
    }
*/
    @Bean
    public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService());
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        expressionHandler.setPermissionCacheOptimizer(new AclPermissionCacheOptimizer(aclService()));
        return expressionHandler;
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public EhCacheFactoryBean aclEhCacheFactoryBean() {
        EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
        ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
        ehCacheFactoryBean.setCacheName("aclCache");
        return ehCacheFactoryBean;
    }

    @Bean
    public EhCacheManagerFactoryBean aclCacheManager() {
        return new EhCacheManagerFactoryBean();
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), permissionGrantingStrategy());
    }

    @Bean
    public JdbcMutableAclService aclService() {
        JdbcMutableAclService aclService = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
        //find mysql identity
        //aclService.setClassIdentityQuery("SELECT @@IDENTITY");
        //aclService.setSidIdentityQuery("SELECT @@IDENTITY");
        return aclService;
    }

    @Bean
    public AclEntryAfterInvocationProvider afterAclRead() {
        class CustomAclEntryAfterInvocationProvider extends AclEntryAfterInvocationProvider {
            public CustomAclEntryAfterInvocationProvider(AclService aclService, List<Permission> requirePermission) {
                super(aclService, requirePermission);
            }

            public CustomAclEntryAfterInvocationProvider(AclService aclService,
                                                         String processConfigAttribute,
                                                         List<Permission> requirePermission) {
                super(aclService, processConfigAttribute, requirePermission);
            }

            @Override
            public Object decide(Authentication authentication, Object object, Collection<ConfigAttribute> config,
                                 Object returnedObject) throws AccessDeniedException {
                if (returnedObject instanceof Optional) {
                    if (returnedObject == null) {
                        logger.debug("Return object is null, skipping");
                        return null;
                    }
                    if (!((Optional) returnedObject).isPresent()) {
                        return Optional.empty();
                    }
                    if (!getProcessDomainObjectClass().isAssignableFrom(((Optional) returnedObject).get().getClass())) {
                        return returnedObject;
                    }
                    for (ConfigAttribute attr : config) {
                        if (!this.supports(attr)) {
                            continue;
                        }
                        if (hasPermission(authentication, ((Optional) returnedObject).get())) {
                            return returnedObject;
                        }
                        throw new AccessDeniedException(messages.getMessage(
                                "AclEntryAfterInvocationProvider.noPermission", new Object[] {
                                        authentication.getName(), returnedObject },
                                "Authentication {0} has NO permissions to the domain object {1}"));
                    }
                    return returnedObject;
                }
                return super.decide(authentication, object, config, returnedObject);
            }
        }
        AclEntryAfterInvocationProvider provider = new CustomAclEntryAfterInvocationProvider(aclService(),
                Arrays.asList(BasePermission.ADMINISTRATION, BasePermission.READ));
        return provider;
    }

    @Bean
    public AclEntryAfterInvocationCollectionFilteringProvider afterAclCollectionRead() {
        return new AclEntryAfterInvocationCollectionFilteringProvider(aclService(),
                Arrays.asList(BasePermission.ADMINISTRATION, BasePermission.READ));
    }

    //为@Query语句中能使用SpEL表达式提供支持
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
