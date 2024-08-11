package org.snapgram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

//@Configuration
//@EnableElasticsearchRepositories(basePackages = "org.snapgram.repository.elasticsearch")
//public class ESConfig extends ElasticsearchConfiguration {
//
//    @Override
//    public ClientConfiguration clientConfiguration() {
//
//        return ClientConfiguration.builder()
//                .connectedTo("localhost:9200")
//                .usingSsl()
//                .withBasicAuth("root","elasticsearch")
//                .build();
//    }
//}
public class ESConfig{

}