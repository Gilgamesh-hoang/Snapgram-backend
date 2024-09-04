package org.snapgram;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }


    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

        };
    }
}
/*
 * thu hồi token:
 *v (x) tạo table trong database chứa RT, mỗi user có 3 RT
 *v (x) mỗi lần login sẽ tạo 1 RT mới, nếu đã đủ 3 RT thì sẽ đưa RT cũ nhất vào redis và tạo 1 RT mới
 *v (x) mỗi lần logout sẽ đưa RT vào blacklist va xoa RT trong database
 *v (x) mỗi lần RT thì sẽ trả ve ma 2 ma moi va dua RT cu vao blacklist va xoa RT cu trong database
 *v (x) mỗi lần RT sẽ kiểm tra RT có trong blacklist không, nếu có thì trả về lỗi
 *v (x) khi đổi mật khẩu sẽ đưa tất cả RT của user vào blacklist
 *v (x) khi đổi email sẽ đưa tất cả RT của user vào blacklist
 * Thuật toán bất đối xứng
 * mỗi lần login user có 1 cặp key cho access token và refresh token
 * 2 cap key nay luu trong db, tuy nhien private key sẽ được mã hóa trc khi luu
 *
 * */
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4