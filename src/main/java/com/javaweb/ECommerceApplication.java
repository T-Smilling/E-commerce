package com.javaweb;

import com.javaweb.entity.RoleEntity;
import com.javaweb.repository.RoleRepository;
import com.javaweb.utils.MessageUtils;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SecurityScheme(name = "E-Commerce", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class ECommerceApplication implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load(); // Tải file .env

        System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("MYSQL_USERNAME", dotenv.get("MYSQL_USERNAME"));
        System.setProperty("MYSQL_ROOT_PASSWORD", dotenv.get("MYSQL_ROOT_PASSWORD"));
        System.setProperty("EMAIL_USER", dotenv.get("EMAIL_USER"));
        System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));

        SpringApplication.run(ECommerceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Kiểm tra nếu role "ADMIN" đã tồn tại, nếu chưa thì tạo mới
            if (!roleRepository.existsByRoleName("ADMIN")) {
                RoleEntity adminRole = new RoleEntity();
                adminRole.setId(MessageUtils.ADMIN_ID);
                adminRole.setRoleName("ADMIN");
                roleRepository.save(adminRole);  // Lưu role "ADMIN"
                System.out.println("ADMIN role đã được tạo.");
            } else {
                System.out.println("ADMIN role đã tồn tại.");
            }

            // Kiểm tra nếu role "USER" đã tồn tại, nếu chưa thì tạo mới
            if (!roleRepository.existsByRoleName("USER")) {
                RoleEntity userRole = new RoleEntity();
                userRole.setId(MessageUtils.USER_ID);
                userRole.setRoleName("USER");
                roleRepository.save(userRole);  // Lưu role "USER"
                System.out.println("USER role đã được tạo.");
            } else {
                System.out.println("USER role đã tồn tại.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
