package naysav.spring.service.services;

import naysav.spring.service.models.User;
import naysav.spring.service.repository.UserRepository;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.Test;


/** Тестовый класс для методов класса UserService */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest extends TestCase {

	/** Имитация интерфейса userRepository */
	@Mock
	private UserRepository userRepository;

	/** Экземпляр энкодера BCryptPasswordEncoder*/
	@Spy
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/**
	 * Экземпляр класса UserService.
	 * В него встраиваются зависимости BCryptPasswordEncoder и UserRepository
	 * */
	@InjectMocks
	UserService userService;

	/** Экземпляр класса User */
	User user = new User();

	/** Метод инициализирует Mock объекты и объект User. */
	@Before
	public void setup() throws Exception {
		assertNotNull(userRepository);

		user.setId(1L);
		user.setFirstName("testName");
		user.setUsername("test");
		user.setPassword(encoder.encode("testPassword"));
		user.setPasswordVerify(encoder.encode("testPassword"));

		Mockito.when(userRepository.findByUsername("test")).thenReturn(user);
	}

	/**
	 * Методу передается username.
	 * Если объект с полем username не найден,
	 * выбрасывается исключение UsernameNotFoundException.
	 * Если найден, возвращает найденный объект User.
	 */
	@Test
	public void testLoadUserByUsername() {
		User receivedUser = (User) userService.loadUserByUsername("test");
		assertEquals(receivedUser, user);
	}

	/**
	 * Методу передается объект user.
	 * Если пользователь уже есть в БД, возвращает false.
	 * Если user сохранен, возвращает true.
	 */
	@Test
	public void testSaveUser() {
		boolean res = userService.saveUser(user);
		assertFalse(res);
	}
}