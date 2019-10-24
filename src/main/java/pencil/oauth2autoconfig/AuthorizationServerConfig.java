package pencil.oauth2autoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Objects;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    //accessToken 过期
    private int accessTokenValiditySecond = 60 * 60 * 2; //2小时
    private int refreshTokenValiditySecond = 60 * 60 * 24 * 7; // 7 天


    //添加商户信息
    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        configurer
                .inMemory()
                .withClient("client").
                secret(passwordEncoder().encode("123456")) //设置用户 和密码
                .authorizedGrantTypes("password","authorization_code","client_credentials","refresh_token")
                .scopes("all") //设置权限类型,用密码，客户端,刷新的token  权限为所有人
                .accessTokenValiditySeconds(accessTokenValiditySecond)
                .refreshTokenValiditySeconds(refreshTokenValiditySecond)
                .redirectUris("http://localhost:8081/login");
    }

    //定义授权和令牌端点和令牌服务
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints){
        //刷新令牌时需要的认证管理和用户信息来源
        endpoints.authenticationManager(authenticationManager()).allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST);
        endpoints.authenticationManager(authenticationManager());
        endpoints.userDetailsService(userDetailsService());
        endpoints.tokenEnhancer(tokenEnhancer()); //自定义token生成
        endpoints.authorizationCodeServices(austhorizationCodeServices()); //自定义code生成
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {

        //允许表单认证
        oauthServer.allowFormAuthenticationForClients();

        //允许 check_token 访问
        oauthServer.checkTokenAccess("permitAll()");
    }


    /**
     * @Description 自定义生成令牌token
     * @Date 2019/7/9 19:58
     * @Version  1.0
     */
    @Bean
    public TokenEnhancer tokenEnhancer(){
        return new UserTokenEnhancer();
    }

    @Bean
    public OssAusthorizationCodeServices austhorizationCodeServices(){
        return new OssAusthorizationCodeServices();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        AuthenticationManager authenticationManager = authentication -> daoAuhthenticationProvider().authenticate(authentication);
        return authenticationManager;
    }

    @Bean
    public AuthenticationProvider daoAuhthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    // 设置添加用户信息,正常应该从数据库中读取
    @Bean
    UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        userDetailsService.createUser(User.withUsername("user").password(passwordEncoder().encode("123456")).authorities("ROLE_USER").build());
        userDetailsService.createUser(User.withUsername("admin").password(passwordEncoder().encode("123456")).authorities("ROLE_USER").build());
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return Objects.equals(charSequence.toString(),s);
            }
        };
    }

}