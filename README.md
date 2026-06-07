# 🗄️ Database Visualizer & Manager
 
A full-featured **Java Desktop Application** that combines traditional SQL query execution with an intuitive GUI and interactive data visualizations — making database management accessible to both technical and non-technical users.
 
> **Object-Oriented Programming Course Project** — Demonstrating real-world application of OOP principles using Java, Oracle DB, and JFreeChart.
 
---
 
## 📌 Overview
 
Most database tools force users to either write raw SQL or use oversimplified visual interfaces. **Database Visualizer & Manager** bridges that gap — it lets power users write SQL directly while giving beginners simple buttons to interact with and understand data without touching a single query.
 
---
 
## ✨ Features
 
### 🖱️ GUI-Based Database Operations
- **View Data** — Browse any table's records with a clean, readable table interface
- **Insert Data** — Add new records through simple form-style inputs — no SQL needed
- **Analyze Data** — Run statistical summaries and analysis on selected tables
### 💻 SQL Query Editor
- Write and execute **custom SQL queries** directly in the application
- View real-time results in a formatted output panel
- Supports standard Oracle SQL syntax (SELECT, INSERT, UPDATE, DELETE, etc.)
### 📊 Data Visualization
- Auto-generate **charts and graphs** from your database data using JFreeChart
- Visual representations help users understand data trends at a glance
- Designed to make database content approachable for non-technical stakeholders
### 🔗 Oracle Database Connectivity
- Connects to **Oracle Database** via JDBC (`ojdbc17`)
- Secure and configurable database connection setup
---
 
## 🛠️ Tech Stack
 
| Component | Technology |
|---|---|
| Language | Java |
| Database | Oracle DB (via JDBC) |
| Charting | JFreeChart 1.5.4 |
| Data Support | JCommon 1.0.24 |
| JSON Handling | org.json (json-20140107) |
| Architecture | Object-Oriented Programming (OOP) |
| UI | Java Swing |
 
---
 
## 📁 Project Structure
 
```
Database-Visualizer-Manager/
│
├── src/                        # All Java source files
│
├── ojdbc17.jar                 # Oracle JDBC driver
├── jfreechart-1.5.4.jar        # Chart rendering library
├── jcommon-1.0.24.jar          # JFreeChart dependency
├── json-20140107.jar           # JSON parsing library
│
├── Project Report.pdf          # Full project documentation & report
└── LICENSE                     # MIT License
```
 
---
 
## 🚀 Getting Started
 
### Prerequisites
- Java JDK 8 or higher
- Oracle Database instance (local or remote)
- An IDE such as IntelliJ IDEA or Eclipse (recommended)
### Setup & Run
 
1. **Clone the repository**
   ```bash
   git clone https://github.com/arsal-nia/Database-Visualizer-Manager.git
   cd Database-Visualizer-Manager
   ```
 
2. **Add JARs to your project classpath**
   Include all `.jar` files from the root directory:
   - `ojdbc17.jar`
   - `jfreechart-1.5.4.jar`
   - `jcommon-1.0.24.jar`
   - `json-20140107.jar`
3. **Configure your database connection**
   Update the DB credentials in the source code (host, port, SID/service name, username, password) to match your Oracle instance.
4. **Build and run**
   ```bash
   javac -cp ".;ojdbc17.jar;jfreechart-1.5.4.jar;jcommon-1.0.24.jar;json-20140107.jar" src/*.java
   java -cp ".;ojdbc17.jar;jfreechart-1.5.4.jar;jcommon-1.0.24.jar;json-20140107.jar" Main
   ```
   > On macOS/Linux, replace `;` with `:` in the classpath.
---
 
## 🎯 OOP Concepts Applied
 
This project was built as part of an **Object-Oriented Programming course** and demonstrates:
 
- **Encapsulation** — Database connection logic and query handling are separated into dedicated classes
- **Abstraction** — Clean interfaces between UI, business logic, and data layers
- **Inheritance & Polymorphism** — Reusable component hierarchies for GUI panels and data handlers
- **Separation of Concerns** — Each module (SQL editor, data viewer, chart generator) operates independently
---
 
## 📄 Documentation
 
A detailed **Project Report** is included in the repository covering:
- System design and architecture
- Class diagrams and OOP structure
- Feature walkthroughs
- Challenges and solutions
📎 [View Project Report (PDF)](./Project%20Report.pdf)
 
---
 
## 📜 License
 
This project is licensed under the [MIT License](./LICENSE).
 
---
 
## 👤 Author
 
**Arsal Khan**  
GitHub: [@arsal-nia](https://github.com/arsal-nia)
 
---
 
*Built with ❤️ as an OOP course project — showcasing how software engineering principles translate into real, functional applications.*
