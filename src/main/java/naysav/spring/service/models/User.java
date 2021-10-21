package naysav.spring.service.models;

import javax.persistence.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.AssertTrue;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Модель, предназначенная для хранения и валидации учетных данных пользователя,
 * и имплементирующая класс UserDetails.
 */
@Data
@Entity
public class User implements UserDetails{
	/**
	 * Индентификационный номер для таблицы user
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Поле имени пользователя
	 */
	@NotEmpty(message = "• обязательное поле")
	private String firstName;

	/**
	 * Поле фамилии пользователя
	 */
	private String lastName;

	/**
	 * Поле логина пользователя
	 */
	@NotEmpty(message = "• обязательное поле")
	@Size(min = 4, max = 12, message = "• необходимо 4-12 символов")
	@Pattern(regexp="[a-zA-Z[0-9]]{4,12}", message = "• необходимы цифры или латинские буквы")
	private String username;

	/**
	 * Поле пароля пользователя
	 */
	@NotEmpty(message = "• обязательное поле")
	@Size(min = 8, message = "• необходимо от 8 символов")
	private String password;

	/**
	 * Поле подтверждения пароля пользователя
	 */
	@NotEmpty(message = "• обязательное поле")
	@Size(min = 8, message = "• необходимо от 8 символов")
	@Transient
	private String passwordVerify;

	/**
	 * Поле половой принадлежности пользователя
	 */
	private String gender;

	/**
	 * Поле возраста пользователя
	 */
	@Pattern(regexp="\\d{0,3}", message = "• необходимо неотрицательное число")
	private String age;

	/**
	 * Поле роли пользователя при работе с сервисом
	 */
	private String role;

	public User() {
	}

	@AssertTrue
	public boolean isValid() {
		return this.password.equals(this.passwordVerify);
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
