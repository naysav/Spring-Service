package naysav.spring.service.repository;

import naysav.spring.service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Интерфейс для взаимодействия с таблицей user БД
 */
@Component
public interface UserRepository
		extends JpaRepository<User, Long> {
	/**
	 * Метод получает записи с искомым значением поля userName
	 */
	User findByUsername(String userName);
}
