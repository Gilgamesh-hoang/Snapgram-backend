/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `snapgram` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `snapgram`;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` char(36) NOT NULL,
  `post_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `content` varchar(300) NOT NULL,
  `parent_comment_id` char(36) DEFAULT NULL,
  `sentiment` enum('POSITIVE','NEGATIVE','NEUTRAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `like_count` tinyint(1) DEFAULT '0',
  `level` tinyint(1) DEFAULT '0',
  `reply_count` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  KEY `user_id` (`user_id`),
  KEY `parent_comment_id` (`parent_comment_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `comment_ibfk_3` FOREIGN KEY (`parent_comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `comment_like` (
  `id` char(36) NOT NULL,
  `comment_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`comment_id`),
  KEY `comment_id` (`comment_id`),
  CONSTRAINT `comment_like_ibfk_1` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`),
  CONSTRAINT `comment_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `follow` (
  `id` char(36) NOT NULL,
  `follower` char(36) NOT NULL,
  `followee` char(36) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `closeness` float DEFAULT '1',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_follow_user` (`follower`) USING BTREE,
  KEY `FK_follow_user_2` (`followee`) USING BTREE,
  CONSTRAINT `FK_follow_user` FOREIGN KEY (`follower`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_follow_user_2` FOREIGN KEY (`followee`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message` (
  `id` char(36) NOT NULL,
  `sender_id` char(36) NOT NULL,
  `content` varchar(4000) NOT NULL,
  `type` enum('TEXT','IMAGE','VIDEO') NOT NULL,
  `created_at` timestamp NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message_conversation` (
  `id` char(36) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `owner_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `type` enum('USER','GROUP') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_message_conversation_user` (`owner_id`),
  CONSTRAINT `FK_message_conversation_user` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message_participant` (
  `id` char(36) NOT NULL,
  `conversation_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` char(36) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj2ywtc5meno2ouhf5pcq9rsbh` (`user_id`),
  KEY `FK9abrloaivnfpi9a25iopjx2rn` (`conversation_id`) USING BTREE,
  CONSTRAINT `FK9abrloaivnfpi9a25iopjx2rn` FOREIGN KEY (`conversation_id`) REFERENCES `message_conversation` (`id`),
  CONSTRAINT `FKj2ywtc5meno2ouhf5pcq9rsbh` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message_recipient` (
  `id` char(36) NOT NULL,
  `message_id` char(36) NOT NULL,
  `participant_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id` (`message_id`,`participant_id`) USING BTREE,
  KEY `recipient_group_id` (`participant_id`) USING BTREE,
  CONSTRAINT `message_recipient_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `message` (`id`),
  CONSTRAINT `message_recipient_ibfk_3` FOREIGN KEY (`participant_id`) REFERENCES `message_participant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notification_entity` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('LIKE_POST','COMMENT_POST','REPLY_COMMENT','LIKE_COMMENT','FOLLOW_USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `entity_id` char(36) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `notification_recipient` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `notification_entity_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `recipient_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_notification_recipient_notification_entity` (`notification_entity_id`),
  KEY `FK_notification_recipient_user` (`recipient_id`),
  CONSTRAINT `FK_notification_recipient_notification_entity` FOREIGN KEY (`notification_entity_id`) REFERENCES `notification_entity` (`id`),
  CONSTRAINT `FK_notification_recipient_user` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `notification_trigger` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `notification_entity_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `actor_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_notification_trigger_user` (`actor_id`),
  KEY `FK_notification_trigger_notification_entity` (`notification_entity_id`),
  CONSTRAINT `FK_notification_trigger_notification_entity` FOREIGN KEY (`notification_entity_id`) REFERENCES `notification_entity` (`id`),
  CONSTRAINT `FK_notification_trigger_user` FOREIGN KEY (`actor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `post` (
  `id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `caption` varchar(2200) DEFAULT NULL,
  `like_count` int DEFAULT '0',
  `comment_count` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `update_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `post_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `post_like` (
  `id` char(36) NOT NULL,
  `post_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  PRIMARY KEY (`id`),
  UNIQUE KEY `post_id` (`post_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `post_like_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `post_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `post_media` (
  `id` char(36) NOT NULL,
  `post_id` char(36) NOT NULL,
  `url` varchar(255) NOT NULL,
  `type` enum('IMAGE','VIDEO') NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `cloudinary_public_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `post_media_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `post_tag` (
  `post_id` char(36) NOT NULL,
  `tag_id` char(36) NOT NULL,
  KEY `FKac1wdchd2pnur3fl225obmlg0` (`tag_id`),
  KEY `FKc2auetuvsec0k566l0eyvr9cs` (`post_id`),
  CONSTRAINT `FKac1wdchd2pnur3fl225obmlg0` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`),
  CONSTRAINT `FKc2auetuvsec0k566l0eyvr9cs` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `saved` (
  `id` char(36) NOT NULL,
  `post_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `saved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `post_id` (`post_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `saved_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `saved_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tag` (
  `id` char(36) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1wdpsed5kna2y38hnbgrnhi5b` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `timeline` (
  `user_id` char(36) COLLATE utf8mb4_general_ci NOT NULL,
  `post_id` char(36) COLLATE utf8mb4_general_ci NOT NULL,
  `post_created_at` timestamp NOT NULL,
  PRIMARY KEY (`user_id`,`post_id`) USING BTREE,
  KEY `FK__createdAt` (`post_created_at`) USING BTREE,
  KEY `IDX_post_created_at` (`post_created_at`) USING BTREE,
  KEY `IDX3gpvq4k4caw29wiyct8kywrla` (`post_created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
/*!50100 PARTITION BY KEY (user_id)
PARTITIONS 8 */;

CREATE TABLE IF NOT EXISTS `token` (
  `id` char(36) NOT NULL,
  `user_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `expire_rt` datetime(6) DEFAULT NULL,
  `refresh_token_id` char(36) DEFAULT NULL,
  `expired` tinyint(1) DEFAULT '0',
  `type` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe32ek7ixanakfqsdaokm4q9y2` (`user_id`),
  CONSTRAINT `FKe32ek7ixanakfqsdaokm4q9y2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user` (
  `id` char(36) NOT NULL,
  `email` varchar(100) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_info` (
  `user_id` char(36) NOT NULL,
  `active_code` varchar(50) DEFAULT NULL,
  `bio` varchar(600) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `gender` enum('FEMALE','MALE') NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FKn8pl63y4abe7n0ls6topbqjh2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
