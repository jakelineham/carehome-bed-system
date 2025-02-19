package uk.ac.cardiff.AftercareConnect.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static final String[] ENDPOINTS_WHITELIST = {
            "/", "/403", "/css/**", "/images/**", "/contactUs", "/contactUsConfirmation", "/home", "/user/register", "/userThanks", "/registerUser", "/error", "/carehome/add", "/addCarehome", "/carehomeThanks"
    };

    public static final String[] CAREHOMEMANAGER_ENDPOINTS_WHITELIST = {
            "/carehome/update/**", "/carehome/edit/**", "/registerCarehome", "/careHomeManager/home", "/editAftercare", "/updateBedNum"
    };
    
    public static final String[] SOCIALSERVICE_ENDPOINTS_WHITELIST = {
            "/socialService/home"
    };
    public static final String[] SOCIETYMANAGER_ENDPOINTS_WHITELIST = {
            "/societyManager/incomingRegistrations", "/societyManager/home", "/approveUser", "/declineUser"
    };
    public static final String[] STAKEHOLDER_ENDPOINTS_WHITELIST = {
            "/carehome/overview", "/getChartData", "/stakeholder/home", "/generateReport"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(request -> request
                        .requestMatchers(ENDPOINTS_WHITELIST).permitAll()
                        .requestMatchers(CAREHOMEMANAGER_ENDPOINTS_WHITELIST).hasAnyRole("CAREHOMEMANAGER", "ADMIN")
                        .requestMatchers(SOCIALSERVICE_ENDPOINTS_WHITELIST).hasAnyRole("SOCIALSERVICE", "ADMIN")
                        .requestMatchers(SOCIETYMANAGER_ENDPOINTS_WHITELIST).hasAnyRole("SOCIETYMANAGER", "ADMIN")
                        .requestMatchers(STAKEHOLDER_ENDPOINTS_WHITELIST).hasAnyRole("STAKEHOLDER", "ADMIN")
                        .requestMatchers("/user/edit/**").authenticated()
                        .requestMatchers("/editUser").authenticated()
                        .requestMatchers("/carehome/**").authenticated()
                        .requestMatchers("/filter").authenticated()
                        .requestMatchers("/carehome/search").authenticated()

                        .anyRequest().hasRole("ADMIN")
                )
                .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login") // Redirect invalid sessions to login
                        .maximumSessions(1) // Ensure one session per user
                );
        return security.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        JdbcDaoImpl jdbcUserDetails = new JdbcDaoImpl();
        jdbcUserDetails.setDataSource(dataSource);
        jdbcUserDetails.setUsersByUsernameQuery("select email as username, password, accepted from userAccounts where email = ?");
        jdbcUserDetails.setAuthoritiesByUsernameQuery("select username, authority from userAuthorities where username = ?   ");
        return jdbcUserDetails;
    }
}
