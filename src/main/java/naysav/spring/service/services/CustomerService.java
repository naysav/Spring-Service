package naysav.spring.service.services;

import lombok.extern.slf4j.Slf4j;
import naysav.spring.service.models.Customer;
import naysav.spring.service.repository.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Сервисный класс для чтения и записи данных таблицы customer,
 * полученных от класса Controllers.
 */
@Slf4j
@Service
public class CustomerService {

	/**
	 * Поле для доступа к таблице customer.
	 */
	@Autowired
	CustomerRepository customerRepository;

	/**
	 * Поле, хранящее путь загрузки PDF-файлов.
	 */
	@Value("${upload.path}")
	private String uploadPath;

	/**
	 * Метод поиска клиента в таблице customer
	 * @param passportSeries - серия паспорта
	 * @param passportNumber - номер паспорта
	 * @return модель Customer с данными клиента,
	 *         если не найдена, то null
	 */
	@Transactional(readOnly = true)
	public Customer findCustomer(String passportSeries, String passportNumber)
	{
		Customer customerFromDB = customerRepository.
				findByPassportSeriesAndPassportNumber(passportSeries, passportNumber);

		return customerFromDB;
	}

	/**
	 * Метод записывает данные клиента и ссылку на файл с его данными
	 * в таблицу customer
	 * @param customer - модель, хранящая данные клиента
	 * @param file - PDF-файл с данными клиента
	 * @return true, если успешно сохранены данные
	 *         false, если сохранение не удалось
	 */
	@Transactional()
	public boolean saveCustomer(Customer customer, MultipartFile file)
			throws IOException {

		if (file != null) {
			File uploadDirectory = new File(uploadPath);

			if (!uploadDirectory.exists())
				uploadDirectory.mkdir();

			String uuidFile = UUID.randomUUID().toString();
			String resultFilename = uuidFile + "." + file.getOriginalFilename();

			file.transferTo(new File(uploadPath + "/" + resultFilename));
			customer.setLinkToFile(uploadPath + "/" + resultFilename);
		}
		else
			return false;
		try {
			customerRepository.save(customer);
			log.info("Customer saved: " + customer);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

