<div align="center" >
<h1>SNAPGRAM (BACK-END)</h1>
</div>

##  Table of Contents

- [Overview](#overview)
- [Github Frontend and Sentiment Analysis](#github-backend-and-sentiments-analysis)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Getting Started](#getting-started)
   - [ Prerequisites](#prerequisites)
   - [ Installation](#installation)
- [License](#license)

---

##  Overview

Snapgram is a modern social media application inspired by Instagram, designed to provide users with a seamless platform for sharing photos and connecting with others. This repository contains the backend services that power the Snapgram application.

---

##  Github Frontend and Sentiment Analysis

* Frontend: [here](https://github.com/Gilgamesh-hoang/Snapgram-frontend.git)
* Sentiment Analysis: [here](https://github.com/Gilgamesh-hoang/Sentiment-Analysis-Comments.git)

---

## Technologies Used
* Backend Framework: Spring Boot, Spring Security, Spring Data JPA
* Database: MySQL
* API Documentation: Swagger API
* Messaging: Kafka
* Caching: Redis
* Search Engine: Elasticsearch
* Change Data Capture: Debezium
---

##  Features

* User authentication and authorization
* Photo uploads with captions
* Like and comment on posts
* Follow and unfollow users
* Recommendations for users to follow
* Real-time notifications
* Scalable and efficient backend architecture
* Change information profile
* Sync data between database and Elasticsearch using Debezium
* Search for users using Elasticsearch
* Send messages to other users or groups in real-time
* Search user using image with Facial Recognition Service
* Prevent the customs by Aho-Corasick algorithm
* Publish and subscribe to topics using Kafka
* Cache data using Redis


---

##  Getting Started

###  Prerequisites

Before getting started with Snapgram-backend, ensure your runtime environment meets the following requirements:

- **Programming Language:** Java 17
- **Package Manager:** Gradle
- Docker


###  Installation

Install Snapgram-backend using one of the following methods:

**Build from source:**

1. Clone the Snapgram-backend repository:
```sh
❯ git clone https://github.com/Gilgamesh-hoang/Snapgram-backend.git
```

2. Navigate to the project directory:
```sh
❯ cd Snapgram-backend
```

3. Install the project dependencies:


**Using `gradle`** &nbsp; [<img align="center" src="https://img.shields.io/badge/Gradle-02303A.svg?style={badge_style}&logo=gradle&logoColor=white" />](https://gradle.org/)

```sh
❯ ./gradlew build  # For macOS/Linux
❯ gradlew.bat build  # For Windows

```

4. Run docker-compose:
```sh
❯ docker-compose -f services-compose.yml up -d
```

5. Set up environment variables:
Create a `application.properties` file in the `\resources` folder and add the necessary environment variables.


6. Import database schema:
Create a database schema and import the schema from the `MySQL_DB.sql` file.


7. Run the application:
Run Snapgram-backend using the following command:
**Using `gradle`** &nbsp; [<img align="center" src="https://img.shields.io/badge/Gradle-02303A.svg?style={badge_style}&logo=gradle&logoColor=white" />](https://gradle.org/)

```sh
❯ ./gradlew bootRun  # Trên macOS/Linux
❯ gradlew.bat bootRun  # Trên Windows
```

---

##  License

This project is protected under the MIT license. See the [LICENSE](https://choosealicense.com/licenses/) file for more information.

---
