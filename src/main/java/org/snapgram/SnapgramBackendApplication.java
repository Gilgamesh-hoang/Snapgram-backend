package org.snapgram;

import org.snapgram.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
//@EnableJpaRepositories(basePackages = "org.snapgram.repository.database")
//@EnableElasticsearchRepositories(basePackages = "org.snapgram.repository.elasticsearch")
public class SnapgramBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);

    }

    @Autowired
    IUserService userService;

//    @Autowired
//    ElasticsearchOperations elasticsearchOperations;
//    IUserRepository userRepository;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            List<UserDocument> list = new ArrayList<>();
//            userRepository.findAll().forEach(user ->
//                    list.add(UserDocument.builder().id(user.getId()).email(user.getEmail()).fullName(user.getFullName()).isActive(user.getIsActive()).isDeleted(user.getIsDeleted()).nickname(user.getNickname()).build()));
//            userElasticRepository.saveAll(list);
//            System.out.println("saved " + list.size() + " users to elastic search");
//            userElasticRepository.searchByWildcard("uns").forEach(System.out::println);
//            int pageNumber = 0; // start from first page
//            int pageSize = 2; // size of each page
//
//            Pageable pageable = PageRequest.of(pageNumber, pageSize);
//
//            String searchTerm = "uns";
//            userService.findByKeyword(searchTerm, pageable).forEach(System.out::println);
        };
    }

}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:9000 -D sonar.login=sqp_d7c21f84c155366567f0566185617822378096d0