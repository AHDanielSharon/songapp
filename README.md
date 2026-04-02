# 🎵 SoundWave — Global Music Streaming Platform

A full-stack music streaming web application built with **Java Spring Boot** (backend) + HTML/CSS/JS (frontend).

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17 + Spring Boot 3.2 |
| **Database** | H2 (file-based, no setup needed) |
| **API** | REST (Spring Web MVC) |
| **ORM** | Spring Data JPA + Hibernate |
| **Build** | Apache Maven |
| **Frontend** | HTML5, CSS3, Vanilla JS |
| **Deploy** | Render.com |

## 📁 Java Project Structure

```
src/main/java/com/soundwave/
├── SoundWaveApplication.java        ← Main Spring Boot entry point
├── model/
│   └── Song.java                    ← JPA Entity (database model)
├── repository/
│   └── SongRepository.java          ← Spring Data JPA queries
├── service/
│   └── SongService.java             ← Business logic, file handling
├── controller/
│   ├── SongController.java          ← REST API endpoints
│   └── WebController.java           ← Serves the frontend
└── config/
    └── WebConfig.java               ← CORS, static resources
```

## 🚀 Features

- 🎵 **Upload any audio** — MP3, WAV, FLAC, OGG, AAC, M4A, OPUS (no size limit)
- 🔍 **Global search** — search by title, artist, album, genre, tags
- 🔥 **Trending** — most played songs
- ❤️ **Most Loved** — highest liked songs
- 🆕 **New Releases** — recently uploaded
- 🎲 **Discover** — random picks
- ▶️ **Full player** — play/pause, next/prev, seek, volume
- 🔀 **Shuffle & Repeat** — all repeat modes
- 💾 **My Uploads** — track your own uploads
- 📊 **Genre stats** — see what's popular
- 🌍 **No auth needed** — everyone can listen

## 🖥 Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# Clone / extract the project
cd soundwave

# Build
mvn clean package -DskipTests

# Run
java -jar target/soundwave-1.0.0.jar

# Open browser
open http://localhost:8080
```

## 🌐 Deploy to Render

1. Push this project to a **GitHub repository**
2. Go to [render.com](https://render.com) → **New Web Service**
3. Connect your GitHub repo
4. Fill in:
   - **Environment**: Java
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/soundwave-1.0.0.jar`
5. Add environment variable: `PORT = 10000`
6. Click **Deploy**

> The `render.yaml` file in this project auto-configures everything!

## 🔗 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/songs/upload` | Upload a song |
| GET | `/api/songs/{id}/stream` | Stream audio |
| GET | `/api/songs/search?q=...` | Search songs |
| GET | `/api/songs/trending` | Top 20 by plays |
| GET | `/api/songs/recent` | Top 20 by upload date |
| GET | `/api/songs/liked` | Top 20 by likes |
| GET | `/api/songs/discover` | 10 random picks |
| GET | `/api/songs/genres` | Genre counts |
| POST | `/api/songs/{id}/like` | Like a song |
| DELETE | `/api/songs/{id}` | Delete a song |
| GET | `/api/songs/health` | Health check |

## 📋 College Project Notes

This project demonstrates:
- **OOP in Java** — Entity classes, Service layer, Repository pattern
- **Spring Boot** — Dependency injection, auto-configuration
- **REST API design** — CRUD operations over HTTP
- **File I/O in Java** — `java.nio.file` for audio storage
- **JPA/Hibernate** — ORM, custom JPQL queries
- **Maven** — dependency management and build
- **MVC Pattern** — Controller → Service → Repository → DB
