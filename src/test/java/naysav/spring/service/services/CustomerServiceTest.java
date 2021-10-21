package naysav.spring.service.services;

import junit.framework.TestCase;
import naysav.spring.service.models.Customer;
import naysav.spring.service.repository.CustomerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/** Тестовый класс для методов класса ClientService */
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest extends TestCase {

	/** Имитация реализации интерфейса customerRepository */
	@Mock
	private CustomerRepository customerRepository;

	/**
	 * Экземпляр CustomerService.
	 * В него встраивается зависимость CustomerRepository
	 * */
	@InjectMocks
	CustomerService customerService;

	/** Экземпляр класса Customer*/
	Customer customer = new Customer();

	/** Экземпляр класса MultipartFile == null*/
	MultipartFile file;

	/** Метод инициализирует Mock объекты и объект Customer. */
	@Before
	public void setup() throws Exception {
		assertNotNull(customerRepository);

		customer.setId(1L);
		customer.setFirstName("testFirstName");
		customer.setLastName("testLastName");
		customer.setGender("Male");
		customer.setAge("21");
		customer.setPassportSeries("1234");
		customer.setPassportNumber("567890");
		customer.setPhoneNumber("1234567890");

		Mockito.when(customerRepository.
				findByPassportSeriesAndPassportNumber("1234", "567890"))
				.thenReturn(customer);
	}

	/**
	 * Методу передаются серия и номера паспорта.
	 * Если есть клиент с такими данными, возвращается объект Customer.
	 * Если нет, возвращается null.
	 */
	@Test
	public void testFindCustomer() {
		Customer resCustomer = customerService.findCustomer("1234", "567890");
		assertNotNull(resCustomer);

		resCustomer = customerService.findCustomer("1111", "222222");
		assertNull(resCustomer);
	}

	/**
	 * Методу передаются объект Customer и файл file.
	 * Если file == null, возвращается false.
	 * Если file существует, объект customer записывается
	 * в БД с добавлением ссылки на file и возвращается true.
	 */
	@Test
	public void testSaveCustomer() {
		try {
			boolean res = customerService.saveCustomer(customer, file);
			assertFalse(res);
		} catch (IOException e) {
			System.out.println("Exception: " + e);
		}
	}
}

