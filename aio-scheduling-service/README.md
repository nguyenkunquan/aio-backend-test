# 👥 AIO Scheduling Service
Rule-based scheduling service that manages weekly day-offs, shift transitions, and workload balance across 4-week cycles.

# 🚀 Features

- Generate weekly shift schedules asynchronously for given staff groups
- Communicate with Data Service via API to fetch data
- Store schedules and job metadata in PostgreSQL or similar
- Must support horizontal scaling
- Ensure job execution is distributed safely (e.g., avoid double processing)
- Must be asynchronous
- Logic must be stateless and idempotent
- Allow scheduling multiple groups in parallel
- Use Circuit Breaker for Data Service API calls
- Handle failures gracefully (log error, mark job failed)
- Configurable Schedule (plugin-like rule): each rule (e.g., 1 DAY-OFF , no MORNING after EVENING ) should be configurable - Enable/disable, Customize number of day-offs

# 🗃️ Database Schema
![image](https://github.com/user-attachments/assets/90f72a1a-b6ef-44e7-a880-15825c8fc416)

# 📖 API Documentation

### Swagger UI Access
Once the application is running, access the interactive API documentation at:
```
http://localhost:8081/api/v1/swagger-ui/index.html
```

# 📊 Sample Data
> ⚠️ **Note:** The `id` of each document is generated using `UUID.randomUUID()`.  
> Sample data is for demonstration purposes only — you must retrieve the actual ID after saving to the database.
### Schedule Job Entity Sample
```json
{
    "id": "e9348bf7-3cd3-4658-8c88-acc5a2a7b9d3",
    "staffGroupId": "5f729a8e-b8f2-4ca5-bc7c-4a5fc59d297c",
    "weekBeginDate": "2025-06-02",
    "status": "PENDING",
    "errorMessage": null
  }
```

### Shift Assignment Entity Sample
```json
{
    "id": "7c1450e2-9214-41db-8399-51241644b4b5"
    "jobId": "e9348bf7-3cd3-4658-8c88-acc5a2a7b9d3",
    "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
    "date": "2025-06-03",
    "shift": "EVENING",
},
```

### Schedule Result Sample
```json
{
  "scheduleId": "e9348bf7-3cd3-4658-8c88-acc5a2a7b9d3",
  "weekBeginDate": "2025-06-02",
  "staffGroupId": "5f729a8e-b8f2-4ca5-bc7c-4a5fc59d297c",
  "assignments": [
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-02",
      "shift": "DAY_OFF"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-03",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-04",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-05",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-06",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-07",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-08",
      "shift": "DAY_OFF"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-09",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-10",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-11",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-12",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-13",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-14",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-15",
      "shift": "DAY_OFF"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-16",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-17",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-18",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-19",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-20",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-21",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-22",
      "shift": "DAY_OFF"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-23",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-24",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-25",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-26",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-27",
      "shift": "MORNING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-28",
      "shift": "EVENING"
    },
    {
      "staffId": "addf8c31-f608-4d6f-89fe-f21e807d61d3",
      "date": "2025-06-29",
      "shift": "DAY_OFF"
    }
  ]
}
```

---

**Happy Coding!** 🚀
