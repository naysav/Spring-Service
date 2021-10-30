package naysav.spring.service.integration;

import io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils;
import naysav.spring.service.models.Customer;
import naysav.spring.service.models.User;
import naysav.spring.service.repository.CustomerRepository;
import naysav.spring.service.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/** Класс, тестирующий вздаимодействие компонентов приложения. */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

	/** Экземпляр класса UserRepository для вздаимодействия с БД user */
	@Autowired
	private UserRepository userRepository;

	/** Экземпляр класса CustomerRepository для вздаимодействия с БД customer */
	@Autowired
	private CustomerRepository customerRepository;

	/** Экземпляр класса MockMvc, необходимый для
	 *  тестирования взаимодествия компонентов приложения*/
	@Autowired
	public MockMvc mockMvc;

	/** Модель User, необходимый для тестирования. */
	User user = new User();

	/** Модель Customer, необходимый для тестирования. */
	Customer customer = new Customer();

	/** Метод предварительно заполняет модели user и customer данными. */
	@Before
	public void setup() {
		user.setFirstName("TestJohn");
		user.setLastName("TestSmith");
		user.setUsername("TestUsername");
		user.setPassword("Testing123");
		user.setPasswordVerify("Testing123");
		user.setGender("Male");
		user.setAge("68");

		customer.setFirstName("testFirstName");
		customer.setLastName("testLastName");
		customer.setGender("Male");
		customer.setAge("21");
		customer.setPassportSeries("1234");
		customer.setPassportNumber("567890");
		customer.setPhoneNumber("1234567890");
	}

	/**
	 * Тестирует вызов Post-метода registrationProcess, обрабатывающего форму регистрации.
	 * Ожидаемый результат: статус ответа = 302, перенаправление на URL == "/login",
	 * данные пользователя внесены в БД
	 * @throws Exception
	 */
	@Test
	@Order(1)
	public void registrationPostTest() throws Exception {
		this.mockMvc.perform(post("/registration").with(MockMvcRequestBuilderUtils.form(user)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		User userResult = userRepository.findByUsername("TestUsername");
		assertNotNull(userResult);
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска по серии и номеру паспорта без ошибок.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * создан только аттрибут с найденным клиентом.
	 * @throws Exception
	 */
	@Test
	@Order(2)
	@WithMockUser
	public void customersPostTest() throws Exception {
		this.mockMvc.perform(post("/customers")
						.param("passportSeries", "4321")
						.param("passportNumber", "098765"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", is(nullValue())))
				.andExpect(model().attribute("errorNumber", is(nullValue())))
				.andExpect(model().attribute("error", is(nullValue())))
				.andExpect(model().attribute("customer", instanceOf(Customer.class)));
		Customer customerResult = customerRepository
				.findByPassportSeriesAndPassportNumber("4321", "098765");
		assertNotNull(customerResult);
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента с корректными данными.
	 * Ожидаемый результат: статус ответа = 302,  отображаемое представление == "createCustomer",
	 * создан только аттрибут перенаправления с успешно созданным клиентом,
	 * клиент внесен в БД, файл с его данными успешно создан и после проверки успешно удален.
	 * @throws Exception
	 */
	@Test
	@Order(3)
	@WithMockUser
	public void createCustomerPostTest() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/pdf", "information".getBytes());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer").file(file)
						.with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/createCustomer"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("errorCustomer", is(nullValue())))
				.andExpect(model().attribute("errorPDF", is(nullValue())))
				.andExpect(model().attribute("errorDB", is(nullValue())))
				.andExpect(flash().attribute("successMessage", instanceOf(String.class)));
		Customer customerResult = customerRepository
				.findByPassportSeriesAndPassportNumber("1234", "567890");
		File savedFile = new File(customerResult.getLinkToFile());
		assertNotNull(customerResult);
		assertTrue(savedFile.exists());
	}

	/**
	 * Тестирует вызов Get-метода fileView авторизованным пользователем,
	 * обрабатывающего отображение предварительно созданного PDF-файла.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление типа == "application/pdf",
	 * PDF-файл после проверки успешно удален.
	 * @throws Exception
	 */
	@Test
	@Order(4)
	@WithMockUser
	public void fileViewGetTest() throws Exception {
		String path = customerRepository.
				findByPassportSeriesAndPassportNumber("1234", "567890").getLinkToFile();
		File savedFile = new File(path);
		this.mockMvc.perform(get("/fileView")
							.param("path", path))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/pdf"));
		assertTrue(savedFile.delete());
	}
}
