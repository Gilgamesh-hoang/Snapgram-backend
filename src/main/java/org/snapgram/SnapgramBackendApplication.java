package org.snapgram;

import org.snapgram.service.token.ITokenService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    ITokenService tokenService;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            tokenService.blacklistAllUserTokens(UUID.fromString("f4aea8f0-967c-4700-aea8-f0967c8700b8"));
//            KeyPair keyPair = keyService.generateKeyPair();
//            System.out.println("public key AT: " + keyPair.getPublicKeyAT());
//            System.out.println("private key AT: " + keyPair.getPrivateKeyAT());
//            System.out.println("public key RT: " + keyPair.getPublicKeyRT());
//            System.out.println("private key RT: " + keyPair.getPrivateKeyRT());
//            System.out.println("====================================");
//
//            String at = jwtService.generateAccessToken("at", keyPair.getPrivateKeyAT());
//            System.out.println(at);
//            System.out.println("verify at=true: " + jwtService.validateToken2(at, keyPair.getPublicKeyAT()));
//            System.out.println("verify at=false: " + jwtService.validateToken2(at, keyPair.getPrivateKeyAT()));
//            System.out.println("====================================");
//            String rt = jwtService.generateRefreshToken("rt", keyPair.getPrivateKeyRT());
//            System.out.println(rt);
//            System.out.println("verify rt=true: " + jwtService.validateToken2(rt, keyPair.getPublicKeyRT()));
//            System.out.println("verify rt=false: " + jwtService.validateToken2(rt, keyPair.getPrivateKeyRT()));
//            System.out.println("====================================");

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