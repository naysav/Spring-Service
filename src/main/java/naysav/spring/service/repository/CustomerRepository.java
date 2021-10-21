package naysav.spring.service.repository;

import naysav.spring.service.models.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Интерфейс для взаимодействия с таблицей customer БД
 */
@Component
public interface CustomerRepository
		extends JpaRepository<Customer, Long> {

	/**
	 * Метод получает данные клиента с искомыми значениями полей
	 * passportSeries и passportNumber
	 */
	Customer findByPassportSeriesAndPassportNumber(String passportSeries, String passportNumber);

}
