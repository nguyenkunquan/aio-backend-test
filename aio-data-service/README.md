# 👥 AIO Data Service

A scalable REST API for managing staff and hierarchical staff groups with Elasticsearch storage, Redis caching, and horizontal scaling support.
# 🚀 Features

- Staff Management: Full CRUD operations for staff records
- Staff Group Management: Full CRUD operations for staff records
- Hierarchical Staff Groups: Support unlimited nesting levels
- Group Membership: Add/remove staff to/from groups with validation
- Batch Import: JSON file import for staff and staff groups
- Group Resolution: Resolve all members including nested groups
- Elasticsearch Storage: Fast search and scalable data storage
- Redis Caching: Distributed caching for read-heavy operations
- Horizontal Scaling: Stateless design with distributed locks
- Cache Invalidation: Automatic cache clearing after updates
- RESTful Design: Clean API design with proper HTTP semantics

# 🗃️ Database Schema

## 1. Staff
```json
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "staffCode": {
        "type": "keyword"
      },
      "name": {
        "type": "text"
      },
      "email": {
        "type": "keyword"
      },
      "createdAt": {
        "type": "date",
        "format": "yyyy-MM-dd"
      },
      "updatedAt": {
        "type": "date",
        "format": "yyyy-MM-dd"
      }
    }
  }
}
```
### 2. Staff Group
```json
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "groupCode": {
        "type": "keyword"
      },
      "name": {
        "type": "text"
      },
      "parentId": {
        "type": "keyword"
      },
      "children": {
        "type": "nested",
        "properties": {
          "id": {
            "type": "keyword"
          },
          "groupCode": {
            "type": "keyword"
          },
          "name": {
            "type": "text"
          },
          "parentId": {
            "type": "keyword"
          },
          "memberIds": {
            "type": "keyword"
          },
          "createdAt": {
            "type": "date",
            "format": "yyyy-MM-dd"
          },
          "updatedAt": {
            "type": "date",
            "format": "yyyy-MM-dd"
          }
        }
      },
      "memberIds": {
        "type": "keyword"
      },
      "createdAt": {
        "type": "date",
        "format": "yyyy-MM-dd"
      },
      "updatedAt": {
        "type": "date",
        "format": "yyyy-MM-dd"
      }
    }
  }
}
```

# 📖 API Documentation

### Swagger UI Access
Once the application is running, access the interactive API documentation at:
```
http://localhost:1975/api/v1/swagger-ui/index.html
```

# 📊 Sample Data


---

**Happy Coding!** 🚀
