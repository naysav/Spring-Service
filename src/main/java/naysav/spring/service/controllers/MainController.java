package naysav.spring.service.controllers;

import naysav.spring.service.models.Customer;
import naysav.spring.service.models.User;

import naysav.spring.service.services.CustomerService;
import naysav.spring.service.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Класс-контроллер, отвечающий за отображение страниц,
 * обработку Get/Post запросов, валидацию данных и
 * передачу данных сервисному слою для дальнейшей обработки.
 */
@Slf4j
@Controller
public class MainController {
	/**
	 * Сервисный класс для работы с аккаунтами пользователей.
	 */
	@Autowired
	UserService userService;

	/**
	 * Сервисный класс для работы с данными клиентов.
	 */
	@Autowired
	CustomerService customerService;

	/**
	 * Метод возвращает представление домашней страницы.
	 */
	@GetMapping("/")
	public String home() { return "home"; }

	/**
	 * Метод возвращает представление страницы авторизации.
	 */
	@GetMapping("/login")
	public String authorizationGet() { return "authorization"; }

	/**
	 * Метод возвращает представление страницы регистрации.
	 */
	@GetMapping("/registration")
	public String registrationGet(Model model) {
		model.addAttribute("user", new User());
		return "registration";
	}

	/**
	 * Метод валидирует данные регистрации и в случае
	 * успеха сохраняет их в БД user. После
	 * перенаправляет на страницу регистрации.
	 */
	@PostMapping("/registration")
	public String registrationProcess(@Valid User user, Errors errors) {
		if (errors.hasErrors())
			return "registration";
		if (!userService.saveUser(user)) {
			errors.rejectValue("username", "error.username",
					"• необходим другой логин, этот уже занят");
			return "registration";
		}
		log.info("User saved: " + user);
		return "redirect:/login";
	}

	/**
	 * Метод возвращает представление страницы поиска клиента.
	 */
	@GetMapping("/customers")
	public String customersGet() { return "customers"; }

	/**
	 * Метод валидирует серию и номер паспорта, ищет
	 * по ним клиента в БД Customers и возвращает
	 * обновленное представление.
	 */
	@PostMapping("/customers")
	public String customersFind(Model model,
	                            @RequestParam String passportSeries,
	                            @RequestParam String passportNumber) {
		if (!passportSeries.matches("\\d{4}"))
			model.addAttribute("errorSeries", "Серия состоит из 4 цифр");
		if (!passportNumber.matches("\\d{6}"))
			model.addAttribute("errorNumber", "Номер состоит из 6 цифр");
		if (model.getAttribute("errorSeries") != null ||
			model.getAttribute("errorNumber") != null)
			return "customers";
		Customer customer = customerService.findCustomer(passportSeries, passportNumber);
		if (customer == null) {
			model.addAttribute("error", "Клиент с паспортными данными "
					+ passportSeries + " " + passportNumber + " не найден");
			log.info("Customer not found: " + customer);
			return "customers";
		}
		log.info("Customer found: " + customer);
		model.addAttribute("customer", customer);
		return "customers";
	}

	/**
	 * Метод возращает представление с формой создания клиента.
	 */
	@GetMapping("/createCustomer")
	public String customersCreateGet(Model model) {
		model.addAttribute("customer", new Customer());
		return "createCustomer";
	}

	/**
	 * Метод валидирует данные создающегося клиента,
	 * в случае наличия инвалидных полей возвращает представление
	 * с ошибками, иначе сохраняет клиента в БД Customers и
	 * возвращает представление createCustomer.html с
	 * сообщении об успешной записи.
	 */
	@PostMapping("/createCustomer")
	public String createCustomerProcess(@Valid Customer customer, Errors errors,
	                                    Model model, @RequestParam("file") MultipartFile file,
	                                    RedirectAttributes redirectAttrs) throws IOException {
		if (errors.hasErrors())
			return "createCustomer";
		if (customerService.findCustomer(customer.getPassportSeries(),
				customer.getPassportNumber()) != null)
			model.addAttribute("errorCustomer",
					"Клиент с такими паспортными данными уже есть!");

		if(!"application/pdf".equals(file.getContentType())) {
			model.addAttribute("errorPDF",
					"Загруженный файл должен быть формата PDF!");
		}

		if (model.getAttribute("errorCustomer") == null &&
				model.getAttribute("errorPDF") == null) {
			if (!customerService.saveCustomer(customer, file))
				model.addAttribute("errorDB",
						"Не удалось добавить клиента!");
		}

		if (model.getAttribute("errorCustomer") != null ||
				model.getAttribute("errorDB") != null ||
				model.getAttribute("errorPDF") != null)
			return "createCustomer";
		else
			redirectAttrs.addFlashAttribute("successMessage",
					"Клиент успешно добавлен!");
		return "redirect:/createCustomer";
	}

	/**
	 * Метод отображает файл.
	 * @param filePath - локальный путь загрузки файла
	 * @return - страница просмотра файла PDF с пользовательскими данными
	 * @throws FileNotFoundException - если файл не найден
	 */
	@GetMapping("/fileView")
	public ResponseEntity<InputStreamResource> fileView(@RequestParam("path") String filePath)
			throws FileNotFoundException {
		File file = new File(filePath);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, file.getName())
				.contentType(MediaType.parseMediaType("application/pdf"))
				.contentLength(file.length())
				.body(resource);

	}

}
