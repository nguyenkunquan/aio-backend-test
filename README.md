# 🚀 Overview
Design and implement a shift scheduling system using 2 scalable microservices:
- Data Service: Shift data and group management (backed by Elasticsearch)
- Scheduling Service: Weekly shift scheduling (asynchronous, backed by relational DB

Both services must support horizontal scaling to handle increased load efficiently.

# 🏃‍♂️ Quick Start

To get the entire project up and running, navigate to the project directory and execute:

```bash
docker-compose up --build
```

This command will:

- Build all necessary Docker images
- Start all services in the correct order
- Set up the complete development environment

# 📚 Detailed Documentation
For comprehensive information about each service and component, please refer to the individual README.md files located in each service directory.

# 🔧 Prerequisites
Make sure you have the following installed on your system:

- Docker
- Docker Compose
