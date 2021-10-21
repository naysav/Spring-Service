package naysav.spring.service.configs;

import naysav.spring.service.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Конфигурация Spring Security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * Поле для доступа к методу loadUserByUsername() для
	 * автоматизации авторизации пользователя.
	 */
	@Autowired
	UserService userService;

	/**
	 * Поле для доступа к шифрованию.
	 */
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	/**
	 * Метод устанавливает разграничение доступа к страницам веб-приложения,
	 * а также конфигурирует процесс авторизации и выхода из аккаунта.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf()
				.disable()
			.authorizeRequests()
				.antMatchers("/", "/registration", "/css/*").permitAll()
				.anyRequest().authenticated()
			.and()
				.formLogin()
					.loginPage("/login")
					.defaultSuccessUrl("/", true)
					.permitAll()
			.and()
				.rememberMe()
					.tokenValiditySeconds(60*60*24)
					.rememberMeParameter("remember-me")
			.and()
				.logout()
					.logoutSuccessUrl("/")
					.clearAuthentication(true)
					.invalidateHttpSession(true)
					.deleteCookies("JSESSIONID", "remember-me")
					.permitAll();
	}

	/**
	 * Метод конфигурирует хранилище пользователей для авторизации.
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(userService)
			.passwordEncoder(bCryptPasswordEncoder);

	}
}
