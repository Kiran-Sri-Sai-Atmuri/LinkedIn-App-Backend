🚀 LinkedIn-Style Social Media Backend — Microservices

A scalable LinkedIn-style social media backend built using Spring Boot Microservices, designed to handle user authentication, post creation, personalized feeds, full-text search, media storage, and event-driven notifications.

The project demonstrates how modern backend technologies such as Apache Kafka, Redis, Elasticsearch, MySQL, AWS S3, JWT, Spring Cloud Gateway, OpenFeign, Docker, and Docker Compose can work together to build a distributed and scalable backend system.

---

🏗️ Architecture

                         ┌─────────────────┐
                         │     Client      │
                         │  Web / Mobile   │
                         └────────┬────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │       API Gateway        │
                    │       Port: 8080         │
                    │                          │
                    │ • JWT Validation         │
                    │ • Rate Limiting          │
                    │ • Request Routing        │
                    └────────────┬─────────────┘
                                 │
             ┌───────────────────┼────────────────────┐
             │                   │                    │
             ▼                   ▼                    ▼
      ┌────────────┐      ┌────────────┐      ┌────────────┐
      │    User    │      │    Post    │      │    Feed    │
      │  Service   │      │  Service   │      │  Service   │
      │   :8081    │      │   :8082    │      │   :8083    │
      └─────┬──────┘      └─────┬──────┘      └─────┬──────┘
            │                   │                    │
          MySQL               MySQL                Redis
                                │
                              AWS S3

             ┌──────────────────┴──────────────────┐
             │                                     │
             ▼                                     ▼
      ┌────────────┐                       ┌────────────────┐
      │   Search   │                       │ Notification   │
      │  Service   │                       │    Service     │
      │   :8084    │                       │     :8085      │
      └─────┬──────┘                       └───────┬────────┘
            │                                      │
      Elasticsearch                               Kafka

---

🧩 Microservices

Service| Port| Responsibility| Storage
API Gateway| "8080"| Routing, JWT validation, rate limiting| Redis
User Service| "8081"| Registration, login, user management| MySQL
Post Service| "8082"| Create/manage posts and media| MySQL + S3
Feed Service| "8083"| Personalized feed and caching| Redis
Search Service| "8084"| Full-text search| Elasticsearch
Notification Service| "8085"| Event-driven notifications| Stateless

---

🛠️ Tech Stack

Technology| Purpose
Java| Primary programming language
Spring Boot 3.2| Microservices framework
Spring Cloud Gateway| API Gateway and request routing
Apache Kafka| Event-driven communication
Redis| Feed caching and rate limiting
MySQL| Persistent transactional storage
Elasticsearch 8.x| Full-text search
AWS S3| Media/object storage
JWT| Authentication
BCrypt| Secure password hashing
OpenFeign| Synchronous service-to-service communication
Docker| Containerization
Docker Compose| Multi-container development environment

---


🎯 Key Features

- ✅ Microservices architecture
- ✅ JWT authentication
- ✅ BCrypt password hashing
- ✅ API Gateway
- ✅ User management
- ✅ Post management
- ✅ AWS S3 media storage
- ✅ Event-driven architecture with Kafka
- ✅ Personalized feed caching
- ✅ Redis caching
- ✅ Elasticsearch full-text search
- ✅ OpenFeign service communication
- ✅ Stateless notification service
- ✅ Database-per-service architecture
- ✅ Docker Compose environment

---

📚 What I Learned

This project helped me understand how different backend technologies work together in a distributed system rather than using them as isolated tools.

Key concepts explored:

- Microservice decomposition
- API Gateway pattern
- JWT authentication
- Secure password storage
- Synchronous vs asynchronous communication
- Event-driven architecture
- Kafka producers and consumers
- Redis caching
- Elasticsearch indexing and searching
- AWS S3 object storage
- Service-to-service communication
- Stateless services
- Database-per-service architecture

---

📌 Project Summary

This project is a production-inspired LinkedIn-style social media backend built using Spring Boot Microservices.

It combines:

Spring Boot
     +
Microservices
     +
Kafka
     +
Redis
     +
Elasticsearch
     +
MySQL
     +
AWS S3
     +
JWT
     +
OpenFeign
     +
Docker

The architecture focuses on scalability, loose coupling, asynchronous processing, caching, secure authentication, efficient search, and cloud-based media storage.

---

👨‍💻 Author

Kiran Sri Sai

Built as a hands-on project to explore Spring Boot, Microservices, Distributed Systems, Event-Driven Architecture, Cloud Storage, Caching, and Search Technologies.
