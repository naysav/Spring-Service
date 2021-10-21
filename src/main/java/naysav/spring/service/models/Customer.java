package naysav.spring.service.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Модель, предназначенная для хранения и валидации учетных данных клиента
 */
@Data
@Entity
public class Customer{
	/**
	 * Индентификационный номер для таблицы customer
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Поле имени клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	private String firstName;

	/**
	 * Поле фамилии клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	private String lastName;

	/**
	 * Поле половой принадлежности клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	private String gender;

	/**
	 * Поле возраста клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	@Pattern(regexp="\\d{1,3}", message = "• необходимо неотрицательное число")
	private String age;

	/**
	 * Поле серии паспорта клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	@Pattern(regexp="\\d{4}", message = "• необходимо 4 цифры")
	private String passportSeries;

	/**
	 * Поле номера паспорта клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	@Pattern(regexp="\\d{6}", message = "• необходимо 6 цифр")
	private String passportNumber;

	/**
	 * Поле телефонного номера клиента
	 */
	@NotEmpty(message = "• обязательное поле")
	@Pattern(regexp="\\d{10}", message = "• необходимо 10 цифр")
	private String phoneNumber;

	/**
	 * Поле ссылки на PDF-файл с данными клиента
	 */
	private String linkToFile;

	public Customer() {	}
}
