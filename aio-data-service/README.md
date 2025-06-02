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

### 1. Staff
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
> ⚠️ **Note:** The `id` of each document is generated using `UUID.randomUUID()`.  
> Sample data is for demonstration purposes only — you must retrieve the actual ID after saving to the database.
### Staff Document Sample
```json
{
  "id": "baad0a85-23e3-4a45-96f5-111a2f735031",
  "staffCode": "STAFF-01",
  "name": "Quan Nguyen Do Minh",
  "email": "quanndm@company.com",
  "createdAt": "2025-06-02",
  "updatedAt": "2025-06-02"
}
```

### Staff Group Document Sample
```json
{
    "id": "5352cc05-d19a-459f-82b6-fe762a05299b",
    "groupCode": "DEV-TEAM",
    "name": "Development Team",
    "parentId": "c2fa1fae-0418-4662-af45-bb77fd150cf9",
    "children": [
      {
        "id": "3ea1ec6b-a26b-4d14-bcb1-73f86436a4f3",
        "groupCode": "FRONTEND-TEAM",
        "name": "Frontend Development Team",
        "parentId": "5352cc05-d19a-459f-82b6-fe762a05299b",
        "createdAt": "2025-06-02",
        "updatedAt": "2025-06-02"
      },
      {
        "id": "dae90712-a580-4fed-a30f-a31f1d235f96",
        "groupCode": "BACKEND-TEAM",
        "name": "Backend Development Team",
        "parentId": "5352cc05-d19a-459f-82b6-fe762a05299b",
        "createdAt": "2025-06-02",
        "updatedAt": "2025-06-02"
      }
    ],
    "memberIds": [
      "c0c989f4-22ab-4a17-8d65-c352b5f9218a",
      "63b86211-a549-4d81-9510-e727a894e740"
    ],
    "createdAt": "2025-06-02",
    "updatedAt": "2025-06-02"
  }
```
# 📄 Import JSON Examples
### 📥 Staff Import File (`staff-import.json`)
```json
[
  {
    "staffCode": "STAFF-02",
    "name": "John Doe",
    "email": "john.doe@company.com"
  },
  {
    "staffCode": "STAFF-03",
    "name": "Jane Smith",
    "email": "jane.smith@company.com"
  },
  {
    "staffCode": "STAFF-04",
    "name": "Bob Johnson",
    "email": "bob.johnson@company.com"
  },
  {
    "staffCode": "STAFF-05",
    "name": "Alice Williams",
    "email": "alice.williams@company.com"
  },
  {
    "staffCode": "STAFF-06",
    "name": "Charlie Brown",
    "email": "charlie.brown@company.com"
  },
  {
    "staffCode": "STAFF-07",
    "name": "Diana Prince",
    "email": "diana.prince@company.com"
  },
  {
    "staffCode": "STAFF-08",
    "name": "Edward Norton",
    "email": "edward.norton@company.com"
  },
  {
    "staffCode": "STAFF-09",
    "name": "Fiona Green",
    "email": "fiona.green@company.com"
  }
]
```
### 📥 Staff Group Import File (staff-groups-import.json`)
```json
[
  {
    "groupCode": "COMPANY",
    "name": "Company",
    "childrenCodes": ["IT-DEPT", "HR-DEPT", "SALES-DEPT"]
  },
  {
    "groupCode": "IT-DEPT",
    "name": "Information Technology Department",
    "parentCode": "COMPANY",
    "childrenCodes": ["DEV-TEAM", "QA-TEAM", "DEVOPS-TEAM"]
  },
  {
    "groupCode": "DEV-TEAM",
    "name": "Development Team",
    "parentCode": "IT-DEPT",
    "childrenCodes": ["FRONTEND-TEAM", "BACKEND-TEAM"]
  },
  {
    "groupCode": "FRONTEND-TEAM",
    "name": "Frontend Development Team",
    "parentCode": "DEV-TEAM"
  },
  {
    "groupCode": "BACKEND-TEAM",
    "name": "Backend Development Team",
    "parentCode": "DEV-TEAM"
  },
  {
    "groupCode": "QA-TEAM",
    "name": "Quality Assurance Team",
    "parentCode": "IT-DEPT"
  },
  {
    "groupCode": "DEVOPS-TEAM",
    "name": "Devops Team",
    "parentCode": "IT-DEPT"
  },
  {
    "groupCode": "HR-DEPT",
    "name": "Human Resources Department",
    "parentCode": "COMPANY"
  },
  {
    "groupCode": "SALES-DEPT",
    "name": "Sales Department",
    "parentCode": "COMPANY"
  }
]
```

---

**Happy Coding!** 🚀
