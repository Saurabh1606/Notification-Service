# 📬 Notification Service

A scalable, modular **Notification Service** built with **Spring Boot 3**, **Kafka**, **Redis**, **WebSockets**, and **PostgreSQL** — supporting **Email**, **Push**, and **SMS** channels with support for **scheduled delivery**, **failover**, **user preferences**, and **metrics (Prometheus/Grafana)**.

---

## ⚙️ Features

- ✅ **Priority-based Queuing** (HIGH, NORMAL, LOW)
- 📧 **Multi-channel Delivery**: Email, Push, SMS
- 🕒 **Scheduled Notifications** via cron polling
- 📬 **Real-Time Delivery Status** via WebSockets
- 🔁 **Failover Support** (e.g., fallback from SendGrid → Mailgun)
- 🧠 **User Preferences**: mute/unmute, channel preferences
- 📊 **Prometheus & Grafana Metrics**
- 📦 **Kafka-based Fanout + Retry Mechanism**
- 🔒 Secure with Spring Security + Redis-backed Sessions

---

## 🧱 Tech Stack

| Layer            | Tech                                 |
|------------------|--------------------------------------|
| Language         | Java 21                              |
| Framework        | Spring Boot 3.5                      |
| Message Queue    | Apache Kafka                         |
| Data Store       | PostgreSQL, Redis                    |
| WebSockets       | Spring WebSocket (STOMP)             |
| Monitoring       | Prometheus, Grafana                  |
| Tracing          | Zipkin + Micrometer Tracing          |
| Containerization | Docker & Docker Compose              |
