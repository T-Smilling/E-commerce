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

import java.util.List;

@SpringBootApplication
@SecurityScheme(name = "E-Commerce Application", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class ECommerceApplication implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load(); // Tải file .env

        System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("MYSQL_USERNAME", dotenv.get("MYSQL_USERNAME"));
        System.setProperty("MYSQL_ROOT_PASSWORD", dotenv.get("MYSQL_ROOT_PASSWORD"));

        SpringApplication.run(ECommerceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            RoleEntity adminRole = null;
            RoleEntity userRole = null;
            // Kiểm tra nếu role "ADMIN" đã tồn tại
            if (!roleRepository.existsByRoleName("ADMIN")) {
                adminRole = new RoleEntity();
                adminRole.setId(MessageUtils.ADMIN_ID);
                adminRole.setRoleName("ADMIN");
                roleRepository.save(adminRole);  // Chỉ lưu nếu chưa tồn tại
            }

            // Kiểm tra nếu role "USER" đã tồn tại
            if (!roleRepository.existsByRoleName("USER")) {
                userRole = new RoleEntity();
                userRole.setId(MessageUtils.USER_ID);
                userRole.setRoleName("USER");
                roleRepository.save(userRole);  // Chỉ lưu nếu chưa tồn tại
            }

            if (adminRole != null && userRole != null) {
                List<RoleEntity> roles = List.of(adminRole, userRole);

                List<RoleEntity> savedRoles = roleRepository.saveAll(roles);

                savedRoles.forEach(System.out::println);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
