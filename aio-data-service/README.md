### I. Database Design

#### 1. Staff
```json
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "staffId": {
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
`````
#### 2. Staff Group
```json
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "groupId": {
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
          "id": { "type": "keyword" },
          "groupId": { "type": "keyword" },
          "name": { "type": "text" },
          "parentId": { "type": "keyword" },
          "children": { "type": "nested" },
          "memberIds": { "type": "keyword" },
          "createdAt": { "type": "date", "format": "yyyy-MM-dd" },
          "updatedAt": { "type": "date", "format": "yyyy-MM-dd" }
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
