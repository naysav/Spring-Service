package naysav.spring.service.services;

import naysav.spring.service.models.User;
import naysav.spring.service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервисный класс для чтения и записи данных таблицы user,
 * полученных от класса Controllers.
 */
@Service
public class UserService implements UserDetailsService{

	/**
	 * Поле для доступа к таблице user.
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * Поле для доступа к шифрованию.
	 */
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	/**
	 * Метод необходим для реализации интерфейса UserDetailsService.
	 * @param username - логин пользователя
	 * @return - пользователь с логином username
	 * @throws UsernameNotFoundException, если пользователь не найден
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return user;
	}

	/**
	 * Метод сохраняет данные пользователя в БД user
	 * @param user - модель, содержащая данные пользователя
	 * @return true, если успешно сохранены
	 *         false, если сохранение не удалось
	 */
	public boolean saveUser(User user) {
		User userFromDB = userRepository.findByUsername(user.getUsername());

		if (userFromDB != null) {
			return false;
		}

		try {
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			user.setPasswordVerify(user.getPassword());
			user.setRole("USER");
			userRepository.save(user);
			return true;
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			return false;
		}
	}
}

