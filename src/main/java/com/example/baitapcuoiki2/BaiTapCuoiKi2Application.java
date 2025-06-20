package com.example.baitapcuoiki2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class BaiTapCuoiKi2Application {

    public static void main(String[] args) {
        SpringApplication.run(BaiTapCuoiKi2Application.class, args);
    }

}
