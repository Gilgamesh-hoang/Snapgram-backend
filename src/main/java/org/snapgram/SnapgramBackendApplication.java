package org.snapgram;

import org.snapgram.repository.database.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }

    @Autowired
    FollowRepository followRepository;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            UUID follower = UUID.fromString("00273374-33be-3703-a863-edaae868dbf4");
//            List<UUID> followee = List.of(
//                    UUID.fromString("83937518-781f-3bf4-a648-07a34fab5a9d"),
//                    UUID.fromString("586cdd67-e47d-34b4-8a2b-c670dbd5dd2c"),
//                    UUID.fromString("2f9e4420-42df-3d44-b976-424ece77a1f5"),
//                    UUID.fromString("ae29cd36-df38-3022-b950-9261a2b36184"),
//                    UUID.fromString("2d84c2eb-c1aa-30a3-8420-d395ec7cd756"),
//                    UUID.fromString("9ea64384-ba5e-3bd7-a5c9-0ddb23275d08")
//            );
//            System.out.println("s");
//            followRepository.findByFollowerIdAndFolloweeIdIn(follower, followee).forEach(System.out::println);
//            System.out.println("e");
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
 *
 * khi follow/unfollow thì cập nhật lại cache
 * */
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4