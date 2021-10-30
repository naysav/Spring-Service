INSERT INTO user(id, username, password, password_verify, role, first_name)
values('5', 'naysav', '$2a$10$zxhgnn.hkUFRlh3Qg/j0q.ngbXxEghXMhm2bfg493qJATYHYvN0.S',
       '$2a$10$zxhgnn.hkUFRlh3Qg/j0q.ngbXxEghXMhm2bfg493qJATYHYvN0.S', 'USER', 'VASILY');

INSERT INTO user(id, username, password, password_verify, role, first_name)
values('8', 'tuxedo', '$2a$10$zxhgnn.hkUFRlh3Qg/j0q.ngbXxEghXMhm2bfg493qJATYHYvN0.S',
       '$2a$10$zxhgnn.hkUFRlh3Qg/j0q.ngbXxEghXMhm2bfg493qJATYHYvN0.S', 'USER', 'VASILIY');

INSERT INTO customer(id, first_name, last_name, gender, age, passport_series, passport_number, phone_number, link_to_file)
values('7', 'Ivan', 'Ivanov', 'Male', '34', '4321', '098765', '1234567890', 'C:/my/testFile.pdf');