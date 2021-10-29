package naysav.spring.service.controllers;

import io.florianlopes.spring.test.web.servlet.request.MockMvcRequestBuilderUtils;
import naysav.spring.service.models.Customer;
import naysav.spring.service.models.User;
import naysav.spring.service.services.CustomerService;
import naysav.spring.service.services.UserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileNotFoundException;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Класс, тестирующий веб-слой (MainController). */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class MainControllerTest{

	/** Имитация сервисного класса UserService */
	@MockBean
	UserService userService;

	/** Имитация сервисного класса CustomerService */
	@MockBean
	CustomerService customerService;

	/** Экземпляр класса MockMvc, необходимый для тестирования класса MainController */
	@Autowired
	public MockMvc mockMvc;

	/**
	 * Тестирует вызов Get-метода home, обрабатывающего URL == "/".
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "home"
	 * @throws Exception
	 */
	@Test
	public void homeTest() throws Exception {
		this.mockMvc.perform(get("/"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("home"));
	}

	/**
	 * Тестирует вызов Get-метода authorizationGet, обрабатывающего URL == "/login".
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "authorization"
	 * @throws Exception
	 */
	@Test
	public void loginGetTest() throws Exception {
		this.mockMvc.perform(get("/login"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("authorization"));
	}

	/**
	 * Тестирует вызов Get-метода registrationGet, обрабатывающего URL == "/registration".
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "registration",
	 * атрибут "user" создан
	 * @throws Exception
	 */
	@Test
	public void registrationGetTest() throws Exception {
		this.mockMvc.perform(get("/registration"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attribute("user", instanceOf(User.class)));
	}

	/**
	 * Тестирует вызов Post-метода registrationProcess, обрабатывающего форму регистрации.
	 * Ожидаемый результат: статус ответа = 302, перенаправление на URL == "/login",
	 * @throws Exception
	 */
	@Test
	public void registrationPostTest1() throws Exception {
		User user = createTestUser();

		when(userService.saveUser(user)).thenReturn(true);

		this.mockMvc.perform(post("/registration").with(MockMvcRequestBuilderUtils.form(user)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	/**
	 * Тестирует вызов Post-метода registrationProcess, обрабатывающего
	 * форму регистрации c инвалидным полем.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "registration",
	 * поле password аттрибута user имеет ошибку.
	 * @throws Exception
	 */
	@Test
	public void registrationPostTest2() throws Exception {
		User user = createTestUser();
		user.setPassword("1");

		when(userService.saveUser(user)).thenReturn(true);

		this.mockMvc.perform(post("/registration").with(MockMvcRequestBuilderUtils.form(user)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("user", "password"));
	}

	/**
	 * Тестирует вызов Post-метода registrationProcess, обрабатывающего
	 * форму регистрации c отличающимися друг от друга паролями.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "registration",
	 * наличие ошибки валидации.
	 * @throws Exception
	 */
	@Test
	public void registrationPostTest3() throws Exception {
		User user = createTestUser();
		user.setPassword("Testing1234");

		when(userService.saveUser(user)).thenReturn(true);

		this.mockMvc.perform(post("/registration").with(MockMvcRequestBuilderUtils.form(user)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().hasErrors());
	}

	/**
	 * Тестирует вызов Post-метода registrationProcess, обрабатывающего
	 * форму регистрации c уже существующим логином (поле username).
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "registration",
	 * поле username аттрибута user имеет ошибку.
	 * @throws Exception
	 */
	@Test
	public void registrationPostTest4() throws Exception {
		User user = createTestUser();

		when(userService.saveUser(user)).thenReturn(false);

		this.mockMvc.perform(post("/registration").with(MockMvcRequestBuilderUtils.form(user)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attributeHasFieldErrors("user", "username"));
	}

	/**
	 * Тестирует вызов Get-метода customersGet неавторизованным пользователем,
	 * обрабатывающего URL == "/customers".
	 * Ожидаемый результат: статус ответа = 302, перенаправление на URL == "/login".
	 * @throws Exception
	 */
	@Test
	public void customersGetTest1() throws Exception {
		this.mockMvc.perform(get("/customers"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	/**
	 * Тестирует вызов Get-метода customersGet авторизованным пользователем,
	 * обрабатывающего URL == "/customers".
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers".
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersGetTest2() throws Exception {
		this.mockMvc.perform(get("/customers"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"));
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска по серии и номеру паспорта без ошибок.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * создан только аттрибут с найденным клиентом.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersPostTest1() throws Exception {
		String passportSeries = "1234";
		String passportNumber = "567890";

		when(customerService.findCustomer(passportSeries, passportNumber)).thenReturn(createTestCustomer());

		this.mockMvc.perform(post("/customers")
				.param("passportSeries", passportSeries)
				.param("passportNumber", passportNumber))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", is(nullValue())))
				.andExpect(model().attribute("errorNumber", is(nullValue())))
				.andExpect(model().attribute("error", is(nullValue())))
				.andExpect(model().attribute("customer", instanceOf(Customer.class)));
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска по серии и номеру паспорта с инвалилным полем серии.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * создан только аттрибут с ошибкой некорректного ввода серии паспорта.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersPostTest2() throws Exception {
		String passportSeries = "12345";
		String passportNumber = "567890";

		when(customerService.findCustomer(passportSeries, passportNumber)).thenReturn(createTestCustomer());

		this.mockMvc.perform(post("/customers")
						.param("passportSeries", passportSeries)
						.param("passportNumber", passportNumber))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", instanceOf(String.class)))
				.andExpect(model().attribute("errorNumber", is(nullValue())))
				.andExpect(model().attribute("error", is(nullValue())))
				.andExpect(model().attribute("customer", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска по серии и номеру паспорта с инвалилным полем номера.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * создан только аттрибут с ошибкой некорректного ввода номера паспорта.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersPostTest3() throws Exception {
		String passportSeries = "1234";
		String passportNumber = "56789";

		when(customerService.findCustomer(passportSeries, passportNumber)).thenReturn(createTestCustomer());

		this.mockMvc.perform(post("/customers")
						.param("passportSeries", passportSeries)
						.param("passportNumber", passportNumber))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", is(nullValue())))
				.andExpect(model().attribute("errorNumber", instanceOf(String.class)))
				.andExpect(model().attribute("error", is(nullValue())))
				.andExpect(model().attribute("customer", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска по серии и номеру паспорта с инвалилным полями.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * созданы только аттрибуты с ошибками некорректного ввода серии и номера паспорта.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersPostTest4() throws Exception {
		String passportSeries = "12345";
		String passportNumber = "56789";

		when(customerService.findCustomer(passportSeries, passportNumber)).thenReturn(createTestCustomer());

		this.mockMvc.perform(post("/customers")
						.param("passportSeries", passportSeries)
						.param("passportNumber", passportNumber))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", instanceOf(String.class)))
				.andExpect(model().attribute("errorNumber", instanceOf(String.class)))
				.andExpect(model().attribute("error", is(nullValue())))
				.andExpect(model().attribute("customer", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода customersFind авторизованным пользователем,
	 * обрабатывающего форму поиска несуществующего клиента по серии и номеру паспорта.
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "customers",
	 * создан только аттрибут с сообщением об отсутствии клиента в базе данных.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersPostTest5() throws Exception {
		String passportSeries = "1234";
		String passportNumber = "567890";

		when(customerService.findCustomer(passportSeries, passportNumber)).thenReturn(null);

		this.mockMvc.perform(post("/customers")
						.param("passportSeries", passportSeries)
						.param("passportNumber", passportNumber))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("customers"))
				.andExpect(model().attribute("errorSeries", is(nullValue())))
				.andExpect(model().attribute("errorNumber", is(nullValue())))
				.andExpect(model().attribute("error", instanceOf(String.class)))
				.andExpect(model().attribute("customer", is(nullValue())));
	}

	/**
	 * Тестирует вызов Get-метода customersCreateGet неавторизованным пользователем,
	 * обрабатывающего URL == "/createCustomer".
	 * Ожидаемый результат: статус ответа = 302, перенаправление на URL == "/login".
	 * @throws Exception
	 */
	@Test
	public void customersCreateGetTest1() throws Exception {
		this.mockMvc.perform(get("/createCustomer"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	/**
	 * Тестирует вызов Get-метода customersCreateGet авторизованным пользователем,
	 * обрабатывающего URL == "/createCustomer".
	 * Ожидаемый результат: статус ответа = 200, отображаемое представление == "createCustomer",
	 * создан аттрибут customer для формы создания клиента.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void customersCreateGetTest2() throws Exception {
		this.mockMvc.perform(get("/createCustomer"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("createCustomer"))
				.andExpect(model().attribute("customer", instanceOf(Customer.class)));
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента с корректными данными.
	 * Ожидаемый результат: статус ответа = 302,  отображаемое представление == "createCustomer",
	 * создан только аттрибут перенаправления с успешно созданным клиентом.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void createCustomerPostTest1() throws Exception {
		Customer customer = createTestCustomer();
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/pdf", "information".getBytes());

		when(customerService.findCustomer(customer.getPassportSeries(),
				customer.getPassportNumber())).thenReturn(null);
		when(customerService.saveCustomer(customer, file)).thenReturn(true);

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer").file(file)
					/**
					 * MockMvcRequestBuilderUtils - инструмент передачи полей формы в виде объекта в MockMvc
					 * документация - https://github.com/f-lopes/spring-mvc-test-utils
					 */
					.with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/createCustomer"))
				.andExpect(model().hasNoErrors())
				.andExpect(model().attribute("errorCustomer", is(nullValue())))
				.andExpect(model().attribute("errorPDF", is(nullValue())))
				.andExpect(model().attribute("errorDB", is(nullValue())))
				.andExpect(flash().attribute("successMessage", instanceOf(String.class)));
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента с некорректным полем.
	 * Ожидаемый результат: статус ответа = 200, перенаправление на URL == "/createCustomer",
	 * создан только аттрибут с сообщением об инвалидном поле passportSeries модели customer.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void createCustomerPostTest2() throws Exception {
		Customer customer = createTestCustomer();
		customer.setPassportSeries("1");
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/pdf", "information".getBytes());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer")
								.file(file).with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("createCustomer"))
				.andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("customer", "passportSeries"))
				.andExpect(model().attribute("errorCustomer", is(nullValue())))
				.andExpect(model().attribute("errorPDF", is(nullValue())))
				.andExpect(model().attribute("errorDB", is(nullValue())))
				.andExpect(flash().attribute("successMessage", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента с серией и номером паспорта уже существующего.
	 * Ожидаемый результат: статус ответа = 200, перенаправление на URL == "/createCustomer",
	 * создан только аттрибут с сообщением об уже занятых паспортных данных.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void createCustomerPostTest3() throws Exception {
		Customer customer = createTestCustomer();
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/pdf", "information".getBytes());

		when(customerService.findCustomer(customer.getPassportSeries(),
				customer.getPassportNumber())).thenReturn(createTestCustomer());

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer")
								.file(file).with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("createCustomer"))
				.andExpect(model().attribute("errorCustomer", instanceOf(String.class)))
				.andExpect(model().attribute("errorPDF", is(nullValue())))
				.andExpect(model().attribute("errorDB", is(nullValue())))
				.andExpect(flash().attribute("successMessage", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента с внесенным файлом инвалидного формата (не PDF).
	 * Ожидаемый результат: статус ответа = 200, перенаправление на URL == "/createCustomer",
	 * создан только аттрибут с сообщением инвалидном формате.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void createCustomerPostTest4() throws Exception {
		Customer customer = createTestCustomer();
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/ogg", "information".getBytes());

		when(customerService.findCustomer(customer.getPassportSeries(),
				customer.getPassportNumber())).thenReturn(null);
		when(customerService.saveCustomer(customer, file)).thenReturn(false);

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer")
								.file(file).with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("createCustomer"))
				.andExpect(model().attribute("errorCustomer", is(nullValue())))
				.andExpect(model().attribute("errorPDF", instanceOf(String.class)))
				.andExpect(model().attribute("errorDB", is(nullValue())))
				.andExpect(flash().attribute("successMessage", is(nullValue())));
	}

	/**
	 * Тестирует вызов Post-метода createCustomerProcess авторизованным пользователем,
	 * обрабатывающего форму создания клиента при неудачной записи в БД.
	 * Ожидаемый результат: статус ответа = 200, перенаправление на URL == "/createCustomer",
	 * создан только аттрибут с сообщением об ошибке записи в БД.
	 * @throws Exception
	 */
	@Test
	@WithMockUser
	public void createCustomerPostTest5() throws Exception {
		Customer customer = createTestCustomer();
		MockMultipartFile file = new MockMultipartFile("file", "INFO.pdf",
				"application/pdf", "information".getBytes());

		when(customerService.findCustomer(customer.getPassportSeries(),
				customer.getPassportNumber())).thenReturn(null);
		when(customerService.saveCustomer(customer, file)).thenReturn(false);

		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/createCustomer")
						.file(file).with(MockMvcRequestBuilderUtils.form(customer)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("createCustomer"))
				.andExpect(model().attribute("errorCustomer", is(nullValue())))
				.andExpect(model().attribute("errorPDF", is(nullValue())))
				.andExpect(model().attribute("errorDB", instanceOf(String.class)))
				.andExpect(flash().attribute("successMessage", is(nullValue())));
	}

	/**
	 * Тестирует вызов Get-метода fileView авторизованным пользователем,
	 * обрабатывающего отображение несуществующего файла PDF-файла.
	 * Ожидаемый результат: выброшено исключение FileNotFoundException
	 * @throws Exception
	 */
	@Test(expected = FileNotFoundException.class)
	@WithMockUser
	public void fileViewTest() throws Exception {
		Customer customer = createTestCustomer();

		this.mockMvc.perform(get("/fileView")
						.param("path", customer.getLinkToFile()))
				.andDo(print());
	}

	/**
	 * Метод создает тестовую модель пользователя.
	 * @return User
	 */
	public User createTestUser() {
		User user = new User();
		user.setFirstName("Тест");
		user.setLastName("Тестов");
		user.setUsername("Test");
		user.setPassword("Testing123");
		user.setPasswordVerify("Testing123");
		return user;
	}

	/**
	 * Метод создает тестовую модель клиента.
	 * @return Customer
	 */
	public Customer createTestCustomer() {
		Customer customer = new Customer();
		customer.setId(1L);
		customer.setFirstName("Иван");
		customer.setLastName("Иванов");
		customer.setGender("Мужчина");
		customer.setAge("19");
		customer.setPassportSeries("1234");
		customer.setPassportNumber("567890");
		customer.setPhoneNumber("1234567890");
		customer.setLinkToFile("C:/my/3a18dac2-7a07-4166-a991-f7e9fe77b1f0.INFO.pdf");
		return customer;
	}
}
