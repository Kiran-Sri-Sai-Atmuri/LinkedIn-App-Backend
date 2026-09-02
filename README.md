LinkedIn-App-Backend

A high-performance, event-driven distributed system designed to handle professional networking, content publishing, timeline generation, and real-time alerts. By breaking down typical monolith bottlenecks, the system decouples post ingestion from timeline computation and search indexing using asynchronous messaging and specialized datastores.

System Architecture & Request Lifecycle
[ Client / Mobile / Web ]
           │
           ▼
[ Spring Cloud Gateway :8080 ] ── (Redis Token Bucket Rate Limiting & JWT Validation)
           │
  ┌────────┼──────────────┬──────────────┬──────────────┐
  │ HTTP   │ HTTP         │ HTTP         │ HTTP         │ HTTP
  ▼        ▼              ▼              ▼              ▼
[ User ] [ Post ]     [ Feed ]       [ Search ]    [ Notification ]
 :8081    :8082        :8083          :8084          :8085
(MySQL)  (MySQL+S3)   (Redis)     (Elasticsearch)  (Kafka Consumer)
           │
           ▼ (PostCreated, UserConnected events)
    [ Apache Kafka ] ───► Fanout Workers ───► Writes to Feed, Search, Notification


    
1.Edge Security & Routing: All client traffic
 enters through the API Gateway (8080), which     validates incoming JWTs and rejects abuse using Redis-backed token bucket rate limiting before requests reach internal networks.

2. Synchronous Core Operations: Client-driven mutations (profile edits, connection handshakes, media uploads) route synchronously via HTTP/OpenFeign to the User (8081) or Post (8082) services.

3. Asynchronous Decoupling (Kafka): State changes produce events (e.g., PostCreatedEvent, ConnectionAcceptedEvent) pushed to dedicated Kafka topics.

4. Independent Consumer Ingestion: Downstream microservices process Kafka events concurrently to update follower feeds, refresh search inverted indices, and deliver notifications without blocking the primary post-creation thread.

Conversation with Gemini
Hey gemini i have built backend of linked app with spring boot microservices

this is the architecture

🏗️ Architecture

Client → API Gateway (JWT validation + rate limiting)

↓

┌─────────┼──────────┬──────────┬──────────┐

↓ ↓ ↓ ↓ ↓

User Post Feed Search Notification

Service Service Service Service Service

8081 8082 8083 8084 8085

MySQL MySQL Redis Elastic Kafka

JWT AWS S3 Cache search consumers



🛠️ Tech Stack

Technology Purpose

Spring Boot 3.2 Backend framework

Apache Kafka Event driven communication

Elasticsearch 8.x Full text search

Redis Feed caching

MySQL Persistent storage

AWS S3 Media storage

JWT + BCrypt Authentication

Spring Cloud Gateway API Gateway

Docker + Docker Compose Containerization

OpenFeign Service to service calls





🗄️ Services & Ports

Service Port Database

API Gateway 8080 Redis (rate limiting)

User Service 8081 MySQL

Post Service 8082 MySQL + AWS S3

Feed Service 8083 Redis only

Search Service 8084 Elasticsearch only

Notification Service 8085 Stateless



So generate a detailed description describing this whole project

This backend mimics LinkedIn's core distributed workflows, separating high-write transactional services from read-intensive aggregation and event-driven fanouts.

Project Overview
A high-performance, event-driven distributed system designed to handle professional networking, content publishing, timeline generation, and real-time alerts. By breaking down typical monolith bottlenecks, the system decouples post ingestion from timeline computation and search indexing using asynchronous messaging and specialized datastores.

System Architecture & Request Lifecycle
[ Client / Mobile / Web ]
           │
           ▼
[ Spring Cloud Gateway :8080 ] ── (Redis Token Bucket Rate Limiting & JWT Validation)
           │
  ┌────────┼──────────────┬──────────────┬──────────────┐
  │ HTTP   │ HTTP         │ HTTP         │ HTTP         │ HTTP
  ▼        ▼              ▼              ▼              ▼
[ User ] [ Post ]     [ Feed ]       [ Search ]    [ Notification ]
 :8081    :8082        :8083          :8084          :8085
(MySQL)  (MySQL+S3)   (Redis)     (Elasticsearch)  (Kafka Consumer)
           │
           ▼ (PostCreated, UserConnected events)
    [ Apache Kafka ] ───► Fanout Workers ───► Writes to Feed, Search, Notification
Edge Security & Routing: All client traffic enters through the API Gateway (8080), which validates incoming JWTs and rejects abuse using Redis-backed token bucket rate limiting before requests reach internal networks.

Synchronous Core Operations: Client-driven mutations (profile edits, connection handshakes, media uploads) route synchronously via HTTP/OpenFeign to the User (8081) or Post (8082) services.

Asynchronous Decoupling (Kafka): State changes produce events (e.g., PostCreatedEvent, ConnectionAcceptedEvent) pushed to dedicated Kafka topics.

Independent Consumer Ingestion: Downstream microservices process Kafka events concurrently to update follower feeds, refresh search inverted indices, and deliver notifications without blocking the primary post-creation thread.

Service Breakdown
Service	Port	Storage / Infrastructure	Core Responsibilities
API Gateway	8080	Redis	Central reverse proxy, JWT claim verification, CORS filtering, and distributed rate limiting per user/IP.
User Service	8081	MySQL	User registration, authentication (BCrypt + JWT generation), profile data, and bidirectional connection graphs.
Post Service	8082	MySQL + AWS S3	Post authoring, pre-signed URL generation for direct-to-S3 media uploads, engagement persistence (likes/comments), and publishing PostCreatedEvent to Kafka.
Feed Service	8083	Redis	Serves low-latency timeline queries. Consumes post events and applies a fanout strategy to push post IDs into the Redis Sorted Sets (ZSET) of active followers.
Search Service	8084	Elasticsearch 8.x	Consumes user updates and post events to build full-text search indices with fuzzy matching across profile headlines, skills, and post content.
Notification Service	8085
